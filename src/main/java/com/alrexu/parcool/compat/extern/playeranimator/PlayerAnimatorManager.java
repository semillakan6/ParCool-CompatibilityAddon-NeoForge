package com.alrexu.parcool.compat.extern.playeranimator;

import com.alrexu.parcool.compat.extern.ModManager;
import net.neoforged.bus.api.IEventBus;

public class PlayerAnimatorManager extends ModManager {
    @Override
    public void initWhenInstalled(IEventBus modBus, IEventBus forgeBus) {
    }

    @Override
    public void initInClient(IEventBus modBus, IEventBus forgeBus) {
        // The model composition layer handles all Player Animator stacks without cancelling a
        // complete ParCool animator or depending on animation/resource-pack identifiers.
    }

    @Override
    public String getModID() {
        return "playeranimator";
    }
}
