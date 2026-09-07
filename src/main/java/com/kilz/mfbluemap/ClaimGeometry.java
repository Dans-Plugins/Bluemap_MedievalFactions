package com.kilz.mfbluemap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Merges claimed chunks into contiguous polygons.
 *
 * Deliberately free of Bukkit, BlueMap and MedievalFactions types so the geometry
 * can be exercised by unit tests without a running server. Callers are responsible
 * for grouping claims by world before calling in — a union across worlds would be
 * meaningless.
 *
 * Coordinates are block coordinates in the horizontal plane: {@link Coordinate#x} is
 * the world X axis and {@link Coordinate#y} is the world Z axis, matching what
 * BlueMap's two-dimensional {@code Shape} expects.
 */
final class ClaimGeometry {

    /** Width and depth of a Minecraft chunk, in blocks. */
    static final double CHUNK_SIZE = 16.0;

    /**
     * JTS documents {@code GeometryFactory} as safe to share; this class is called
     * from the {@code mfbluemap-geom} executor thread.
     */
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private ClaimGeometry() {
    }

    /**
     * Unions the chunk squares into the smallest set of polygons that covers them.
     *
     * Chunks that touch along an edge or a corner become a single polygon; a region
     * fully enclosed by claims but not itself claimed becomes a hole on the
     * enclosing polygon. Returns an empty list for an empty input.
     */
    static List<PolygonData> unionChunks(List<ChunkPos> chunks) {
        if (chunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Polygon> rects = new ArrayList<>(chunks.size());
        for (ChunkPos chunk : chunks) {
            double minX = (double) chunk.x * CHUNK_SIZE;
            double minZ = (double) chunk.z * CHUNK_SIZE;
            double maxX = minX + CHUNK_SIZE;
            double maxZ = minZ + CHUNK_SIZE;
            Coordinate[] coords = new Coordinate[] {
                    new Coordinate(minX, minZ),
                    new Coordinate(maxX, minZ),
                    new Coordinate(maxX, maxZ),
                    new Coordinate(minX, maxZ),
                    new Coordinate(minX, minZ)
            };
            rects.add(GEOMETRY_FACTORY.createPolygon(coords));
        }

        Geometry unioned = UnaryUnionOp.union(rects);
        List<PolygonData> polygons = new ArrayList<>();
        int num = unioned.getNumGeometries();

        for (int i = 0; i < num; ++i) {
            Geometry g = unioned.getGeometryN(i);
            if (g instanceof Polygon) {
                Polygon poly = (Polygon) g;
                Coordinate[] shellCoords = poly.getExteriorRing().getCoordinates();
                List<Coordinate[]> holes = new ArrayList<>();
                int holeCount = poly.getNumInteriorRing();
                for (int h = 0; h < holeCount; ++h) {
                    holes.add(poly.getInteriorRingN(h).getCoordinates());
                }
                polygons.add(new PolygonData(shellCoords, holes));
            }
        }
        return polygons;
    }

    /** The chunk-grid position of a claimed chunk, in chunk units rather than blocks. */
    static final class ChunkPos {
        final int x;
        final int z;

        ChunkPos(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    /** One merged claim polygon: a closed outer ring plus zero or more closed hole rings. */
    static final class PolygonData {
        final Coordinate[] shell;
        final List<Coordinate[]> holes;

        PolygonData(Coordinate[] shell, List<Coordinate[]> holes) {
            this.shell = shell;
            this.holes = holes;
        }
    }
}
