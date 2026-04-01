package com.alrexu.parcool.compat;

import com.alrexu.parcool.compat.extern.ExternalModManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ParCoolCompatAddon.MOD_ID)
public class ParCoolCompatAddon {
    public static final String MOD_ID = "parcool_compat_addon";
    public static final Logger LOGGER = LogManager.getLogger();

    public ParCoolCompatAddon(IEventBus modBus) {
        var forgeBus = NeoForge.EVENT_BUS;
        ExternalModManager.init(modBus, forgeBus);
    }
}
