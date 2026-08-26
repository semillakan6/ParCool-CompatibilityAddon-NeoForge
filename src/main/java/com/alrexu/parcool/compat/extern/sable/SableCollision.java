package com.alrexu.parcool.compat.extern.sable;

import com.alrex.parcool.common.action.impl.HangDown;
import com.alrexu.parcool.compat.ParCoolCompatAddon;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Extends world-space collision probes into the plot space used by Sable sub-levels.
 */
public final class SableCollision {
    private static final ThreadLocal<Boolean> CHECKING_SUB_LEVELS = ThreadLocal.withInitial(() -> false);
    private static final AtomicBoolean FAILURE_LOGGED = new AtomicBoolean();
    private static final double CLIFF_CONTACT_TOLERANCE = 0.08;
    private static final long CLIFF_CONTACT_GRACE_TICKS = 4;
    private static final double ASCENDING_CLIFF_HISTORY = 0.30;
    private static final double ASCENDING_CLIFF_STEP = 0.05;
    private static final double SUB_LEVEL_MOTION_SEARCH_MARGIN = 1.0;
    private static final double[] SWEPT_POSE_STEPS = {0.25, 0.5, 0.75};
    private static final Map<LivingEntity, CachedWall> LAST_GRABBABLE_WALL = new WeakHashMap<>();

    private SableCollision() {
    }

    public static boolean isCheckingSubLevels() {
        return CHECKING_SUB_LEVELS.get();
    }

    public static boolean hasSubLevelCollision(Level level, AABB worldBounds) {
        return hasSubLevelCollision(level, worldBounds, false);
    }

    /**
     * Checks cliff contact through the sub-level's last-to-current tick motion. Sable transports
     * players using both poses, so a contact query made between those updates can otherwise miss
     * a wall for one tick and make ClingToCliff stop or fail to start.
     */
    private static boolean hasSweptSubLevelContact(Level level, AABB worldBounds) {
        return hasSubLevelCollision(level, worldBounds, true);
    }

    private static boolean hasSubLevelCollision(Level level, AABB worldBounds, boolean includeTickMotion) {
        if (isCheckingSubLevels()) {
            return false;
        }

        CHECKING_SUB_LEVELS.set(true);
        try {
            BoundingBox3d globalBounds = new BoundingBox3d(worldBounds);
            BoundingBox3d candidateBounds = includeTickMotion
                    ? new BoundingBox3d(globalBounds).expand(SUB_LEVEL_MOTION_SEARCH_MARGIN)
                    : globalBounds;
            for (SubLevelAccess subLevel : SableCompanion.INSTANCE.getAllIntersecting(level, candidateBounds)) {
                Pose3dc currentPose = subLevel.logicalPose();
                if (hasCollisionAtPose(level, worldBounds, globalBounds, currentPose)) {
                    return true;
                }
                if (!includeTickMotion) {
                    continue;
                }

                Pose3dc lastPose = subLevel.lastPose();
                if (hasCollisionAtPose(level, worldBounds, globalBounds, lastPose)) {
                    return true;
                }
                Pose3d samplePose = new Pose3d();
                for (double step : SWEPT_POSE_STEPS) {
                    lastPose.lerp(currentPose, step, samplePose);
                    if (hasCollisionAtPose(level, worldBounds, globalBounds, samplePose)) {
                        return true;
                    }
                }
            }
        } catch (RuntimeException | LinkageError error) {
            logFailure(error);
        } finally {
            CHECKING_SUB_LEVELS.remove();
        }
        return false;
    }

