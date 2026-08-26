package com.alrexu.parcool.compat.client;

import com.alrex.parcool.common.attachment.client.Animation;
import com.alrex.parcool.common.attachment.common.Parkourability;
import com.alrexu.parcool.compat.ParCoolCompatAddon;
import dev.kosmx.playerAnim.api.IPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Registers EMF's {@code registerVanillaModelCondition} via reflection so the dedicated server never loads EMF classes
 * (Entity Model Features is client-only). Resource-pack CEM poses yield whenever ParCool or a
 * Player Animator stack owns the player pose; those gameplay animations remain enabled.
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
            Method registerVanillaModel = api.getMethod("registerVanillaModelCondition", Function.class);
            Method registerAnimationPause = api.getMethod("registerPauseCondition", Function.class);
            Function<Object, Boolean> condition = EntityModelFeaturesCompat::gameplayAnimationOwnsPose;
            registerVanillaModel.invoke(null, condition);
            registerAnimationPause.invoke(null, condition);
            ParCoolCompatAddon.LOGGER.info("Registered ParCool + Entity Model Features gameplay-pose conditions");
        } catch (Throwable t) {
            ParCoolCompatAddon.LOGGER.warn("Could not register Entity Model Features compatibility (wrong EMF version?)", t);
        }
    }

    /**
     * Argument is EMF's {@code EMFEntity} at runtime; kept as Object so this class loads without EMF on the classpath.
     * The animator state is authoritative: it remains active through transition frames after an action flag clears,
     * and automatically covers every ParCool 3.x animator without depending on a resource pack or action list.
     */
    private static boolean gameplayAnimationOwnsPose(Object emfEntity) {
        if (!(emfEntity instanceof Player player)) {
            return false;
        }
        boolean ownsPose = FirstPersonRenderState.isRenderingHand();
        Animation parcoolAnimation = Animation.get(player);
        if (parcoolAnimation != null && parcoolAnimation.hasAnimator()) {
            ownsPose = true;
        }
        Parkourability parkourability = Parkourability.get(player);
        if (parkourability != null && !parkourability.isDoingNothing()) {
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
