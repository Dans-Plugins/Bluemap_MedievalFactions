# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

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
