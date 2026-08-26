# ParCool+ / Compatibility Addon (NeoForge 1.21.1 fork)

Addon for **[ParCool](https://www.curseforge.com/minecraft/mc-mods/parcool)** that smooths animation and gameplay interactions with a small set of third-party mods on **Minecraft 1.21.1** and **NeoForge**.

This tree is a **fork** of the original compatibility addon, ported to NeoForge and trimmed to the integrations that matter for 1.21.1. Upstream mod and idea: ParCool / alRex_U.

---

## Requirements

| Mod        | Role |
|-----------|------|
| **ParCool** | Required. Supported integration target: `3.4.3.3` for MC 1.21.1. ParCool 4.x is load-only until its API is stable. |
| **NeoForge** | Required. Pinned in `gradle.properties` (e.g. `21.1.248`; Sable needs at least `21.1.228`). |

Optional compat layers load only if the corresponding mod is present:

| Optional mod        | What this addon does |
|--------------------|----------------------|
| **Better Combat**  | Composes Better Combat’s Player Animator transforms after ParCool instead of cancelling either animation, keeping attacks, held items, and skin layers aligned in first and third person. |
| **Carry On**       | Stops ParCool actions while you are carrying blocks or entities. |
| **Player Animator**| Defers active model-part transforms until ParCool finishes its pose, then applies the data-driven animation exactly once and resynchronizes every outer skin layer. |
| **ETF + EMF** | [Entity Texture Features](https://github.com/Traben-0/Entity_Texture_Features) + [Entity Model Features](https://github.com/Traben-0/Entity_Model_Features): resource-pack CEM poses yield during ParCool actions or while Player Animator/Better Combat owns the pose, with a short transition grace period. Ordinary grounded, jumping, falling, and flying resource-pack animations remain available. The gameplay animation itself is not cancelled, and vanilla geometry is used consistently for that pass to prevent split skin layers. Tested against EMF **3.0.17** / ETF **7.0.13**. |
| **Sable** | Makes ParCool probes use exact transformed collision shapes from Sable 2.x physics sub-levels, enabling wall jumps, stable wall runs, vaults, cliff clings, and hanging from supported bars. Tested target: Sable **2.0.5** with Sable Companion **1.6.0**. |

On ParCool 4.x, the addon intentionally registers only a placeholder and does not claim Better Combat, Carry On, Player Animator, EMF, or Sable action compatibility yet.

**Paraglider** and similar cases are handled inside **ParCool** itself; this addon does not add a separate Paraglider module.

---

## Building from source

1. **JDK 21** (matches the Gradle toolchain).

2. **Compile dependencies** (ParCool, Better Combat, Carry On, Player Animator, Cloth Config, Sable Companion, plus optional EMF/ETF for IDE support):

   - **Easiest:** leave `libs/` empty (or omit jars you do not care to pin). Gradle pulls matching mod versions from [Modrinth Maven](https://api.modrinth.com/maven). Sable Companion is compile-only and comes from Ryanhcode's Maven; Sable supplies the runtime implementation, so it is not embedded in this addon.
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

- `src/main/java` — addon entrypoint, ParCool-version-gated compat managers, client-only `EntityModelFeaturesCompat`, and the optional Sable collision mixin.
- `src/main/resources/META-INF/neoforge.mods.toml` — mod metadata and optional dependencies.  
- `build.gradle` — ModDevGradle, `libs/` with Modrinth fallback, resource filtering for the TOML.

---

## Versioning

- **Game / loader**: see `minecraft_version`, `neo_version` in `gradle.properties`.  
- **Addon semver**: `mod_version` in `gradle.properties` (also written into `neoforge.mods.toml` at build time).

Current addon release: **1.2**. Sable compatibility covers ParCool wall and ledge collision probes plus the block-state classification used for hanging from supported pillars, end rods, fences, and walls inside a sub-level.

---

## License

GNU Lesser General Public License v3 — see `LICENSE` and the license line in `neoforge.mods.toml`.
