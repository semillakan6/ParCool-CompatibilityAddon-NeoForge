package com.alrexu.parcool.compat.mixin.client;

import com.alrexu.parcool.compat.client.EntityModelFeaturesCompat;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Selects vanilla geometry for the base player model without changing EMF armor model variants. */
@Mixin(value = PlayerModel.class, priority = 2000)
public abstract class PlayerModelEmfCompatMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void parcoolCompatAddon$prepareEmfPlayerModel(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo callback
    ) {
        if (entity instanceof Player player) {
            EntityModelFeaturesCompat.preparePlayerModel((PlayerModel<?>) (Object) this, player);
        }
    }
}
