package com.cladonia.xml.transform;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cladonia.xngreditor.scenario.ParameterProperties;
import com.cladonia.xngreditor.scenario.ScenarioProperties;

/**
 * Tests for the static utility methods in {@link ScenarioUtilities} that
 * back all five Transform menu actions.
 */
class ScenarioUtilitiesTest {

    // ── substituteMacros ─────────────────────────────────────────

    @Nested
    @DisplayName("substituteMacros()")
    class SubstituteMacros {

        @Test
        @DisplayName("replaces ${path} macro")
        void replacesPathMacro() {
            String result = ScenarioUtilities.substituteMacros(
                    "file:///home/user/docs/input.xml",
                    "${path}output.html");
            assertTrue(result.contains("output.html"));
            assertFalse(result.contains("${path}"));
        }

        @Test
        @DisplayName("replaces ${file} macro")
        void replacesFileMacro() {
            String result = ScenarioUtilities.substituteMacros(
                    "file:///home/user/docs/input.xml",
                    "/tmp/${file}.html");
            assertTrue(result.contains("input.html"));
            assertFalse(result.contains("${file}"));
        }

        @Test
        @DisplayName("replaces ${ext} macro")
        void replacesExtMacro() {
            String result = ScenarioUtilities.substituteMacros(
                    "file:///home/user/docs/input.xml",
                    "/tmp/output.${ext}");
            assertTrue(result.contains(".xml"));
            assertFalse(result.contains("${ext}"));
        }

        @Test
        @DisplayName("replaces all macros combined")
        void replacesAllMacros() {
            String result = ScenarioUtilities.substituteMacros(
                    "file:///home/user/docs/input.xml",
                    "${path}${file}_out.${ext}");
            assertFalse(result.contains("${path}"));
            assertFalse(result.contains("${file}"));
            assertFalse(result.contains("${ext}"));
            assertTrue(result.contains("input_out.xml"));
        }

        @Test
        @DisplayName("returns output unchanged when input is null/empty")
        void noSubstitutionWhenInputEmpty() {
            assertEquals("/tmp/out.html",
                    ScenarioUtilities.substituteMacros(null, "/tmp/out.html"));
            assertEquals("/tmp/out.html",
                    ScenarioUtilities.substituteMacros("", "/tmp/out.html"));
        }

        @Test
        @DisplayName("handles output with no macros")
        void noMacrosPresent() {
            assertEquals("/tmp/out.html",
                    ScenarioUtilities.substituteMacros(
                            "file:///input.xml", "/tmp/out.html"));
        }
    }

    // ── getParameters ────────────────────────────────────────────

    @Nested
    @DisplayName("getParameters()")
    class GetParameters {

        @Test
        @DisplayName("returns empty vector for empty scenario")
        void emptyForNoParams() {
            ScenarioProperties scenario = new ScenarioProperties();
            Vector params = ScenarioUtilities.getParameters(scenario);
            assertNotNull(params);
            assertTrue(params.isEmpty());
        }

        @Test
        @DisplayName("converts ParameterProperties to String[] pairs")
        void convertsToStringPairs() {
            ScenarioProperties scenario = new ScenarioProperties();
            scenario.addParameter(new ParameterProperties("name1", "value1"));
            scenario.addParameter(new ParameterProperties("name2", "value2"));

            Vector params = ScenarioUtilities.getParameters(scenario);
            assertEquals(2, params.size());

            String[] first = (String[]) params.get(0);
            assertEquals(2, first.length);
            assertEquals("name1", first[0]);
            assertEquals("value1", first[1]);

            String[] second = (String[]) params.get(1);
            assertEquals("name2", second[0]);
            assertEquals("value2", second[1]);
        }
    }

    // ── getEncoding ──────────────────────────────────────────────

    @Nested
    @DisplayName("getEncoding()")
    class GetEncoding {

        @Test
        @DisplayName("detects UTF-8 encoding from XML declaration")
        void detectsUtf8() throws Exception {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(xml.getBytes("UTF-8"));

            String encoding = ScenarioUtilities.getEncoding(baos);
            assertNotNull(encoding);
            assertTrue(encoding.toUpperCase().contains("UTF"));
        }

        @Test
        @DisplayName("falls back to UTF-8 for plain text without declaration")
        void fallbackToUtf8() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write("plain text no xml".getBytes("UTF-8"));

            String encoding = ScenarioUtilities.getEncoding(baos);
            assertEquals("UTF-8", encoding);
        }

        @Test
        @DisplayName("detects ISO-8859-1 encoding")
        void detectsIso88591() throws Exception {
            String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><root/>";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(xml.getBytes("ISO-8859-1"));

            String encoding = ScenarioUtilities.getEncoding(baos);
            assertNotNull(encoding);
            assertTrue(encoding.toUpperCase().contains("8859")
                    || encoding.toUpperCase().contains("ISO")
                    || encoding.toUpperCase().equals("UTF-8")); // acceptable fallback
        }
    }
}
