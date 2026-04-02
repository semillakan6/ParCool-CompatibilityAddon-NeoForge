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

2. **Compile dependencies** (ParCool, Better Combat, Carry On, Player Animator, Cloth Config):

   - **Easiest:** leave `libs/` empty (or omit jars you do not care to pin). Gradle pulls matching versions from [Modrinth Maven](https://api.modrinth.com/maven) using the `*_modrinth_version` entries in `gradle.properties`.
   - **Pinned jars:** copy release JARs into **`libs/`** using the exact filenames from `gradle.properties` (e.g. `parcool_jar`, `bettercombat_jar`, …). A file present in `libs/` wins over Modrinth for that mod.
   - **Build deps from source:** upstream branches for this stack:

     | Mod | Source repository |
     |-----|-------------------|
     | ParCool (NeoForge) | [alRex-U/ParCool `1.21.1-NF`](https://github.com/alRex-U/ParCool/tree/1.21.1-NF) |
     | Better Combat | [ZsoltMolnarrr/BetterCombat `1.21.1`](https://github.com/ZsoltMolnarrr/BetterCombat/tree/1.21.1) (NeoForge subproject; Gradle setup in the [Better Combat readme](https://github.com/ZsoltMolnarrr/BetterCombat/tree/1.21.1)) |
     | Carry On | [Tschipp/CarryOn `1.21.1`](https://github.com/Tschipp/CarryOn/tree/1.21.1) |
     | Player Animator | [KosmX/minecraftPlayerAnimator `1.21`](https://github.com/KosmX/minecraftPlayerAnimator/tree/1.21) ([KosmX Maven](https://maven.kosmx.dev/) is also documented there) |

     Build each mod’s NeoForge (or Forge API) JAR, then drop it into `libs/` with the name expected in `gradle.properties`.

3. Build:

   ```bash
   ./gradlew.bat build
   ```

   On Linux/macOS:

   ```bash
   ./gradlew build
   ```

   The mod jar is produced under `build/libs/`.

4. **Dev client:**

   ```bash
   ./gradlew.bat runClient
   ```

   Runtime mods are taken from the same `libs/` vs Modrinth resolution as compile. `neoforge.mods.toml` gets its `version` from Gradle (`mod_version` in `gradle.properties`).

---

## Project layout (high level)

- `src/main/java` — addon entrypoint and compat managers for Better Combat, Carry On, and Player Animator.  
- `src/main/resources/META-INF/neoforge.mods.toml` — mod metadata and optional dependencies.  
- `build.gradle` — ModDevGradle, `libs/` with Modrinth fallback, resource filtering for the TOML.

---

## Versioning

- **Game / loader**: see `minecraft_version`, `neo_version` in `gradle.properties`.  
- **Addon semver**: `mod_version` in `gradle.properties` (also written into `neoforge.mods.toml` at build time).

---

## License

GNU Lesser General Public License v3 — see `LICENSE` and the license line in `neoforge.mods.toml`.
