package com.alrexu.parcool.compat.extern;

import com.alrexu.parcool.compat.extern.bettercombat.BetterCombatManager;
import com.alrexu.parcool.compat.extern.carryon.CarryOnManager;
import com.alrexu.parcool.compat.extern.playeranimator.PlayerAnimatorManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;

public class ExternalModManager {
    private static final List<IModManager> MOD_MANAGERS = List.of(
            new BetterCombatManager(),
            new PlayerAnimatorManager(),
            new CarryOnManager()
    );

    public static void init(IEventBus modBus, IEventBus forgeBus) {
        for (var manager : MOD_MANAGERS) {
            manager.init(modBus, forgeBus);
        }
        if (FMLEnvironment.dist.isClient()) {
            MOD_MANAGERS.forEach(manager -> manager.initInClient(modBus, forgeBus));
        } else {
            MOD_MANAGERS.forEach(manager -> manager.initInDedicatedServer(modBus, forgeBus));
        }
    }

    public static <T extends IModManager> T get(Class<T> clazz) {
        for (var manager : MOD_MANAGERS) {
            if (clazz.isInstance(manager)) {
                return (T) manager;
            }
        }
        throw new IllegalArgumentException("ModManager [" + clazz.getSimpleName() + "] is not registered");
    }
}
