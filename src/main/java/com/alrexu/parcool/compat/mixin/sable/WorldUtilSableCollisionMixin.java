package com.alrexu.parcool.compat.mixin.sable;

import com.alrex.parcool.common.action.impl.HangDown;
import com.alrex.parcool.utilities.WorldUtil;
import com.alrexu.parcool.compat.extern.sable.SableCollision;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds Sable plot-space collision only to ParCool's environment probes.
 *
 * <p>Do not mix this into {@code Level} or {@code CollisionGetter}: changing the global
 * {@code noCollision} result also changes vanilla pose selection and fights Sable's own
 * entity movement correction, resulting in swimming poses and jitter on sub-levels.</p>
 */
@Mixin(WorldUtil.class)
public abstract class WorldUtilSableCollisionMixin {
    @Inject(
            method = "getGrabbableWall(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void parcoolCompatAddon$findSableGrabbableWall(
            LivingEntity entity,
            CallbackInfoReturnable<Vec3> callback
    ) {
        if (callback.getReturnValue() == null) {
            Vec3 sableWall = SableCollision.findGrabbableWall(entity);
            if (sableWall != null) {
                callback.setReturnValue(sableWall);
            }
        }
    }

    @Inject(
            method = "getHangableBars(Lnet/minecraft/world/entity/LivingEntity;)Lcom/alrex/parcool/common/action/impl/HangDown$BarAxis;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void parcoolCompatAddon$findSableHangableBar(
            LivingEntity entity,
            CallbackInfoReturnable<HangDown.BarAxis> callback
    ) {
        if (callback.getReturnValue() == null) {
            HangDown.BarAxis sableAxis = SableCollision.findHangableBar(entity);
            if (sableAxis != null) {
                callback.setReturnValue(sableAxis);
            }
        }
    }

    @Redirect(
            method = {
                    "getVaultableStep",
                    "getWallHeight",
                    "getHangableBars",
                    "existsDivableSpace",
                    "getGrabbableWall"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"
            )
    )
    private static boolean parcoolCompatAddon$includeSableCollision(
            Level level,
            @Nullable Entity entity,
            AABB bounds
    ) {
        return level.noCollision(entity, bounds) && !SableCollision.hasSubLevelCollision(level, bounds);
    }

    @Redirect(
            method = {
                    "existsSpaceBelow",
                    "existsDivableSpace",
                    "lambda$getWall$7",
                    "lambda$getWall$6",
                    "lambda$getWall$5",
                    "lambda$getWall$4",
                    "lambda$getRunnableWall$3",
                    "lambda$getRunnableWall$2",
                    "lambda$getRunnableWall$1",
                    "lambda$getRunnableWall$0"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/phys/AABB;)Z"
            )
    )
    private static boolean parcoolCompatAddon$includeSableCollision(Level level, AABB bounds) {
        return level.noCollision(bounds) && !SableCollision.hasSubLevelCollision(level, bounds);
    }
}
