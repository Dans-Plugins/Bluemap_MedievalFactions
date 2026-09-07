package com.kilz.mfbluemap;

/**
 * Colour parsing and fallback colour derivation for faction overlays.
 *
 * Deliberately free of Bukkit, BlueMap and MedievalFactions types so the logic can
 * be exercised by unit tests without a running server.
 */
final class FactionColors {

    /** Mask that keeps the low 24 bits of a hash — one byte each of red, green and blue. */
    private static final int RGB_MASK = 0xFFFFFF;

    private FactionColors() {
    }

    /**
     * Parses a hex colour such as {@code "#FF0000"} or {@code "FF0000"} into packed RGB.
     *
     * @throws NumberFormatException if the value is not hexadecimal
     * @throws NullPointerException  if {@code hex} is null
     */
    static int parseColor(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return Integer.parseInt(hex, 16);
    }

    /**
     * Derives a stable colour from a faction id.
     *
     * Used only when a faction has neither a {@code factions:} override in
     * {@code config.yml} nor a usable colour flag in MedievalFactions. The same id
     * always yields the same colour, so a faction keeps its colour across restarts
     * without any configuration.
     */
    static int generateDeterministicColor(String input) {
        return input.hashCode() & RGB_MASK;
    }
}
