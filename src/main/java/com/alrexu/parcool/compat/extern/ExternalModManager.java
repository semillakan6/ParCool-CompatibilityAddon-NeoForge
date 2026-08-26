package com.alrexu.parcool.compat.extern;

import com.alrexu.parcool.compat.ParCoolCompatAddon;
import com.alrexu.parcool.compat.ParCoolVersion;
import com.alrexu.parcool.compat.extern.parcool4.ParCool4Manager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;

public class ExternalModManager {
    private static final List<String> V3_MANAGER_CLASSES = List.of(
            "com.alrexu.parcool.compat.extern.bettercombat.BetterCombatManager",
            "com.alrexu.parcool.compat.extern.playeranimator.PlayerAnimatorManager",
            "com.alrexu.parcool.compat.extern.carryon.CarryOnManager"
    );
    private static List<IModManager> modManagers = List.of();

    public static void init(IEventBus modBus, IEventBus forgeBus) {
        modManagers = switch (ParCoolVersion.current()) {
            case V3 -> loadV3Managers();
            case V4_OR_NEWER -> List.of(new ParCool4Manager());
            case UNKNOWN -> List.of();
        };

        for (var manager : modManagers) {
            manager.init(modBus, forgeBus);
        }
        if (FMLEnvironment.dist.isClient()) {
            modManagers.forEach(manager -> manager.initInClient(modBus, forgeBus));
        } else {
            modManagers.forEach(manager -> manager.initInDedicatedServer(modBus, forgeBus));
        }
    }

    public static <T extends IModManager> T get(Class<T> clazz) {
        for (var manager : modManagers) {
            if (clazz.isInstance(manager)) {
                return clazz.cast(manager);
            }
        }
        throw new IllegalArgumentException("ModManager [" + clazz.getSimpleName() + "] is not registered");
    }

    private static List<IModManager> loadV3Managers() {
        List<IModManager> managers = new ArrayList<>();
        for (String className : V3_MANAGER_CLASSES) {
            try {
                Class<?> managerClass = Class.forName(className);
                managers.add((IModManager) managerClass.getDeclaredConstructor().newInstance());
            } catch (Throwable t) {
                ParCoolCompatAddon.LOGGER.warn("Failed to load ParCool 3.x compatibility manager {}", className, t);
            }
        }
        return List.copyOf(managers);
    }
}
