# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- The Build workflow now triggers on `master`, the repository's only branch. It was previously scoped to `main` and `develop`, neither of which exists, so it never ran.
- `CONTRIBUTING.md` no longer instructs contributors to branch from and target a `develop` branch that does not exist.
- `README.md` and `CONTRIBUTING.md` no longer present `mvn clean test` as a passing test suite; the project has no `src/test/` directory and no test framework, so the command executes zero tests.
- `USER_GUIDE.md` and `COMMANDS.md` no longer suggest reloading the plugin to apply configuration changes; no commands are registered, so a server restart is the only way.
- `CONFIG.md` and `config.yml` now state that `default-color.fill-color` and `default-color.line-color` are not read by the plugin.

### Added

- Documented the build prerequisite in `README.md` and `CONTRIBUTING.md`: the gitignored `libs/medieval-factions-5.6.0-all.jar` must be supplied out of band before any Maven goal can resolve dependencies.

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
