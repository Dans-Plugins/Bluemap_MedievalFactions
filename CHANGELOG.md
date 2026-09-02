# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- Land claims are no longer drawn on every map. A marker set is now kept per BlueMap map, and each world's claims are placed only on the maps that render that world. Previously a single shared marker set was registered on every map, so Overworld claims were drawn over the Nether at unscaled coordinates — eight times out of place, given the 1:8 coordinate ratio. A world BlueMap does not render is now skipped rather than drawn somewhere arbitrary.

### Changed

- Faction territory is coloured from the faction's own colour flag in Medieval Factions, so the web map matches the colour already shown in chat and on territory titles. The previous faction-ID hash is retained as a fallback for when the flag is unset or still holds the literal `random` placeholder.
- The build targets Medieval Factions 6.0.0. The API survived the major version: `MfFactionId` is a Kotlin value class that erases to `String`, so the existing Java call sites still resolve.

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
