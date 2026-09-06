# Contributing

## Development

This branch is built on [Stonecutter](https://stonecutter.kikugie.dev/), which lets one codebase target
multiple Minecraft versions and loaders. Version/loader-specific code is written with Stonecutter comments:

```java
//? fabric {
fabricOnlyCode();
//?} else {
/*neoforgeOnlyCode();*/
//?}
```

```java
//? 1.21.7 {
LOGGER.info("hello 1.21.7!");
//?} else {
/*LOGGER.info("hello from any other version!");
 *///?}
```

For more, read the [Stonecutter documentation](https://stonecutter.kikugie.dev/wiki/).

### Configuration

Mod metadata and per-version/per-loader dependencies live in `stonecutter.properties.toml`
(e.g. `[fabric."1.21.7"]`).

### Access Wideners/Transformers

* Fabric Access Wideners: `src/main/resources/aw/*.accesswidener` (one per supported Minecraft version)
* (Neo)Forge Access Transformers: `src/main/resources/aw/*.cfg` (one per supported Minecraft version)

### Running in Development

The Gradle plugins of the respective platform provide run configurations. Be careful to run the correct task
for the Stonecutter version/loader you're targeting, e.g.:

```bash
./gradlew :1.21.7-fabric:runClient
```

### Using the CI

**`build.yml`** runs on every push and pull request - builds all versions and uploads the jars as artifacts.

**`release.yml`** runs when a tag is pushed - validates the tag against `mod.version` + `mod.channel_tag`,
builds all versions, generates a changelog via [`git-cliff`](https://git-cliff.org/), and publishes to whichever
platforms are enabled via the repository's Actions secrets/variables.

## Adding a new Minecraft version

Checklist for wiring up a new Fabric/NeoForge/Forge version anchor.

### 1. Add the toml block

In `stonecutter.properties.toml`, add a section for the new version, e.g.:

```toml
[fabric."1.22.0"]
deps.minecraft = "1.22.0"
deps.fabric-api = "..."
deps.modmenu = "..."
```

Look up the exact `fabric-api`/`modmenu` version via Modrinth's version API:

```
https://api.modrinth.com/v2/project/fabric-api/version?game_versions=%5B%221.22.0%22%5D&loaders=%5B%22fabric%22%5D
```

Pick the latest result. Same pattern for `modmenu`, and for `neoforge`/`forge`
via `deps.forge`/`deps.neoforge` on the loader-specific tables below.

### 2. WTHIT

Check whether WTHIT even publishes a release for this version:

```
https://api.modrinth.com/v2/project/wthit/version?game_versions=%5B%221.22.0%22%5D&loaders=%5B%22fabric%22%5D
https://api.modrinth.com/v2/project/badpackets/version?game_versions=%5B%221.22.0%22%5D&loaders=%5B%22fabric%22%5D
```

If a WTHIT release exists, download it and check which plugin API generation
it actually ships (don't assume based on release date or version number):

```bash
curl -sL -o wthit.jar "<download url from the version API's files[].url>"
unzip -l wthit.jar | grep -E "IWailaCommonPlugin|ICommonRegistrar|IWailaPlugin\.class"
```

- If `IWailaCommonPlugin`/`ICommonRegistrar`/`IClientRegistrar` are present:
  modern generation. Set `deps.wthit`/`deps.badpackets` normally - no other
  changes needed, the existing `wthit_plugin && >=1.21.2` (or the matching
  version-range branch) in `MarkedGoatsWailaPlugin.java` already covers it.
- If only `IWailaPlugin.class` is present: legacy generation. Set
  `deps.wthit-legacy-api = true` too. Then check the plugin-discovery
  mechanism - extract and check the mod's own base `PluginLoader.class`
  (not `FabricPluginLoader.class` - that one references the string
  `"waila:plugins"` in every generation as a fallback, it isn't a reliable
  signal on its own):

  ```bash
  unzip -p wthit.jar mcp/mobius/waila/plugin/PluginLoader.class > PluginLoader.class
  javap -p -c PluginLoader.class | grep -i "waila_plugins.json"
  ```

  - Prints a match: the file-based mechanism exists, no extra toml flag
    needed (the existing `generateWailaPluginsJson` task in
    `build.fabric-o.gradle.kts` already picks the right schema based on
    `deps.wthit-legacy-api`).
  - No match: only the `fabric.mod.json` custom-value mechanism exists.
    Also set `deps.wthit-legacy-fabric-mod-json = true`.

  Either way, if the target version predates goat horns (pre-1.19, i.e. it's
  `<1.19` in the `wthit_plugin_legacy` branches), the legacy Java branch
  needs the pre-1.19 variant (no icon, `TranslatableComponent`/
  `TextComponent` constructors instead of `Component.translatable()`/
  `.literal()`) rather than the `>=1.19` one - see the existing
  `wthit_plugin_legacy && <1.19` block in `MarkedGoatsWailaPlugin.java` for
  the pattern.

If no WTHIT release exists for the version at all, or the version predates
BadPackets *and* the only WTHIT release available for it also requires
BadPackets: leave WTHIT unwired and note the reason in the toml comment.

### 3. Jade (optional)

Only wired for `>=1.21.2` so far - not required for a new version, but if you
want to extend it:

1. Check whether Jade even publishes a release for this version:

   ```
   https://api.modrinth.com/v2/project/jade/version?game_versions=%5B%221.22.0%22%5D&loaders=%5B%22fabric%22%5D
   ```

2. Set `deps.jade` to that version's exact `version_number` (including the
   `+fabric` suffix, e.g. `"19.3.2+fabric"`) - that's the real Modrinth Maven
   coordinate under `maven.modrinth:jade:...`, already wired in
   `build.fabric-o.gradle.kts`/`build.fabric-m.gradle.kts` and the `"jade"`
   fabric.mod.json entrypoint in `Loader.kt`, both gated on this property.
3. Jade's plugin API (`snownee.jade.api`) has been stable in shape across
   every `>=1.21.2` release checked so far - only `ResourceLocation` vs
   `Identifier` changes, and that's already handled by the same
   `current.parsed >= "1.21.11"` string replacement used everywhere else in
   this codebase. Don't assume this holds indefinitely - if a new version's
   API differs, verify against the real tagged source
   (`https://github.com/Snownee/Jade/tree/fabric-<version>/src/main/java/snownee/jade/api`)
   rather than guessing from an older version or from `main`.
4. `MarkedGoatsJadePlugin.java` is gated `jade_plugin && >=1.21.2` - no known
   Jade support exists in this codebase below that yet (API shape differs
   there and hasn't been checked).

### 4. fabric-api mod id

If the fabric-api version you're pinning is old enough it might still use
the pre-rename `"fabric"` id instead of `"fabric-api"`. Check the jar's own
manifest:

```bash
unzip -p fabric-api.jar fabric.mod.json | grep '"id"'
```

If it says `"id": "fabric"` (no `"provides": ["fabric-api"]`), set
`deps.fabric-api-id = "fabric"` in the toml block too.

### 5. Verify - don't trust a clean compile

Compiling clean is not proof anything actually works. Every real bug found
this project (a silent WTHIT registration failure, a raw-source caching
issue, a race between overlapping launches) still compiled fine.

1. Compile in isolation: point `.sc_active_version` at a *different*
   project first, then `./gradlew :X-fabric:compileJava`. Never leave it
   pointed at the version you're about to compile - Gradle sometimes
   compiles the raw, un-chiseled `src/main/java` directly instead of the
   per-version generated copy when they match.
2. Launch for real, alone: `./gradlew :X-fabric:runClient`, waited for full
   completion before touching `.sc_active_version` or starting anything
   else. Two overlapping launches can race on that shared file and produce
   a build failure that has nothing to do with your actual change.
3. If WTHIT is wired, check the log for `Registering plugin
   markedgoats:plugin` (or `Initializing plugin`/`Initializing common/client
   plugin` depending on the WTHIT generation) with no `ClassCastException`
   or `Error creating instance`. A clean launch with no error is not enough
   - WTHIT can silently fail to register a plugin and fall back to its own
   generic display, which looks identical to "working" at a glance.
4. If Jade is wired, confirm `MarkedGoatsJadePlugin` actually loaded - Jade
   logs registered plugins at startup, and the mod's tooltip lines/icon are
   easy to confuse with Jade's own generic entity display if registration
   silently failed.
5. Look at an actual goat in-game and confirm the title, icon, and
   instrument name.

### 6. Gametest (optional)

Only wired for `1.21.7` so far - not required for a new version, but if you
want to extend it:

1. Confirm `fabric-gametest-api-v1` actually has a build for the fabric-api
   version you're pinning - check that version's own POM, not just the
   umbrella jar's contents (it's compile-scope only, not embedded in the
   jar-in-jar):

   ```
   https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/<deps.fabric-api>/fabric-api-<deps.fabric-api>.pom
   ```

   Look for a `fabric-gametest-api-v1` `<dependency>` entry and note its
   version.
2. Add `deps.gametest = true` to the version's toml block. This alone wires
   the dependency, the `runGameTest`/`runClientGameTest` run configs, and
   the `"fabric-gametest"` entrypoint (all gated in
   `build.fabric-o.gradle.kts`/`Loader.kt` on this same property) - no
   other file needs touching for a straightforward Fabric anchor.
3. `GoatVariantGameTests.java` is gated behind `//? if gametest { ... }`
   and only has real content when the property is set anywhere - so it's
   safe to leave as-is; it'll pick up the new version automatically.
4. Verify like anything else here: `./gradlew :X-fabric:runGameTest`, read
   the actual log line ("All N required tests passed"), don't just trust a
   zero exit code.
