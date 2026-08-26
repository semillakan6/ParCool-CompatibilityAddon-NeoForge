package com.alrexu.parcool.compat.extern.parcool4;

import com.alrexu.parcool.compat.ParCoolCompatAddon;
import com.alrexu.parcool.compat.extern.IModManager;
import net.neoforged.bus.api.IEventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Load-only placeholder for ParCool 4.x.
 *
 * <p>TODO when ParCool 4 is stable:</p>
 * <ul>
 *     <li>Port Carry On action cancellation to the new action events.</li>
 *     <li>Port EMF action checks to the new ParCoolActions API.</li>
 *     <li>Revisit Better Combat and Player Animator cancellation after the animation-engine rewrite.</li>
 * </ul>
 */
public final class ParCool4Manager implements IModManager {
    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Override
    public void init(IEventBus modBus, IEventBus forgeBus) {
        if (LOGGED.compareAndSet(false, true)) {
            ParCoolCompatAddon.LOGGER.info(
                    "ParCool 4.x detected; compatibility handlers are placeholders until the stable 4.x API is supported"
            );
        }
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public String getModID() {
        return "parcool";
    }
}
