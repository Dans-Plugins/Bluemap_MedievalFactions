# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [1.1.0] – 2026-10-05

### Added

- A rolling `dev` prerelease of `master`, rebuilt on every merge by `dev-release.yml`, which Dan's Plugin Manager installs with `/dpm get bluemapmedievalfactions --experimental`. Stable releases are now made from it after release checks.
- `minecraft-versions.json`, declaring 26.2 and 26.3 as the supported Minecraft versions; every build checks the plugin's Bukkit API use against each of them.
- `default-color.mode`, which decides how a faction with no `factions:` override is coloured. The default `auto` keeps the current behaviour — the faction's own colour flag in Medieval Factions, falling back to a colour derived from its ID — while `fixed` paints all such factions in `default-color.fill-color` and `default-color.line-color`. Any unrecognised value is treated as `auto`.
- An automated test suite. JUnit 5 runs under Maven Surefire, and `mvn clean test` now executes tests covering claim-chunk union geometry — including merging, disjoint regions and enclave holes — and colour parsing and fallback derivation.

### Changed

- The claim-merging geometry and the colour parsing and fallback derivation moved out of `BlueMapIntegration` into `ClaimGeometry` and `FactionColors`. Both are free of Bukkit, BlueMap and Medieval Factions types, so they can be tested without a running server; `BlueMapIntegration` itself could not be, because its constructor registers Bukkit event listeners. Behaviour is unchanged.
- Geometry failures are logged through `Logger.log(Level.SEVERE, ...)` with the throwable attached, rather than `printStackTrace()`, so the stack trace reaches the server log through the plugin's logger.

### Fixed

- A disbanded faction's territory is removed from the map. Disbanding does not fire `FactionUnclaimEvent`, so its markers stayed until the next server restart; the plugin now also listens for `FactionDisbandEvent`.

- The map no longer lags one claim behind. A faction's claims were read when Medieval Factions fired `FactionClaimEvent` or `FactionUnclaimEvent`, which is before Medieval Factions saves the change, so the newest claim (or unclaim) only appeared after the next one or a `bluemap reload`. The claims are now read when the 1.5-second debounce fires.

- `default-color.fill-color` and `default-color.line-color` now take effect. Both were shipped in `config.yml` and documented, but no code path read either one; they are now read whenever `default-color.mode` is `fixed`. A value that is not a hex colour logs a warning and falls back to per-faction colours rather than leaving the territory undrawn.
- `CONFIG.md`, `USER_GUIDE.md` and `config.yml` no longer state that an unconfigured faction is always coloured from a hash of its ID. The faction's own colour flag in Medieval Factions takes precedence; the ID hash is only the fallback.
- `README.md` and `CONTRIBUTING.md` no longer state that the project has no test suite.
- `CONFIG.md` now documents the `factions:` override sub-keys' defaults, that faction names match case-sensitively, that three-digit hex shorthand is not expanded, and that an entry with a non-hex colour is ignored with a warning.
- The `ClaimGeometry.unionChunks` Javadoc no longer states that chunks touching only at a corner merge into one polygon, and `USER_GUIDE.md` now says so explicitly. Such chunks stay separate shapes, as the test suite confirms.
- A faction whose Medieval Factions colour flag is still the literal `random` placeholder no longer logs a "Could not read colour flag" warning each time its claims are redrawn. The value was treated as an error because it is six characters long; it now falls back to the ID-derived colour silently, as an unset flag does. A flag value with a leading `+` or `-` sign is also no longer accepted as a colour.
- `plugin.yml` now takes its `version` from `pom.xml` through Maven resource filtering, so the version a server reports for the plugin always matches the released artifact. It was hard-coded to `1.0` and was not bumped alongside the `1.0.0` release. Only `plugin.yml` is filtered; the bundled `config.yml` is still copied verbatim.

### Removed

- `dependency-reduced-pom.xml` is no longer tracked and is now gitignored. It is maven-shade-plugin output that every `mvn package` regenerates, and the committed copy had drifted from `pom.xml` (it still named version `1.0-SNAPSHOT` and lacked the test dependencies).

## [1.0.0] – 2026-09-04

### Fixed

- Land claims are no longer drawn on every map. A marker set is now kept per BlueMap map, and each world's claims are placed only on the maps that render that world. Previously a single shared marker set was registered on every map, so Overworld claims were drawn over the Nether at unscaled coordinates — eight times out of place, given the 1:8 coordinate ratio. A world BlueMap does not render is now skipped rather than drawn somewhere arbitrary.
- The build is reproducible from a clean checkout. Both workflows now fetch the Medieval Factions jar that `pom.xml` resolves by path from the gitignored `libs/`, and build on JDK 21 — `bluemap-api` and `paper-api` ship Java 21 class files, which a Java 17 compiler rejects with `bad class file`. Neither had ever succeeded in CI.
- The Build workflow now triggers on `master`, the repository's only branch. It was previously scoped to `main` and `develop`, neither of which exists, so it never ran.
- `CONTRIBUTING.md` no longer instructs contributors to branch from and target a `develop` branch that does not exist.
- `README.md` and `CONTRIBUTING.md` no longer present `mvn clean test` as a passing test suite; the project has no `src/test/` directory and no test framework, so the command executes zero tests.
- `USER_GUIDE.md` and `COMMANDS.md` no longer suggest reloading the plugin to apply configuration changes; no commands are registered, so a server restart is the only way.
- `CONFIG.md` and `config.yml` now state that `default-color.fill-color` and `default-color.line-color` are not read by the plugin.

### Changed

- Faction territory is coloured from the faction's own colour flag in Medieval Factions, so the web map matches the colour already shown in chat and on territory titles. The previous faction-ID hash is retained as a fallback for when the flag is unset or still holds the literal `random` placeholder.
- The build targets Medieval Factions 6.0.0. The API survived the major version: `MfFactionId` is a Kotlin value class that erases to `String`, so the existing Java call sites still resolve.

### Added

- Documented the build prerequisite in `README.md` and `CONTRIBUTING.md`: the gitignored `libs/medieval-factions-6.0.0-live-all.jar` must be supplied before any Maven goal can resolve dependencies, and the JDK 21 requirement.

## [1.0] – Initial Release

### Added

- BlueMap integration that renders Medieval Factions land claims as shape overlays on the web map.
- Automatic synchronisation of all faction claims on server startup using a batched schedule to avoid lag spikes.
- Real-time marker updates triggered by `FactionClaimEvent` and `FactionUnclaimEvent`.
- Contiguous claimed chunks belonging to the same faction are merged into unified polygons using JTS geometry operations, including support for holes (enclaves).
- Deterministic per-faction colour assignment based on faction ID hash so each faction consistently receives a unique colour without manual configuration.
- Per-faction colour overrides configurable in `config.yml` under the `factions:` section.
- Configurable Y-level for overlay rendering (`bluemap.y-level`).
- Configurable claim label format with `%faction%` placeholder (`bluemap.label-format`).
- Default fill/line colour and opacity settings (`default-color.*`).
- Debounced recompute scheduling (1500 ms) to batch rapid claim changes into a single geometry update.