    private static boolean hasCollisionAtPose(
            Level level,
            AABB worldBounds,
            BoundingBox3d globalBounds,
            Pose3dc pose
    ) {
        BoundingBox3d localBounds = new BoundingBox3d();
        globalBounds.transformInverse(pose, localBounds);

        // This inverse-transformed AABB is broad phase only. Using it as the collision
        // result overestimates rotated boxes and makes empty ledge clearance look occupied.
        for (var shape : level.getBlockCollisions(null, localBounds.toMojang())) {
            for (AABB localShape : shape.toAabbs()) {
                if (intersectsTransformedBox(worldBounds, localShape, pose)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Repeats ParCool's lower-contact/upper-clearance cliff test against Sable shapes directly.
     * This fallback avoids the main-world BlockState/friction phase that follows WorldUtil's probes.
     */
    public static Vec3 findGrabbableWall(LivingEntity entity) {
        CachedWall cached;
        synchronized (LAST_GRABBABLE_WALL) {
            cached = LAST_GRABBABLE_WALL.get(entity);
        }

        Vec3 currentWall = findGrabbableWallAtOffset(entity, 0);
        VerticalWall wall = currentWall == null ? null : new VerticalWall(currentWall, 0);

        // Once an ascending probe found the ledge, keep validating at that same relative
        // height. ClingToCliff stops vertical movement immediately, so checking only the
        // unshifted narrow band on the following tick would discard a valid grab again.
        if (wall == null && cached != null && cached.verticalOffset != 0) {
            Vec3 cachedOffsetWall = findGrabbableWallAtOffset(entity, cached.verticalOffset);
            if (cachedOffsetWall != null) {
                wall = new VerticalWall(cachedOffsetWall, cached.verticalOffset);
            }
        }

        // ClingToCliff deliberately does not query WorldUtil until upward velocity drops below
        // 0.2. A normal jump can travel farther than its ledge-height band between those ticks.
        // Look back through only that short vertical interval for Sable collision geometry.
        if (wall == null && entity.getDeltaMovement().y > 0) {
            for (double offset = -ASCENDING_CLIFF_STEP;
                 offset >= -ASCENDING_CLIFF_HISTORY - 1.0E-7;
                 offset -= ASCENDING_CLIFF_STEP) {
                Vec3 historicalWall = findGrabbableWallAtOffset(entity, offset);
                if (historicalWall != null) {
                    wall = new VerticalWall(historicalWall, offset);
                    break;
                }
            }
        }

        long gameTime = entity.level().getGameTime();
        synchronized (LAST_GRABBABLE_WALL) {
            if (wall != null) {
                LAST_GRABBABLE_WALL.put(entity, new CachedWall(wall.direction, wall.verticalOffset, gameTime));
                return wall.direction;
            }
            if (cached != null && gameTime - cached.gameTime <= CLIFF_CONTACT_GRACE_TICKS) {
                return cached.direction;
            }
            LAST_GRABBABLE_WALL.remove(entity);
            return null;
        }
    }

    private static Vec3 findGrabbableWallAtOffset(LivingEntity entity, double verticalOffset) {
        double horizontalDistance = entity.getBbWidth() / 2.0;
        double middleHeight = entity.getEyeHeight() + (entity.getBbHeight() - entity.getEyeHeight()) / 2.0;
        Vec3 wall = findGrabbableWall(entity, horizontalDistance, middleHeight, verticalOffset);
        if (wall == null) {
            double upperHeight = entity.getBbHeight()
                    + (entity.getBbHeight() - entity.getEyeHeight()) / 2.0;
            wall = findGrabbableWall(entity, horizontalDistance, upperHeight, verticalOffset);
        }
        return wall;
    }

    private static Vec3 findGrabbableWall(
            LivingEntity entity,
            double distance,
            double heightOffset,
            double verticalOffset
    ) {
        Vec3 position = entity.position();
        double baseY = position.y + verticalOffset;
        double halfWidth = entity.getBbWidth() * 0.49;
        AABB contactSlice = new AABB(
                position.x - halfWidth,
                baseY + heightOffset - entity.getBbHeight() / 6.0,
                position.z - halfWidth,
                position.x + halfWidth,
                baseY + heightOffset,
                position.z + halfWidth
        );
        AABB clearance = new AABB(
                position.x - halfWidth,
                baseY + heightOffset,
                position.z - halfWidth,
                position.x + halfWidth,
                baseY + entity.getBbHeight(),
                position.z + halfWidth
        );

        int x = 0;
        int z = 0;
        if (isGrabbableDirection(entity.level(), contactSlice, clearance, distance, 0)) {
            x++;
        }
        if (isGrabbableDirection(entity.level(), contactSlice, clearance, -distance, 0)) {
            x--;
        }
        if (isGrabbableDirection(entity.level(), contactSlice, clearance, 0, distance)) {
            z++;
        }
        if (isGrabbableDirection(entity.level(), contactSlice, clearance, 0, -distance)) {
            z--;
        }
        return x == 0 && z == 0 ? null : new Vec3(x, 0, z);
    }

    private static boolean isGrabbableDirection(
            Level level,
            AABB contactSlice,
            AABB clearance,
            double xDistance,
            double zDistance
    ) {
        double contactX = xDistance == 0
                ? 0
                : xDistance + Math.copySign(CLIFF_CONTACT_TOLERANCE, xDistance);
        double contactZ = zDistance == 0
                ? 0
                : zDistance + Math.copySign(CLIFF_CONTACT_TOLERANCE, zDistance);
        return hasSweptSubLevelContact(level, contactSlice.expandTowards(contactX, 0, contactZ))
                && !hasSubLevelCollision(level, clearance.expandTowards(xDistance, 0, zDistance));
    }

    /** Finds the plot block that vanilla getHangableBars cannot see after its collision probe. */
    public static HangDown.BarAxis findHangableBar(LivingEntity entity) {
        Level level = entity.level();
        double halfWidth = entity.getBbWidth() / 4.0;
        AABB overhead = new AABB(
                entity.getX() - halfWidth,
                entity.getY() + entity.getBbHeight(),
                entity.getZ() - halfWidth,
                entity.getX() + halfWidth,
                entity.getY() + entity.getBbHeight() + 0.35,
                entity.getZ() + halfWidth
        );

        try {
            BoundingBox3d globalBounds = new BoundingBox3d(overhead);
            for (SubLevelAccess subLevel : SableCompanion.INSTANCE.getAllIntersecting(level, globalBounds)) {
                Vector3d localSample = subLevel.logicalPose().transformPositionInverse(
                        new Vector3d(entity.getX(), entity.getY() + entity.getBbHeight() + 0.4, entity.getZ()),
                        new Vector3d()
                );
                BlockPos localPos = BlockPos.containing(localSample.x, localSample.y, localSample.z);
                Direction.Axis localAxis = classifyHangableAxis(level, localPos, level.getBlockState(localPos));
                if (localAxis == null) {
                    continue;
                }

                HangDown.BarAxis worldAxis = transformHorizontalAxis(localAxis, subLevel.logicalPose());
                if (worldAxis != null && hasSubLevelCollision(level, overhead)) {
                    return worldAxis;
                }
            }
        } catch (RuntimeException | LinkageError error) {
            logFailure(error);
        }
        return null;
    }

    private static Direction.Axis classifyHangableAxis(Level level, BlockPos pos, BlockState state) {
        var block = state.getBlock();
        if (block instanceof RotatedPillarBlock) {
            if (state.isCollisionShapeFullBlock(level, pos)) {
                return null;
            }
            Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
            return axis.isHorizontal() ? axis : null;
        }
        if (block instanceof EndRodBlock) {
            if (state.isCollisionShapeFullBlock(level, pos)) {
                return null;
            }
            Direction.Axis axis = state.getValue(DirectionalBlock.FACING).getAxis();
            return axis.isHorizontal() ? axis : null;
        }
        if (block instanceof CrossCollisionBlock) {
            int northSouth = bool(state, CrossCollisionBlock.NORTH) + bool(state, CrossCollisionBlock.SOUTH);
            int eastWest = bool(state, CrossCollisionBlock.EAST) + bool(state, CrossCollisionBlock.WEST);
            if (northSouth > 0 && eastWest == 0) {
                return Direction.Axis.Z;
            }
            if (eastWest > 0 && northSouth == 0) {
                return Direction.Axis.X;
            }
            return null;
        }
        if (block instanceof WallBlock) {
            int northSouth = wall(state, WallBlock.NORTH_WALL) + wall(state, WallBlock.SOUTH_WALL);
            int eastWest = wall(state, WallBlock.EAST_WALL) + wall(state, WallBlock.WEST_WALL);
            if (northSouth > 0 && eastWest == 0) {
                return Direction.Axis.Z;
            }
            if (eastWest > 0 && northSouth == 0) {
                return Direction.Axis.X;
            }
        }
        return null;
    }

    private static int bool(BlockState state, BooleanProperty property) {
        return state.getValue(property) ? 1 : 0;
    }

    private static int wall(BlockState state, EnumProperty<WallSide> property) {
        return state.getValue(property) == WallSide.NONE ? 0 : 1;
    }

    private static HangDown.BarAxis transformHorizontalAxis(Direction.Axis localAxis, Pose3dc pose) {
        Vector3d local = localAxis == Direction.Axis.X ? new Vector3d(1, 0, 0) : new Vector3d(0, 0, 1);
        Vector3d world = pose.transformNormal(local, new Vector3d());
        double horizontal = Math.max(Math.abs(world.x), Math.abs(world.z));
        if (horizontal <= Math.abs(world.y)) {
            return null;
        }
        return Math.abs(world.x) >= Math.abs(world.z) ? HangDown.BarAxis.X : HangDown.BarAxis.Z;
    }

    private static boolean intersectsTransformedBox(AABB worldBox, AABB localBox, Pose3dc pose) {
        Vector3d worldCenter = new Vector3d(
                (worldBox.minX + worldBox.maxX) * 0.5,
                (worldBox.minY + worldBox.maxY) * 0.5,
                (worldBox.minZ + worldBox.maxZ) * 0.5
        );
        Vector3d localCenter = new Vector3d(
                (localBox.minX + localBox.maxX) * 0.5,
                (localBox.minY + localBox.maxY) * 0.5,
                (localBox.minZ + localBox.maxZ) * 0.5
        );
        Vector3d transformedCenter = pose.transformPosition(localCenter, new Vector3d());
        Vector3d delta = transformedCenter.sub(worldCenter, new Vector3d());

        double[] worldHalf = {
                (worldBox.maxX - worldBox.minX) * 0.5,
                (worldBox.maxY - worldBox.minY) * 0.5,
                (worldBox.maxZ - worldBox.minZ) * 0.5
        };
        double[] localHalf = {
                (localBox.maxX - localBox.minX) * 0.5 * Math.abs(pose.scale().x()),
                (localBox.maxY - localBox.minY) * 0.5 * Math.abs(pose.scale().y()),
                (localBox.maxZ - localBox.minZ) * 0.5 * Math.abs(pose.scale().z())
        };
        Vector3d[] localAxes = {
                pose.orientation().transform(new Vector3d(1, 0, 0)),
                pose.orientation().transform(new Vector3d(0, 1, 0)),
                pose.orientation().transform(new Vector3d(0, 0, 1))
        };
        for (Vector3d axis : localAxes) {
            axis.normalize();
        }

        Vector3d[] worldAxes = {new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1)};
        Vector3d[] axes = new Vector3d[15];
        int count = 0;
        for (Vector3d axis : worldAxes) {
            axes[count++] = axis;
        }
        for (Vector3d axis : localAxes) {
            axes[count++] = axis;
        }
        for (Vector3d worldAxis : worldAxes) {
            for (Vector3d localAxis : localAxes) {
                axes[count++] = worldAxis.cross(localAxis, new Vector3d());
            }
        }

        for (Vector3d axis : axes) {
            if (axis.lengthSquared() < 1.0E-12) {
                continue;
            }
            double distance = Math.abs(delta.dot(axis));
            double worldRadius = worldHalf[0] * Math.abs(axis.x)
                    + worldHalf[1] * Math.abs(axis.y)
                    + worldHalf[2] * Math.abs(axis.z);
            double localRadius = localHalf[0] * Math.abs(axis.dot(localAxes[0]))
                    + localHalf[1] * Math.abs(axis.dot(localAxes[1]))
                    + localHalf[2] * Math.abs(axis.dot(localAxes[2]));
            if (distance > worldRadius + localRadius + 1.0E-7) {
                return false;
            }
        }
        return true;
    }

    private static void logFailure(Throwable error) {
        if (FAILURE_LOGGED.compareAndSet(false, true)) {
            ParCoolCompatAddon.LOGGER.warn(
                    "Sable collision compatibility failed; falling back to vanilla collision checks",
                    error
            );
        }
    }

    private record VerticalWall(Vec3 direction, double verticalOffset) {
    }

    private record CachedWall(Vec3 direction, double verticalOffset, long gameTime) {
    }
}
