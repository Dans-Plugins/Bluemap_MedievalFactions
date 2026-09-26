package com.kilz.mfbluemap;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimGeometryTest {

    @Test
    void noChunksProduceNoPolygons() {
        assertTrue(ClaimGeometry.unionChunks(Collections.emptyList()).isEmpty());
    }

    @Test
    void aSingleChunkBecomesItsSixteenBlockSquare() {
        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(chunks(0, 0));

        assertEquals(1, polygons.size());
        ClaimGeometry.PolygonData polygon = polygons.get(0);
        assertBounds(polygon.shell, 0.0, 0.0, 16.0, 16.0);
        assertTrue(polygon.holes.isEmpty());
    }

    @Test
    void chunkCoordinatesAreScaledToBlockCoordinates() {
        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(chunks(-2, 3));

        assertEquals(1, polygons.size());
        assertBounds(polygons.get(0).shell, -32.0, 48.0, -16.0, 64.0);
    }

    @Test
    void adjacentChunksMergeIntoOnePolygon() {
        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(chunks(0, 0, 1, 0));

        assertEquals(1, polygons.size());
        assertBounds(polygons.get(0).shell, 0.0, 0.0, 32.0, 16.0);
        assertTrue(polygons.get(0).holes.isEmpty());
    }

    @Test
    void disjointChunksStaySeparatePolygons() {
        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(chunks(0, 0, 10, 10));

        assertEquals(2, polygons.size());
    }

    @Test
    void chunksEitherSideOfTheOriginMergeIntoOnePolygon() {
        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(chunks(-1, -1, 0, -1, -1, 0, 0, 0));

        assertEquals(1, polygons.size());
        assertBounds(polygons.get(0).shell, -16.0, -16.0, 16.0, 16.0);
        assertTrue(polygons.get(0).holes.isEmpty());
    }

    @Test
    void chunksTouchingOnlyAtACornerStaySeparatePolygons() {
        // JTS does not merge polygons that share a single vertex, so diagonal
        // neighbours are drawn as two shapes that meet at a point.
        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(chunks(0, 0, 1, 1));

        assertEquals(2, polygons.size());
        for (ClaimGeometry.PolygonData polygon : polygons) {
            assertTrue(polygon.holes.isEmpty());
        }
    }

    @Test
    void aRingOfChunksLeavesTheUnclaimedCentreAsAHole() {
        // Every chunk of a 3x3 block except the middle one.
        List<ClaimGeometry.ChunkPos> ring = new ArrayList<>();
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if (x == 1 && z == 1) {
                    continue;
                }
                ring.add(new ClaimGeometry.ChunkPos(x, z));
            }
        }

        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(ring);

        assertEquals(1, polygons.size());
        ClaimGeometry.PolygonData polygon = polygons.get(0);
        assertBounds(polygon.shell, 0.0, 0.0, 48.0, 48.0);
        assertEquals(1, polygon.holes.size());
        assertBounds(polygon.holes.get(0), 16.0, 16.0, 32.0, 32.0);
    }

    @Test
    void separateEnclavesBecomeSeparateHolesOnOnePolygon() {
        // Every chunk of a 5x3 block except (1, 1) and (3, 1).
        List<ClaimGeometry.ChunkPos> claimed = new ArrayList<>();
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 3; z++) {
                if (z == 1 && (x == 1 || x == 3)) {
                    continue;
                }
                claimed.add(new ClaimGeometry.ChunkPos(x, z));
            }
        }

        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(claimed);

        assertEquals(1, polygons.size());
        ClaimGeometry.PolygonData polygon = polygons.get(0);
        assertBounds(polygon.shell, 0.0, 0.0, 80.0, 48.0);
        assertEquals(2, polygon.holes.size());

        // JTS does not promise an order for interior rings; sort them west to east.
        List<Coordinate[]> holes = new ArrayList<>(polygon.holes);
        holes.sort(Comparator.comparingDouble(ClaimGeometryTest::minX));
        assertBounds(holes.get(0), 16.0, 16.0, 32.0, 32.0);
        assertBounds(holes.get(1), 48.0, 16.0, 64.0, 32.0);
    }

    @Test
    void duplicateChunksDoNotDuplicatePolygons() {
        List<ClaimGeometry.PolygonData> polygons = ClaimGeometry.unionChunks(chunks(5, 5, 5, 5));

        assertEquals(1, polygons.size());
        assertBounds(polygons.get(0).shell, 80.0, 80.0, 96.0, 96.0);
    }

    /** Builds a chunk list from alternating x, z pairs. */
    private static List<ClaimGeometry.ChunkPos> chunks(int... xzPairs) {
        List<ClaimGeometry.ChunkPos> result = new ArrayList<>(xzPairs.length / 2);
        for (int i = 0; i < xzPairs.length; i += 2) {
            result.add(new ClaimGeometry.ChunkPos(xzPairs[i], xzPairs[i + 1]));
        }
        return result;
    }

    private static double minX(Coordinate[] ring) {
        double min = Double.POSITIVE_INFINITY;
        for (Coordinate c : ring) {
            min = Math.min(min, c.x);
        }
        return min;
    }

    /**
     * Asserts the extent of a ring without depending on where JTS starts it or which
     * way round it winds — neither is part of the contract this class offers.
     */
    private static void assertBounds(Coordinate[] ring, double minX, double minZ, double maxX, double maxZ) {
        assertTrue(ring.length >= 5, "a closed ring needs at least five coordinates, got " + ring.length);
        assertEquals(ring[0], ring[ring.length - 1], "ring should be closed");

        double[] xs = new double[ring.length];
        double[] zs = new double[ring.length];
        for (int i = 0; i < ring.length; i++) {
            xs[i] = ring[i].x;
            // Coordinate.y carries the world Z axis; see ClaimGeometry's class comment.
            zs[i] = ring[i].y;
        }
        Arrays.sort(xs);
        Arrays.sort(zs);

        assertEquals(minX, xs[0]);
        assertEquals(maxX, xs[xs.length - 1]);
        assertEquals(minZ, zs[0]);
        assertEquals(maxZ, zs[zs.length - 1]);
    }
}
