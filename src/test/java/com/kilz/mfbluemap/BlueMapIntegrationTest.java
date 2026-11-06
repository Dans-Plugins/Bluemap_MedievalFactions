package com.kilz.mfbluemap;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueMapIntegrationTest {

    private static final String CLAIMS_SET_ID = "mf_claims";

    /** A BlueMapMap that answers only getMarkerSets(), which is all the code under test calls. */
    private static BlueMapMap mapWith(Map<String, MarkerSet> markerSets) {
        return (BlueMapMap) Proxy.newProxyInstance(
                BlueMapMap.class.getClassLoader(),
                new Class<?>[] { BlueMapMap.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getMarkerSets"))
                        return markerSets;
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static Map<String, MarkerSet> markerSets(String... ids) {
        Map<String, MarkerSet> sets = new HashMap<>();
        for (String id : ids)
            sets.put(id, new MarkerSet(id));
        return sets;
    }

    @Test
    void removesTheClaimsSetFromEveryMap() {
        Map<String, MarkerSet> overworld = markerSets(CLAIMS_SET_ID);
        Map<String, MarkerSet> nether = markerSets(CLAIMS_SET_ID);

        BlueMapIntegration.removeMarkerSets(Arrays.asList(mapWith(overworld), mapWith(nether)));

        assertTrue(overworld.isEmpty());
        assertTrue(nether.isEmpty());
    }

    @Test
    void leavesMarkerSetsFromOtherPluginsInPlace() {
        Map<String, MarkerSet> sets = markerSets(CLAIMS_SET_ID, "other_plugin");

        BlueMapIntegration.removeMarkerSets(Collections.singletonList(mapWith(sets)));

        assertFalse(sets.containsKey(CLAIMS_SET_ID));
        assertEquals(Collections.singleton("other_plugin"), sets.keySet());
    }

    @Test
    void aMapWithoutTheClaimsSetIsLeftUnchanged() {
        Map<String, MarkerSet> sets = markerSets("other_plugin");

        BlueMapIntegration.removeMarkerSets(Collections.singletonList(mapWith(sets)));

        assertEquals(Collections.singleton("other_plugin"), sets.keySet());
    }
}
