package com.kilz.mfbluemap;

import com.dansplugins.factionsystem.MedievalFactions;
import com.dansplugins.factionsystem.claim.MfClaimService;
import com.dansplugins.factionsystem.claim.MfClaimedChunk;
import com.dansplugins.factionsystem.event.faction.FactionClaimEvent;
import com.dansplugins.factionsystem.event.faction.FactionUnclaimEvent;
import com.dansplugins.factionsystem.faction.MfFaction;
import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.locationtech.jts.geom.Coordinate;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BlueMapIntegration implements Listener {

    private static final String MARKER_SET_ID = "mf_claims";
    private static final String MARKER_SET_LABEL = "Medieval Factions Claims";

    private final Plugin plugin;
    private MedievalFactions medievalFactions;
    private BlueMapAPI api;

    /**
     * One MarkerSet per BlueMap map, keyed by map id.
     *
     * A single shared MarkerSet cannot work: BlueMap renders a marker set on every
     * map it is attached to, so one shared set puts every world's claims on every
     * map. Overworld claims then appear on the Nether map at raw coordinates,
     * which is eight times out of place given the 1:8 coordinate ratio.
     */
    private final Map<String, MarkerSet> markerSetsByMapId = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduledExecutor;
    private final Map<String, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();
    private final Map<String, List<MarkerRef>> currentFactionMarkers = new ConcurrentHashMap<>();
    private final Map<String, Color[]> factionColors = new HashMap<>();
    private final long DEBOUNCE_MS = 1500L;

    public BlueMapIntegration(Plugin plugin) {
        this.plugin = plugin;
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor((r) -> {
            Thread t = new Thread(r, "mfbluemap-geom");
            t.setDaemon(true);
            return t;
        });

        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean enable(BlueMapAPI api, MedievalFactions medievalFactions) {
        this.medievalFactions = medievalFactions;
        this.api = api;

        this.markerSetsByMapId.clear();
        for (BlueMapMap map : api.getMaps()) {
            MarkerSet set = MarkerSet.builder().label(MARKER_SET_LABEL).build();
            map.getMarkerSets().put(MARKER_SET_ID, set);
            this.markerSetsByMapId.put(map.getId(), set);
        }
        plugin.getLogger().info("Registered claim marker sets on " + this.markerSetsByMapId.size() + " map(s): "
                + String.join(", ", this.markerSetsByMapId.keySet()));

        initFactionColors();
        initialSync();
        return true;
    }

    public void disable() {
        for (ScheduledFuture<?> f : this.pendingTasks.values())
            f.cancel(false);
        if (this.scheduledExecutor != null)
            this.scheduledExecutor.shutdownNow();
    }

    public void reload() {
        int pending = this.pendingTasks.size();
        for (ScheduledFuture<?> f : this.pendingTasks.values())
            f.cancel(false);
        this.pendingTasks.clear();

        initFactionColors();

        int removedCount = this.currentFactionMarkers.values().stream().mapToInt(List::size).sum();

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (List<MarkerRef> refs : this.currentFactionMarkers.values()) {
                for (MarkerRef ref : refs)
                    this.removeMarker(ref);
            }
            this.currentFactionMarkers.clear();

            plugin.getLogger().info(
                    "BlueMap Reload: cancelled " + pending + " tasks; removed " + removedCount + " markers.");

            for (MfFaction f : this.medievalFactions.getServices().getFactionService().getFactions()) {
                this.scheduleFactionRecompute(f.getId());
            }
        });
    }

    /**
     * Loads colors from config.yml
     */
    private void initFactionColors() {
        this.factionColors.clear();
        plugin.reloadConfig();

        // Load custom overrides from 'factions' section (previously SECTOR)
        if (plugin.getConfig().isConfigurationSection("factions")) {
            for (String factionName : plugin.getConfig().getConfigurationSection("factions").getKeys(false)) {
                try {
                    String fillHex = plugin.getConfig().getString("factions." + factionName + ".fillColor", "#FFFFFF");
                    float fillOpacity = (float) plugin.getConfig().getDouble("factions." + factionName + ".fillOpacity",
                            0.35);
                    String lineHex = plugin.getConfig().getString("factions." + factionName + ".lineColor", "#888888");
                    float lineOpacity = (float) plugin.getConfig().getDouble("factions." + factionName + ".lineOpacity",
                            1.0);

                    int fillRGB = FactionColors.parseColor(fillHex);
                    int lineRGB = FactionColors.parseColor(lineHex);

                    this.factionColors.put(factionName, new Color[] {
                            new Color(fillRGB, fillOpacity),
                            new Color(lineRGB, lineOpacity)
                    });
                } catch (Exception ex) {
                    plugin.getLogger().warning("Error loading color for " + factionName + ": " + ex.getMessage());
                }
            }
        }
    }

    private void initialSync() {
        plugin.getLogger().info("Synchronizing (initial - batch mode) MedievalFactions claims with BlueMap...");

        List<MfFaction> factions = new ArrayList<>(
                this.medievalFactions.getServices().getFactionService().getFactions());
        int batchSize = 2;
        int delayTicks = 20;

        for (int i = 0; i < factions.size(); i += batchSize) {
            final int batchIndex = i;
            final int batchEnd = Math.min(i + batchSize, factions.size());

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int j = batchIndex; j < batchEnd; j++) {
                    this.scheduleFactionRecompute(factions.get(j).getId());
                }
                plugin.getLogger().info(String.format(
                        "  BlueMap: processing factions %d-%d of %d",
                        batchIndex + 1, batchEnd, factions.size()));
            }, (i / batchSize) * delayTicks);
        }
    }

    @EventHandler
    public void onFactionClaim(FactionClaimEvent event) {
        this.scheduleFactionRecompute(event.getClaim().getFactionId());
    }

    @EventHandler
    public void onFactionUnclaim(FactionUnclaimEvent event) {
        this.scheduleFactionRecompute(event.getClaim().getFactionId());
    }

    private void scheduleFactionRecompute(String factionId) {
        // We use the full service to get claims
        List<MfClaimedChunk> snapshot = new ArrayList<>(
                this.medievalFactions.getServices().getClaimService().getClaimsByFactionId(factionId));
        ScheduledFuture<?> previous = this.pendingTasks.get(factionId);
        if (previous != null && !previous.isDone()) {
            previous.cancel(false);
        }

        ScheduledFuture<?> future = this.scheduledExecutor.schedule(() -> {
            this.pendingTasks.remove(factionId);
            try {
                Map<UUID, List<ClaimGeometry.PolygonData>> perWorld = this.computeUnionPerWorld(snapshot);
                Bukkit.getScheduler().runTask(plugin, () -> this.applyFactionMarkers(factionId, perWorld));
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE,
                        "Error processing geometry for faction " + factionId, t);
            }
        }, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        this.pendingTasks.put(factionId, future);
    }

    private Map<UUID, List<ClaimGeometry.PolygonData>> computeUnionPerWorld(List<MfClaimedChunk> claimsSnapshot) {
        Map<UUID, List<MfClaimedChunk>> byWorld = claimsSnapshot.stream()
                .collect(Collectors.groupingBy(MfClaimedChunk::getWorldId));
        Map<UUID, List<ClaimGeometry.PolygonData>> result = new HashMap<>();

        for (Map.Entry<UUID, List<MfClaimedChunk>> e : byWorld.entrySet()) {
            List<ClaimGeometry.ChunkPos> chunks = e.getValue().stream()
                    .map(c -> new ClaimGeometry.ChunkPos(c.getX(), c.getZ()))
                    .collect(Collectors.toList());
            result.put(e.getKey(), ClaimGeometry.unionChunks(chunks));
        }
        return result;
    }

    /**
     * Resolves the BlueMap maps that render a given Minecraft world.
     *
     * Returns empty when BlueMap does not render the world at all — the End has no
     * configured map on this server, and claims there must simply not be drawn
     * rather than being dumped onto whichever map happens to be handy.
     *
     * Must run on the main thread: it calls into the Bukkit world lookup.
     */
    private Collection<BlueMapMap> mapsForWorld(UUID worldId) {
        if (this.api == null)
            return Collections.emptyList();
        World bukkitWorld = Bukkit.getWorld(worldId);
        if (bukkitWorld == null)
            return Collections.emptyList();
        Optional<BlueMapWorld> blueMapWorld = this.api.getWorld(bukkitWorld);
        return blueMapWorld.map(BlueMapWorld::getMaps).orElse(Collections.emptyList());
    }

    private void removeMarker(MarkerRef ref) {
        MarkerSet set = this.markerSetsByMapId.get(ref.mapId);
        if (set != null)
            set.remove(ref.markerId);
    }

    private void applyFactionMarkers(String factionId, Map<UUID, List<ClaimGeometry.PolygonData>> perWorld) {
        List<MarkerRef> old = this.currentFactionMarkers.remove(factionId);
        if (old != null)
            old.forEach(this::removeMarker);

        List<MarkerRef> newRefs = new ArrayList<>();
        MfFaction faction = this.findFactionById(factionId);
        String factionName = (faction != null) ? faction.getName() : factionId;

        // Determine color:
        // 1. Check if configured in 'factions' config override
        // 2. Otherwise use the faction's own colour flag in MedievalFactions
        // 3. Otherwise fall back to a deterministic hash of the faction id
        Color[] colors = this.factionColors.get(factionName);

        if (colors == null) {
            // Get defaults from config
            float fillOpacity = (float) plugin.getConfig().getDouble("default-color.fill-opacity", 0.35);
            float lineOpacity = (float) plugin.getConfig().getDouble("default-color.line-opacity", 1.0);

            // Prefer the faction's own colour flag, so the web map matches the colour
            // players already see in chat and territory titles. Fall back to a
            // deterministic hash if the flag is absent or unparseable (for example
            // when it is still the literal "random" placeholder).
            Integer rgb = this.getFactionFlagColor(faction);
            int resolved = (rgb != null) ? rgb : FactionColors.generateDeterministicColor(factionId);

            colors = new Color[] {
                    new Color(resolved, fillOpacity),
                    new Color(resolved, lineOpacity)
            };
        }

        // Configurable values
        float mapY = (float) plugin.getConfig().getDouble("bluemap.y-level", 70.0);
        String labelFormat = plugin.getConfig().getString("bluemap.label-format", "Faction: %faction%");
        String label = labelFormat.replace("%faction%", factionName);

        for (Map.Entry<UUID, List<ClaimGeometry.PolygonData>> e : perWorld.entrySet()) {
            UUID worldId = e.getKey();
            List<ClaimGeometry.PolygonData> polygons = e.getValue();
            if (polygons.isEmpty())
                continue;

            // Only draw this world's claims on the maps that actually render it.
            Collection<BlueMapMap> maps = this.mapsForWorld(worldId);
            if (maps.isEmpty())
                continue;

            for (int i = 0; i < polygons.size(); ++i) {
                ClaimGeometry.PolygonData pd = polygons.get(i);
                List<Vector2d> outer = new ArrayList<>();
                for (int k = 0; k < pd.shell.length - 1; ++k) {
                    outer.add(new Vector2d(pd.shell[k].x, pd.shell[k].y));
                }

                Shape outerShape = new Shape(outer);
                List<Shape> holeShapes = new ArrayList<>();
                for (Coordinate[] holeCoords : pd.holes) {
                    List<Vector2d> hpts = new ArrayList<>();
                    for (int k = 0; k < holeCoords.length - 1; ++k) {
                        hpts.add(new Vector2d(holeCoords[k].x, holeCoords[k].y));
                    }
                    holeShapes.add(new Shape(hpts));
                }

                ShapeMarker.Builder b = ShapeMarker.builder()
                        .label(label)
                        .shape(outerShape, mapY)
                        .fillColor(colors[0])
                        .lineColor(colors[1])
                        .lineWidth(2)
                        .centerPosition();

                if (!holeShapes.isEmpty())
                    b.holes(holeShapes.toArray(new Shape[0]));

                ShapeMarker marker = b.build();
                String id = "faction_" + factionId + "_world_" + worldId + "_poly_" + i;

                for (BlueMapMap map : maps) {
                    MarkerSet set = this.markerSetsByMapId.get(map.getId());
                    if (set == null)
                        continue;
                    set.put(id, marker);
                    newRefs.add(new MarkerRef(map.getId(), id));
                }
            }
        }
        this.currentFactionMarkers.put(factionId, newRefs);
    }

    private MfFaction findFactionById(String id) {
        for (MfFaction f : this.medievalFactions.getServices().getFactionService().getFactions()) {
            if (f.getId().equals(id))
                return f;
        }
        return null;
    }

    /**
     * Reads the faction's colour flag from MedievalFactions and parses it to RGB.
     * Returns null when the faction is unknown, the flag is unset, or the value is
     * not a hex colour (MedievalFactions stores "random" as a placeholder).
     */
    private Integer getFactionFlagColor(MfFaction faction) {
        if (faction == null)
            return null;
        try {
            String hex = faction.getFlags().get(this.medievalFactions.getFlags().getColor());
            if (hex == null)
                return null;
            hex = hex.trim();
            if (hex.startsWith("#"))
                hex = hex.substring(1);
            if (hex.length() != 6)
                return null;
            return FactionColors.parseColor(hex);
        } catch (Exception ex) {
            plugin.getLogger().warning(
                    "Could not read colour flag for faction " + faction.getName() + ": " + ex.getMessage());
            return null;
        }
    }

    /** A marker as placed on one specific BlueMap map. */
    private static class MarkerRef {
        final String mapId;
        final String markerId;

        MarkerRef(String mapId, String markerId) {
            this.mapId = mapId;
            this.markerId = markerId;
        }
    }
}
