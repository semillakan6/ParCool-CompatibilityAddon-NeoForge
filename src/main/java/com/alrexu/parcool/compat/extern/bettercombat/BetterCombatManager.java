package com.alrexu.parcool.compat.extern.bettercombat;

import com.alrexu.parcool.compat.extern.ModManager;
import net.neoforged.bus.api.IEventBus;

public class BetterCombatManager extends ModManager {
    @Override
    public void initWhenInstalled(IEventBus modBus, IEventBus forgeBus) {
    }

    @Override
    public void initInClient(IEventBus modBus, IEventBus forgeBus) {
        // Player Animator transforms are composed after ParCool by client mixins. Registering the
        // old event handler here would suppress model parts instead of composing both animations.
    }

    @Override
    public String getModID() {
        return "bettercombat";
    }
}
