package com.alrexu.parcool.compat.client;

import com.alrex.parcool.common.action.impl.BreakfallReady;
import com.alrex.parcool.common.action.impl.CatLeap;
import com.alrex.parcool.common.action.impl.ChargeJump;
import com.alrex.parcool.common.action.impl.ClimbPoles;
import com.alrex.parcool.common.action.impl.ClimbUp;
import com.alrex.parcool.common.action.impl.ClingToCliff;
import com.alrex.parcool.common.action.impl.Crawl;
import com.alrex.parcool.common.action.impl.Dive;
import com.alrex.parcool.common.action.impl.Dodge;
import com.alrex.parcool.common.action.impl.Flipping;
import com.alrex.parcool.common.action.impl.HangDown;
import com.alrex.parcool.common.action.impl.HideInBlock;
import com.alrex.parcool.common.action.impl.HorizontalWallRun;
import com.alrex.parcool.common.action.impl.JumpFromBar;
import com.alrex.parcool.common.action.impl.QuickTurn;
import com.alrex.parcool.common.action.impl.RideZipline;
import com.alrex.parcool.common.action.impl.SkyDive;
import com.alrex.parcool.common.action.impl.Slide;
import com.alrex.parcool.common.action.impl.Tap;
import com.alrex.parcool.common.action.impl.Vault;
import com.alrex.parcool.common.action.impl.VerticalWallRun;
import com.alrex.parcool.common.action.impl.WallJump;
import com.alrex.parcool.common.action.impl.WallSlide;
import com.alrex.parcool.common.attachment.common.Parkourability;
import com.alrexu.parcool.compat.ParCoolCompatAddon;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Registers EMF's {@code registerVanillaModelCondition} via reflection so the dedicated server never loads EMF classes
 * (Entity Model Features is client-only). When ParCool parkour moves are active, EMF uses vanilla model variant 0 so
 * arm/body poses from ParCool render correctly with CEM resource packs.
 */
public final class EntityModelFeaturesCompat {
    private EntityModelFeaturesCompat() {
    }

    /**
     * Called from {@link com.alrexu.parcool.compat.ParCoolCompatAddon} on the mod bus on physical client only.
     */
    public static void register() {
        if (!ModList.get().isLoaded("entity_model_features")) {
            return;
        }
        try {
            Class<?> api = Class.forName("traben.entity_model_features.EMFAnimationApi");
            Method register = api.getMethod("registerVanillaModelCondition", Function.class);
            Function<Object, Boolean> condition = EntityModelFeaturesCompat::parcoolForcesVanillaPlayerModel;
            register.invoke(null, condition);
            ParCoolCompatAddon.LOGGER.info("Registered ParCool + Entity Model Features vanilla-model condition");
        } catch (Throwable t) {
            ParCoolCompatAddon.LOGGER.warn("Could not register Entity Model Features compatibility (wrong EMF version?)", t);
        }
    }

    /**
     * Argument is EMF's {@code EMFEntity} at runtime; kept as Object so this class loads without EMF on the classpath.
     */
    private static boolean parcoolForcesVanillaPlayerModel(Object emfEntity) {
        if (!(emfEntity instanceof Player player)) {
            return false;
        }
        Parkourability parkourability = Parkourability.get(player);
        return parkourability.isDoingAny(
                ClingToCliff.class,
                HangDown.class,
                ClimbUp.class,
                HorizontalWallRun.class,
                VerticalWallRun.class,
                Vault.class,
                CatLeap.class,
                WallJump.class,
                Slide.class,
                Crawl.class,
                Dive.class,
                SkyDive.class,
                JumpFromBar.class,
                RideZipline.class,
                WallSlide.class,
                ClimbPoles.class,
                Dodge.class,
                Flipping.class,
                QuickTurn.class,
                Tap.class,
                ChargeJump.class,
                HideInBlock.class,
                BreakfallReady.class
        );
    }
}
