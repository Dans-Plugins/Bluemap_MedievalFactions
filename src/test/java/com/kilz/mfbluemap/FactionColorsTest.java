package com.kilz.mfbluemap;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactionColorsTest {

    @Test
    void parsesHexWithAndWithoutTheLeadingHash() {
        assertEquals(0xFF0000, FactionColors.parseColor("#FF0000"));
        assertEquals(0xFF0000, FactionColors.parseColor("FF0000"));
        assertEquals(0x00FF00, FactionColors.parseColor("#00FF00"));
        assertEquals(0x209CEE, FactionColors.parseColor("#209cee"));
        assertEquals(0x000000, FactionColors.parseColor("#000000"));
        assertEquals(0xFFFFFF, FactionColors.parseColor("#FFFFFF"));
    }

    @Test
    void rejectsValuesThatAreNotHexadecimal() {
        assertThrows(NumberFormatException.class, () -> FactionColors.parseColor("#GGGGGG"));
        assertThrows(NumberFormatException.class, () -> FactionColors.parseColor("random"));
        assertThrows(NumberFormatException.class, () -> FactionColors.parseColor("#"));
        assertThrows(NumberFormatException.class, () -> FactionColors.parseColor(""));
    }

    @Test
    void derivedColoursAreStableForTheSameFactionId() {
        String factionId = "3f2a1c44-0000-4000-8000-000000000001";

        assertEquals(
                FactionColors.generateDeterministicColor(factionId),
                FactionColors.generateDeterministicColor(factionId));
    }

    @Test
    void derivedColoursDifferForDifferentFactionIds() {
        assertNotEquals(
                FactionColors.generateDeterministicColor("abc"),
                FactionColors.generateDeterministicColor("abd"));
    }

    @Test
    void derivedColoursAlwaysFitInTwentyFourBits() {
        // Random ids exercise negative hash codes, which a naive derivation would
        // turn into an out-of-range or negative colour.
        for (int i = 0; i < 500; i++) {
            int colour = FactionColors.generateDeterministicColor(UUID.randomUUID().toString());
            assertTrue(colour >= 0x000000 && colour <= 0xFFFFFF, "out of range: " + colour);
        }
    }

    @Test
    void derivedColoursMatchThePreExtractionFormula() {
        // The extraction replaced a three-way mask-shift-recombine with a single
        // mask. This pins the two as equivalent, negative hash codes included.
        for (int i = 0; i < 500; i++) {
            String factionId = UUID.randomUUID().toString();
            assertEquals(legacyDeterministicColor(factionId),
                    FactionColors.generateDeterministicColor(factionId));
        }
        assertEquals(legacyDeterministicColor("abc"), FactionColors.generateDeterministicColor("abc"));
    }

    @Test
    void fixedModeIsRecognisedRegardlessOfCaseOrSurroundingWhitespace() {
        assertTrue(FactionColors.isFixedMode("fixed"));
        assertTrue(FactionColors.isFixedMode("FIXED"));
        assertTrue(FactionColors.isFixedMode("Fixed"));
        assertTrue(FactionColors.isFixedMode("  fixed  "));
    }

    @Test
    void everyOtherModeValueMeansAuto() {
        // A typo must leave each faction its own colour rather than flattening the
        // whole map to the one configured default colour.
        assertFalse(FactionColors.isFixedMode("auto"));
        assertFalse(FactionColors.isFixedMode("AUTO"));
        assertFalse(FactionColors.isFixedMode("fixxed"));
        assertFalse(FactionColors.isFixedMode(""));
        assertFalse(FactionColors.isFixedMode("   "));
        assertFalse(FactionColors.isFixedMode(null));
    }

    @Test
    void autoModePrefersTheFactionsOwnColourFlag() {
        assertEquals(0x209CEE, FactionColors.resolveAutoColor(0x209CEE, "some-faction-id"));
    }

    @Test
    void autoModeFallsBackToTheIdHashWhenTheFlagIsAbsent() {
        String factionId = "3f2a1c44-0000-4000-8000-000000000001";

        assertEquals(FactionColors.generateDeterministicColor(factionId),
                FactionColors.resolveAutoColor(null, factionId));
    }

    @Test
    void autoModeHonoursABlackColourFlagRatherThanTreatingItAsAbsent() {
        // 0x000000 is falsy-looking but a legitimate colour; an unboxing-order slip
        // here would silently swap it for the id hash.
        assertEquals(0x000000, FactionColors.resolveAutoColor(0x000000, "some-faction-id"));
    }

    /** The expression that lived in BlueMapIntegration before FactionColors existed. */
    private static int legacyDeterministicColor(String input) {
        int hash = input.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);
        return (r << 16) | (g << 8) | b;
    }
}
