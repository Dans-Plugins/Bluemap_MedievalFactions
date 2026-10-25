package com.kilz.mfbluemap;

/**
 * Builds the text shown on a faction's claim markers from {@code bluemap.label-format}.
 *
 * Deliberately free of Bukkit, BlueMap and MedievalFactions types so the logic can
 * be exercised by unit tests without a running server.
 */
final class MarkerText {

    /** Placeholder in {@code bluemap.label-format} that is replaced with the faction name. */
    static final String FACTION_PLACEHOLDER = "%faction%";

    private MarkerText() {
    }

    /**
     * The marker label, shown in BlueMap's marker list.
     *
     * The faction name is inserted as-is: BlueMap escapes HTML in a label itself.
     */
    static String label(String labelFormat, String factionName) {
        return labelFormat.replace(FACTION_PLACEHOLDER, factionName);
    }

    /**
     * The marker detail, shown in the popup when a claim is clicked.
     *
     * BlueMap renders the detail as HTML without escaping it, and when no detail is
     * set it falls back to the label — so a faction named with HTML tags would be
     * injected into every visitor's web map. The faction name is chosen by players,
     * so it is escaped here. The rest of {@code labelFormat} is written by the server
     * admin and is left as-is, so HTML styling in it still renders in the popup.
     */
    static String detail(String labelFormat, String factionName) {
        return labelFormat.replace(FACTION_PLACEHOLDER, escapeHtml(factionName));
    }

    /** Escapes the five characters that are significant in HTML text and attributes. */
    static String escapeHtml(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
