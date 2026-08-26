package com.alrexu.parcool.compat.mixin.client;

import com.alrexu.parcool.compat.client.FirstPersonRenderState;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Marks Minecraft's private first-person hand pass before EMF evaluates its model conditions. */
@Mixin(value = PlayerRenderer.class, priority = 2000)
public abstract class PlayerRendererFirstPersonStateMixin {
    @Inject(method = "renderHand", at = @At("HEAD"))
    private void parcoolCompatAddon$enterFirstPersonHand(CallbackInfo callback) {
        FirstPersonRenderState.enterHandRender();
    }

    @Inject(method = "renderHand", at = @At("RETURN"))
    private void parcoolCompatAddon$exitFirstPersonHand(CallbackInfo callback) {
        FirstPersonRenderState.exitHandRender();
    }
}
