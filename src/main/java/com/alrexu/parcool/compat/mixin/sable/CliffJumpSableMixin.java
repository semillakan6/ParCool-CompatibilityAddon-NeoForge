package com.alrexu.parcool.compat.mixin.sable;

import com.alrex.parcool.common.action.impl.ClimbUp;
import com.alrex.parcool.common.action.impl.WallJump;
import com.alrex.parcool.common.attachment.common.Parkourability;
import com.alrexu.parcool.compat.extern.sable.SableCollision;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

/** Releases a cached simulated ledge before ParCool applies its climb or wall-jump impulse. */
@Mixin({ClimbUp.class, WallJump.class})
public abstract class CliffJumpSableMixin {
    @Inject(method = "onStartInLocalClient", at = @At("HEAD"))
    private void parcoolCompatAddon$releaseSableCling(
            Player player,
            Parkourability parkourability,
            ByteBuffer startInfo,
            CallbackInfo callback
    ) {
        SableCollision.beginCliffJump(player);
    }
}
