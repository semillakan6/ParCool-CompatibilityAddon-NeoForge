package com.alrexu.parcool.compat.mixin.client;

import com.alrexu.parcool.compat.client.PlayerAnimatorComposition;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Scopes Player Animator deferral and replays it after ParCool's default-order tail injector. */
@Mixin(value = PlayerModel.class, priority = 900)
public abstract class PlayerModelAnimationCompositionMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"), order = 900)
    private void parcoolCompatAddon$beginAnimationComposition(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo callback
    ) {
        PlayerAnimatorComposition.begin((PlayerModel<?>) (Object) this, entity);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"), order = 2000)
    private void parcoolCompatAddon$finishAnimationComposition(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo callback
    ) {
        PlayerAnimatorComposition.finish((PlayerModel<?>) (Object) this);
    }
}
