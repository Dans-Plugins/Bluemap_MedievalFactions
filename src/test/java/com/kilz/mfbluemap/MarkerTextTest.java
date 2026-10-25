package com.kilz.mfbluemap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkerTextTest {

    private static final String SCRIPT_NAME = "<img src=x onerror=alert(1)>";

    @Test
    void labelAndDetailMatchForAnOrdinaryFactionName() {
        assertEquals("Faction: Ironhold", MarkerText.label("Faction: %faction%", "Ironhold"));
        assertEquals("Faction: Ironhold", MarkerText.detail("Faction: %faction%", "Ironhold"));
    }

    @Test
    void detailEscapesHtmlInTheFactionName() {
        assertEquals("Faction: &lt;img src=x onerror=alert(1)&gt;",
                MarkerText.detail("Faction: %faction%", SCRIPT_NAME));
    }

    @Test
    void labelLeavesTheFactionNameForBlueMapToEscape() {
        assertEquals("Faction: " + SCRIPT_NAME, MarkerText.label("Faction: %faction%", SCRIPT_NAME));
    }

    @Test
    void detailKeepsHtmlWrittenIntoTheLabelFormat() {
        assertEquals("<b>Tom &amp; Jerry</b> Territory",
                MarkerText.detail("<b>%faction%</b> Territory", "Tom & Jerry"));
    }

    @Test
    void everyPlaceholderIsReplaced() {
        assertEquals("North / North", MarkerText.label("%faction% / %faction%", "North"));
        assertEquals("&lt;N&gt; / &lt;N&gt;", MarkerText.detail("%faction% / %faction%", "<N>"));
    }

    @Test
    void replacementCharactersInTheNameAreTakenLiterally() {
        assertEquals("Faction: $1\\x", MarkerText.label("Faction: %faction%", "$1\\x"));
    }

    @Test
    void escapesAllFiveHtmlSignificantCharacters() {
        assertEquals("&amp;&lt;&gt;&quot;&#39;", MarkerText.escapeHtml("&<>\"'"));
        assertEquals("plain text", MarkerText.escapeHtml("plain text"));
        assertEquals("", MarkerText.escapeHtml(""));
    }
}
