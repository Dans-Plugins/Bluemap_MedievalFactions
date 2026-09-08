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

    /**
     * {@code default-color.mode} value that derives a colour per faction, from the
     * faction's own colour flag with the id hash as a fallback.
     */
    static final String MODE_AUTO = "auto";

    /**
     * {@code default-color.mode} value that paints every faction without a
     * {@code factions:} override in the configured {@code default-color.fill-color}
     * and {@code default-color.line-color}.
     */
    static final String MODE_FIXED = "fixed";

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

    /**
     * True when {@code mode} selects the fixed colours configured under
     * {@code default-color}.
     *
     * Matching is case-insensitive and ignores surrounding whitespace. Any
     * unrecognised value — including null and the empty string — means
     * {@link #MODE_AUTO}, so a typo leaves every faction its own colour rather than
     * flattening the whole map to a single one.
     */
    static boolean isFixedMode(String mode) {
        return mode != null && MODE_FIXED.equalsIgnoreCase(mode.trim());
    }

    /**
     * Resolves the colour of a faction that has no {@code factions:} override, under
     * {@link #MODE_AUTO}.
     *
     * Prefers the faction's own colour flag in Medieval Factions, so the web map
     * matches the colour players already see in chat and on territory titles. Falls
     * back to {@link #generateDeterministicColor} when {@code flagRgb} is null,
     * which is what the caller passes for a faction whose flag is unset or is still
     * the literal {@code random} placeholder.
     */
    static int resolveAutoColor(Integer flagRgb, String factionId) {
        return (flagRgb != null) ? flagRgb : generateDeterministicColor(factionId);
    }
}
