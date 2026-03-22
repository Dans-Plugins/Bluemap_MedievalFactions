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

## default-color.fill-color

**Type:** string (hex colour)
**Default:** `"#FFFFFF"`
**Description:** The fill colour used for factions that have no entry under `factions:`. This setting is overridden when a deterministic colour is generated from the faction ID (see `generateDeterministicColor` in the source).

**Example:**

```yaml
default-color:
  fill-color: "#AAAAAA"
```

---

## default-color.fill-opacity

**Type:** double (0.0–1.0)
**Default:** `0.35`
**Description:** The opacity of the fill colour for dynamically coloured factions.

**Example:**

```yaml
default-color:
  fill-opacity: 0.5
```

---

## default-color.line-color

**Type:** string (hex colour)
**Default:** `"#209cee"`
**Description:** The border line colour used for factions that have no entry under `factions:`.

**Example:**

```yaml
default-color:
  line-color: "#0055FF"
```

---

## default-color.line-opacity

**Type:** double (0.0–1.0)
**Default:** `1.0`
**Description:** The opacity of the border line for dynamically coloured factions.

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
