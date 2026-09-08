# Configuration Guide

All options are stored in `plugins/MF_Bluemap/config.yml`. The file is created automatically on first run from the bundled defaults.

---

## bluemap.y-level

**Type:** double
**Default:** `70.0`
**Description:** The Y-coordinate at which claim overlay shapes are rendered on the BlueMap web map. Adjust this if the default level is hidden below terrain on your map.

**Example:**

```yaml
bluemap:
  y-level: 64.0
```

---

## bluemap.label-format

**Type:** string
**Default:** `"Faction: %faction%"`
**Description:** The text label displayed when a user hovers over or clicks a claim shape on the map. Use `%faction%` as a placeholder; it is replaced with the faction's display name at runtime.

**Example:**

```yaml
bluemap:
  label-format: "%faction% Territory"
```

---

## default-color.mode

**Type:** string (`auto` or `fixed`)
**Default:** `auto`
**Description:** How a faction that has no entry under `factions:` is coloured.

| Value   | Behaviour |
|---------|-----------|
| `auto`  | Use the faction's own colour flag in Medieval Factions, falling back to a colour derived from its ID by `FactionColors.generateDeterministicColor` when that flag is unset or is still the literal `random` placeholder. Every faction gets a distinct colour. |
| `fixed` | Use [`default-color.fill-color`](#default-colorfill-color) and [`default-color.line-color`](#default-colorline-color) for every such faction, so all unconfigured territory shares one colour. |

Matching is case-insensitive. Any unrecognised value is treated as `auto`, so a typo
leaves each faction its own colour rather than flattening the whole map to one.

**Example:**

```yaml
default-color:
  mode: fixed
```

---

## default-color.fill-color

**Type:** string (hex colour)
**Default:** `"#FFFFFF"`
**Description:** The fill colour for factions that have no entry under `factions:`.

> Read **only** when [`default-color.mode`](#default-colormode) is `fixed`. Under the
> default `auto` mode the colour comes from the faction itself, and this value is
> ignored. If either this key or `default-color.line-color` is not a valid hex colour,
> the plugin logs a warning and falls back to `auto` behaviour.

**Example:**

```yaml
default-color:
  mode: fixed
  fill-color: "#AAAAAA"
```

---

## default-color.fill-opacity

**Type:** double (0.0–1.0)
**Default:** `0.35`
**Description:** The opacity of the fill colour for factions that have no entry under `factions:`. Applied in both `auto` and `fixed` mode.

**Example:**

```yaml
default-color:
  fill-opacity: 0.5
```

---

## default-color.line-color

**Type:** string (hex colour)
**Default:** `"#209cee"`
**Description:** The border line colour for factions that have no entry under `factions:`.

> Read **only** when [`default-color.mode`](#default-colormode) is `fixed`. Under the
> default `auto` mode the colour comes from the faction itself, and this value is
> ignored. If either this key or `default-color.fill-color` is not a valid hex colour,
> the plugin logs a warning and falls back to `auto` behaviour.

**Example:**

```yaml
default-color:
  mode: fixed
  line-color: "#0055FF"
```

---

## default-color.line-opacity

**Type:** double (0.0–1.0)
**Default:** `1.0`
**Description:** The opacity of the border line for factions that have no entry under `factions:`. Applied in both `auto` and `fixed` mode.

**Example:**

```yaml
default-color:
  line-opacity: 0.8
```

---

## factions

**Type:** mapping of faction name → colour overrides
**Default:** *(empty)*
**Description:** Optional per-faction colour overrides. The key is the faction's display name exactly as it appears in Medieval Factions. Each entry supports the following sub-keys:

| Key           | Type   | Description                   |
|---------------|--------|-------------------------------|
| `fillColor`   | string | Hex fill colour (e.g. `#FF0000`) |
| `fillOpacity` | double | Fill opacity (0.0–1.0)         |
| `lineColor`   | string | Hex border colour             |
| `lineOpacity` | double | Border opacity (0.0–1.0)      |

**Example:**

```yaml
factions:
  ExampleFaction:
    fillColor: "#FF0000"
    fillOpacity: 0.35
    lineColor: "#FF0000"
    lineOpacity: 1.0
  AnotherFaction:
    fillColor: "#00FF00"
    fillOpacity: 0.4
    lineColor: "#007700"
    lineOpacity: 1.0
```
