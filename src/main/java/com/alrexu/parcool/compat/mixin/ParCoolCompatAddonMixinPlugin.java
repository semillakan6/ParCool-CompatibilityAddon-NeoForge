package com.alrexu.parcool.compat.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ParCoolCompatAddonMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String SABLE_MIXIN =
            "com.alrexu.parcool.compat.mixin.sable.WorldUtilSableCollisionMixin";
    private static final String SABLE_CLIFF_JUMP_MIXIN =
            "com.alrexu.parcool.compat.mixin.sable.CliffJumpSableMixin";
    private static final String SABLE_CLING_RELEASE_MIXIN =
            "com.alrexu.parcool.compat.mixin.sable.ClingReleaseSableMixin";
    private static final String PLAYER_MODEL_COMPOSITION_MIXIN =
            "com.alrexu.parcool.compat.mixin.client.PlayerModelAnimationCompositionMixin";
    private static final String ANIMATION_APPLIER_COMPOSITION_MIXIN =
            "com.alrexu.parcool.compat.mixin.client.AnimationApplierCompositionMixin";
    private static final Set<String> EMF_MIXINS = Set.of(
            "com.alrexu.parcool.compat.mixin.client.PlayerRendererFirstPersonStateMixin",
            "com.alrexu.parcool.compat.mixin.client.LivingEntityRendererPlayerBaseStateMixin",
            "com.alrexu.parcool.compat.mixin.client.PlayerModelEmfCompatMixin"
    );

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (SABLE_MIXIN.equals(mixinClassName)
                || SABLE_CLIFF_JUMP_MIXIN.equals(mixinClassName)
                || SABLE_CLING_RELEASE_MIXIN.equals(mixinClassName)) {
            boolean sableLoaded = LoadingModList.get().getModFileById("sable") != null;
            boolean parcool3Loaded = isParCool3Loaded();
            boolean enabled = sableLoaded && parcool3Loaded;
            LOGGER.info(
                    "{} ParCool 3 + Sable mixin {} (Sable loaded: {}, ParCool 3 loaded: {})",
                    enabled ? "Enabling" : "Skipping",
                    mixinClassName,
                    sableLoaded,
                    parcool3Loaded
            );
            return enabled;
        }
        if (PLAYER_MODEL_COMPOSITION_MIXIN.equals(mixinClassName)
                || ANIMATION_APPLIER_COMPOSITION_MIXIN.equals(mixinClassName)) {
            boolean playerAnimatorLoaded = LoadingModList.get().getModFileById("playeranimator") != null;
            boolean parcool3Loaded = isParCool3Loaded();
            boolean enabled = playerAnimatorLoaded && parcool3Loaded;
            LOGGER.info(
                    "{} ParCool 3 + Player Animator composition mixin {}",
                    enabled ? "Enabling" : "Skipping",
                    mixinClassName
            );
            return enabled;
        }
        if (EMF_MIXINS.contains(mixinClassName)) {
            boolean emfLoaded = LoadingModList.get().getModFileById("entity_model_features") != null;
            boolean parcool3Loaded = isParCool3Loaded();
            boolean enabled = emfLoaded && parcool3Loaded;
            LOGGER.info(
                    "{} ParCool 3 + EMF render-scope mixin {}",
                    enabled ? "Enabling" : "Skipping",
                    mixinClassName
            );
            return enabled;
        }
        return true;
    }

    private static boolean isParCool3Loaded() {
        return LoadingModList.get().getMods().stream()
                .filter(info -> "parcool".equals(info.getModId()))
                .map(info -> info.getVersion().toString())
                .map(ParCoolCompatAddonMixinPlugin::parseMajorVersion)
                .anyMatch(major -> major == 3);
    }

    private static int parseMajorVersion(String version) {
        int separator = version.indexOf('.');
        String major = separator >= 0 ? version.substring(0, separator) : version;
        try {
            return Integer.parseInt(major);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
