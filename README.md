# ParCool+ / Compatibility Addon (NeoForge 1.21.1 fork)

Addon for **[ParCool](https://www.curseforge.com/minecraft/mc-mods/parcool)** that smooths animation and gameplay interactions with a small set of third-party mods on **Minecraft 1.21.1** and **NeoForge**.

This tree is a **fork** of the original compatibility addon, ported to NeoForge and trimmed to the integrations that matter for 1.21.1. Upstream mod and idea: ParCool / alRex_U.

---

## Requirements

| Mod        | Role |
|-----------|------|
| **ParCool** | Required. Tested around `3.4.3.1` for MC 1.21.1 (NeoForge build). |
| **NeoForge** | Required. Pinned in `gradle.properties` (e.g. `21.1.222`). |

Optional compat layers load only if the corresponding mod is present:

| Optional mod        | What this addon does |
|--------------------|----------------------|
| **Better Combat**  | Reduces animation clashes with ParCool’s parkour moves. |
| **Carry On**       | Stops ParCool actions while you are carrying blocks or entities. |
| **Player Animator**| Lets Player Animator win where it conflicts with some ParCool animations. |

**Paraglider** and similar cases are handled inside **ParCool** itself; this addon does not add a separate Paraglider module.

---

## Building from source

1. **JDK 21** (matches the Gradle toolchain).

2. Place these JARs under **`libs/`** (file names must match `gradle.properties`):

   - ParCool NeoForge jar for 1.21.1  
   - Better Combat, Carry On, Player Animator (NeoForge / Forge API jars as referenced in `gradle.properties`)  
   - **Cloth Config** (Better Combat depends on it at runtime)

   The exact filenames are defined in `gradle.properties` (e.g. `parcool_jar`, `bettercombat_jar`, …).

3. Build:

   ```bash
   ./gradlew.bat build
   ```

   On Linux/macOS:

   ```bash
   ./gradlew build
   ```

   The mod jar is produced under `build/libs/`.

4. **Dev client** (after `libs/` is populated):

   ```bash
   ./gradlew.bat runClient
   ```

   `neoforge.mods.toml` gets its `version` from Gradle (`mod_version` in `gradle.properties`), so the dev classpath (no shaded JAR manifest) still loads correctly.

---

## Project layout (high level)

- `src/main/java` — addon entrypoint and compat managers for Better Combat, Carry On, and Player Animator.  
- `src/main/resources/META-INF/neoforge.mods.toml` — mod metadata and optional dependencies.  
- `build.gradle` — ModDevGradle, local `libs/` dependencies, resource filtering for the TOML.

---

## Versioning

- **Game / loader**: see `minecraft_version`, `neo_version` in `gradle.properties`.  
- **Addon semver**: `mod_version` in `gradle.properties` (also written into `neoforge.mods.toml` at build time).

---

## License

GNU Lesser General Public License v3 — see `LICENSE` and the license line in `neoforge.mods.toml`.
