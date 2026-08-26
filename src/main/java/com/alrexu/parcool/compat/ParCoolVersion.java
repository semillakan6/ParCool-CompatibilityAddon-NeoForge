package com.alrexu.parcool.compat;

import net.neoforged.fml.ModList;

/**
 * The ParCool API generation available at runtime.
 */
public enum ParCoolVersion {
    V3,
    V4_OR_NEWER,
    UNKNOWN;

    private static final String PARCOOL_MOD_ID = "parcool";

    public static ParCoolVersion current() {
        return CurrentHolder.CURRENT;
    }

    private static ParCoolVersion detect() {
        var modInfo = ModList.get().getMods().stream()
                .filter(info -> PARCOOL_MOD_ID.equals(info.getModId()))
                .findFirst();
        if (modInfo.isEmpty()) {
            ParCoolCompatAddon.LOGGER.error("Could not detect the loaded ParCool version; version-specific compatibility is disabled");
            return UNKNOWN;
        }

        var version = modInfo.get().getVersion();
        int major = resolveMajor(version.toString(), version.getMajorVersion());
        if (major == 3) {
            return V3;
        }
        if (major >= 4) {
            return V4_OR_NEWER;
        }

        ParCoolCompatAddon.LOGGER.error(
                "Unsupported ParCool version {}; version-specific compatibility is disabled",
                version
        );
        return UNKNOWN;
    }

    /**
     * ArtifactVersion sometimes reports major {@code 0} for four-part versions like {@code 3.4.3.3}.
     * Fall back to the first numeric component of the string form.
     */
    private static int resolveMajor(String versionString, int reportedMajor) {
        if (reportedMajor > 0) {
            return reportedMajor;
        }
        int end = versionString.indexOf('.');
        String head = end >= 0 ? versionString.substring(0, end) : versionString;
        try {
            return Integer.parseInt(head);
        } catch (NumberFormatException ignored) {
            return reportedMajor;
        }
    }

    private static final class CurrentHolder {
        private static final ParCoolVersion CURRENT = detect();

        private CurrentHolder() {
        }
    }
}
