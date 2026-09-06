package com.alrexu.parcool.compat.client;

import com.alrex.parcool.common.attachment.client.Animation;
import com.alrex.parcool.common.attachment.common.Parkourability;
import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.impl.FastRun;
import com.alrex.parcool.common.action.impl.FastSwim;
import com.alrexu.parcool.compat.ParCoolCompatAddon;
import dev.kosmx.playerAnim.api.IPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import traben.entity_model_features.models.IEMFModel;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Coordinates EMF with gameplay animations without applying an entity-wide vanilla-model override.
 * Only the base player model yields; armor and equipment models keep their selected EMF variants.
 */
public final class EntityModelFeaturesCompat {
    private static final long OWNERSHIP_LINGER_TICKS = 6;
    private static final Map<Player, Long> POSE_OWNERSHIP_UNTIL = new WeakHashMap<>();

    private EntityModelFeaturesCompat() {
    }

    /**
     * Called from {@link com.alrexu.parcool.compat.ParCoolCompatAddon} on the mod bus on physical client only.
     */
    public static void register() {
        if (!ModList.get().isLoaded("entity_model_features")) {
            return;
        }
        try {
            Class<?> api = Class.forName("traben.entity_model_features.EMFAnimationApi");
            Method registerAnimationPause = api.getMethod("registerPauseCondition", Function.class);
            Function<Object, Boolean> condition = EntityModelFeaturesCompat::shouldPauseEmfAnimation;
            registerAnimationPause.invoke(null, condition);
            ParCoolCompatAddon.LOGGER.info("Registered render-pass-scoped ParCool + Entity Model Features compatibility");
        } catch (Throwable t) {
            ParCoolCompatAddon.LOGGER.warn("Could not register Entity Model Features compatibility (wrong EMF version?)", t);
        }
    }

    /** Selects EMF's vanilla state only on the renderer's base PlayerModel root. */
    public static void preparePlayerModel(PlayerModel<?> model, Player player) {
        if (!isBasePlayerPass() || !gameplayAnimationOwnsPose(player)) {
            return;
        }
        if (model instanceof IEMFModel emfModel && emfModel.emf$isEMFModel()) {
            emfModel.emf$getEMFRootModel().setVariantStateTo(0);
        }
    }

    private static boolean shouldPauseEmfAnimation(Object emfEntity) {
        return emfEntity instanceof Player player
                && isBasePlayerPass()
                && gameplayAnimationOwnsPose(player);
    }

    private static boolean isBasePlayerPass() {
        return PlayerBaseRenderState.isRenderingBaseModel() || FirstPersonRenderState.isRenderingHand();
    }

    /**
     * Argument is EMF's {@code EMFEntity} at runtime; kept as Object so this class loads without EMF on the classpath.
     * The animator state is authoritative: it remains active through transition frames after an action flag clears,
     * and automatically covers every ParCool 3.x animator without depending on a resource pack or action list.
     */
    private static boolean gameplayAnimationOwnsPose(Player player) {
        boolean ownsPose = FirstPersonRenderState.isRenderingHand();
        Parkourability parkourability = Parkourability.get(player);
        boolean fastMovementAction = parkourability != null
                && parkourability.isDoingAny(FastRun.class, FastSwim.class);
        boolean nonFastAction = parkourability != null
                && parkourability.getList().stream()
                .filter(Action::isDoing)
                .anyMatch(action -> !(action instanceof FastRun) && !(action instanceof FastSwim));

        Animation parcoolAnimation = Animation.get(player);
        if (nonFastAction || (parcoolAnimation != null && parcoolAnimation.hasAnimator() && !fastMovementAction)) {
            ownsPose = true;
        }
        if (player instanceof IPlayer animatedPlayer && animatedPlayer.getAnimationStack().isActive()) {
            ownsPose = true;
        }

        long gameTime = player.level().getGameTime();
        synchronized (POSE_OWNERSHIP_UNTIL) {
            if (ownsPose) {
                POSE_OWNERSHIP_UNTIL.put(player, gameTime + OWNERSHIP_LINGER_TICKS);
                return true;
            }
            Long until = POSE_OWNERSHIP_UNTIL.get(player);
            if (until != null && gameTime <= until) {
                return true;
            }
            POSE_OWNERSHIP_UNTIL.remove(player);
            return false;
        }
    }
}
