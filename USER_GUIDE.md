# User Guide

## Prerequisites

Before using Bluemap_MedievalFactions, ensure the following plugins are installed and running on your server:

- **Medieval Factions** – manages factions and land claims.
- **BlueMap** – renders the interactive web map.

Both plugins must be enabled before Bluemap_MedievalFactions loads. The plugin declares them as hard dependencies in `plugin.yml` and will disable itself if either is missing.

## First Steps

After placing the jar in your `plugins/` folder and restarting the server:

1. Open `plugins/MF_Bluemap/config.yml` to review the default settings.
2. Visit your BlueMap web interface (default: `http://<your-server-ip>:8100`).
3. You should see a **Medieval Factions Claims** marker layer available in the layer controls.
4. Claimed chunks will appear as coloured polygons on the map. Contiguous chunks belonging to the same faction are automatically merged into a single shape.

## Common Scenarios

### Viewing Faction Claims

Once the plugin is running, no action is needed — claims are synced automatically on startup and updated in real time whenever a faction claims or unclaims a chunk.

### Customising Faction Colours

By default each faction is drawn in its own colour flag from Medieval Factions, so the web map matches the colour players already see in chat and on territory titles. A faction whose flag is unset — or is still the literal `random` placeholder — falls back to a colour derived from its internal ID, which is stable across restarts. To override the colour for a specific faction, add an entry under `factions:` in `config.yml`:

```yaml
factions:
  MyFaction:
    fillColor: "#FF0000"
    fillOpacity: 0.35
    lineColor: "#FF0000"
    lineOpacity: 1.0
```

To give every faction without such an override one shared colour instead — a plain
"claimed land" overlay rather than a per-faction one — set `default-color.mode` to
`fixed`:

```yaml
default-color:
  mode: fixed
  fill-color: "#AAAAAA"
  line-color: "#0055FF"
```

Restart the server for changes to take effect. The plugin registers no commands, so
there is no in-game way to reload its configuration.

### Changing the Overlay Y-Level

The Y-level at which overlays are rendered can be adjusted in `config.yml`:

```yaml
bluemap:
  y-level: 70.0
```

### Changing the Claim Label Format

The label shown on each claim marker can be customised with the `label-format` option. Use `%faction%` as a placeholder for the faction name:

```yaml
bluemap:
  label-format: "Faction: %faction%"
```

## Permissions

This plugin does not register any permission nodes. All functionality is server-side and automatic.
