package com.alrexu.parcool.compat.mixin.sable;

import com.alrex.parcool.common.action.impl.ClingToCliff;
import com.alrex.parcool.common.attachment.common.Parkourability;
import com.alrexu.parcool.compat.extern.sable.SableCollision;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents a departing simulated cling from zeroing the jump impulse in the same action chain. */
@Mixin(ClingToCliff.class)
public abstract class ClingReleaseSableMixin {
    @Inject(method = "onWorkingTickInLocalClient", at = @At("HEAD"), cancellable = true)
    private void parcoolCompatAddon$preserveCliffJumpMomentum(
            Player player,
            Parkourability parkourability,
            CallbackInfo callback
    ) {
        if (SableCollision.isCliffJumpReleaseActive(player)) {
            callback.cancel();
        }
    }
}
