package com.alrexu.parcool.compat.mixin.client;

import com.alrexu.parcool.compat.client.PlayerAnimatorComposition;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents the normal pre-ParCool application when a composition scope owns this applier. */
@Mixin(value = AnimationApplier.class, remap = false)
public abstract class AnimationApplierCompositionMixin {
    @Inject(method = "updatePart", at = @At("HEAD"), cancellable = true, remap = false)
    private void parcoolCompatAddon$deferPart(String name, ModelPart part, CallbackInfo callback) {
        if (PlayerAnimatorComposition.defer((AnimationApplier) (Object) this, name, part)) {
            callback.cancel();
        }
    }
}
