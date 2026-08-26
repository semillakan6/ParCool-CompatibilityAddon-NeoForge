package com.alrexu.parcool.compat.client;

import com.alrex.parcool.common.attachment.client.Animation;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Moves Player Animator's model-part application after ParCool's setupAnim tail pass.
 * Each transform is still evaluated exactly once; only its ordering changes.
 */
public final class PlayerAnimatorComposition {
    private static final ThreadLocal<Deque<Scope>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);

    private PlayerAnimatorComposition() {
    }

    public static void begin(PlayerModel<?> model, LivingEntity entity) {
        Scope scope = null;
        if (entity instanceof AbstractClientPlayer player && player instanceof IAnimatedPlayer animatedPlayer) {
            Animation parcoolAnimation = Animation.get(player);
            AnimationApplier applier = animatedPlayer.playerAnimator_getAnimation();
            if (parcoolAnimation != null && parcoolAnimation.hasAnimator() && applier.isActive()) {
                scope = new Scope(model, applier);
            }
        }
        SCOPES.get().push(scope == null ? Scope.INACTIVE : scope);
    }

    public static boolean defer(AnimationApplier applier, String name, ModelPart part) {
        Deque<Scope> scopes = SCOPES.get();
        if (scopes.isEmpty()) {
            return false;
        }
        Scope scope = scopes.peek();
        if (!scope.active || scope.replaying || scope.applier != applier) {
            return false;
        }
        scope.parts.add(new DeferredPart(name, part));
        return true;
    }

    public static void finish(PlayerModel<?> model) {
        Deque<Scope> scopes = SCOPES.get();
        if (scopes.isEmpty()) {
            return;
        }
        Scope scope = scopes.pop();
        try {
            if (!scope.active || scope.model != model) {
                return;
            }
            scope.replaying = true;
            for (DeferredPart deferred : scope.parts) {
                scope.applier.updatePart(deferred.name, deferred.part);
            }
            syncOuterLayers(model);
        } finally {
            if (scopes.isEmpty()) {
                SCOPES.remove();
            }
        }
    }

    private static void syncOuterLayers(PlayerModel<?> model) {
        model.hat.copyFrom(model.head);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftPants.copyFrom(model.leftLeg);
        model.rightPants.copyFrom(model.rightLeg);
        model.jacket.copyFrom(model.body);
    }

    private record DeferredPart(String name, ModelPart part) {
    }

    private static final class Scope {
        private static final Scope INACTIVE = new Scope();

        private final boolean active;
        private final PlayerModel<?> model;
        private final AnimationApplier applier;
        private final List<DeferredPart> parts;
        private boolean replaying;

        private Scope() {
            this.active = false;
            this.model = null;
            this.applier = null;
            this.parts = List.of();
        }

        private Scope(PlayerModel<?> model, AnimationApplier applier) {
            this.active = true;
            this.model = model;
            this.applier = applier;
            this.parts = new ArrayList<>(6);
        }
    }
}
