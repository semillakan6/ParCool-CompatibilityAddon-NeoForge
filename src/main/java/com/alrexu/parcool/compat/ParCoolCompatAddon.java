package com.alrexu.parcool.compat;

import com.alrexu.parcool.compat.extern.ExternalModManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ParCoolCompatAddon.MOD_ID)
public class ParCoolCompatAddon {
    public static final String MOD_ID = "parcool_compat_addon";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final String EMF_COMPAT_CLASS = "com.alrexu.parcool.compat.client.EntityModelFeaturesCompat";

    public ParCoolCompatAddon(IEventBus modBus) {
        var forgeBus = NeoForge.EVENT_BUS;
        ExternalModManager.init(modBus, forgeBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(ParCoolCompatAddon::onClientSetup);
        }
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ParCoolCompatAddon::registerClientOnlyCompat);
    }

    private static void registerClientOnlyCompat() {
        if (ParCoolVersion.current() != ParCoolVersion.V3) {
            return;
        }
        try {
            Class<?> compat = Class.forName(EMF_COMPAT_CLASS);
            compat.getMethod("register").invoke(null);
        } catch (Throwable t) {
            LOGGER.warn("Failed to load client-only compatibility layer", t);
        }
    }
}
