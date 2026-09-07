# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- An automated test suite. JUnit 5 runs under Maven Surefire, and `mvn clean test` now executes tests covering claim-chunk union geometry — including merging, disjoint regions and enclave holes — and colour parsing and fallback derivation.

### Changed

- The claim-merging geometry and the colour parsing and fallback derivation moved out of `BlueMapIntegration` into `ClaimGeometry` and `FactionColors`. Both are free of Bukkit, BlueMap and Medieval Factions types, so they can be tested without a running server; `BlueMapIntegration` itself could not be, because its constructor registers Bukkit event listeners. Behaviour is unchanged.
- Geometry failures are logged through `Logger.log(Level.SEVERE, ...)` with the throwable attached, rather than `printStackTrace()`, so the stack trace reaches the server log through the plugin's logger.

### Fixed

- `CONFIG.md`, `USER_GUIDE.md` and `config.yml` no longer state that an unconfigured faction is always coloured from a hash of its ID. The faction's own colour flag in Medieval Factions takes precedence; the ID hash is only the fallback.
- `README.md` and `CONTRIBUTING.md` no longer state that the project has no test suite.

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
