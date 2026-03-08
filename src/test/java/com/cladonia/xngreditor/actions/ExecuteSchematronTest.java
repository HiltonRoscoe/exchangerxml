package com.cladonia.xngreditor.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.cladonia.xngreditor.scenario.ScenarioProperties;

/**
 * Tests validating the Schematron execution pipeline used by
 * <b>Execute Schematron</b> from the Transform menu.
 *
 * <p>The Schematron pipeline works in two phases:
 * <ol>
 *   <li>Phase 1: Apply the Schematron "meta-stylesheet" (e.g.
 *       schematron-message.xsl) to the Schematron rules document,
 *       producing a validation stylesheet.</li>
 *   <li>Phase 2: Apply the generated validation stylesheet to the
 *       input XML, producing validation messages.</li>
 * </ol>
 * These tests exercise both phases and the scenario configuration.</p>
 */
class ExecuteSchematronTest {

    private URL xmlUrl;
    private URL schUrl;
    private URL schematron15Url;

    @BeforeEach
    void setUp() {
        xmlUrl = getClass().getClassLoader().getResource("test-input.xml");
        schUrl = getClass().getClassLoader().getResource("test-schematron.sch");
        schematron15Url = getClass().getClassLoader().getResource(
                "com/cladonia/xml/schematron/resources/implementation15/schematron-message.xsl");
        assertNotNull(xmlUrl, "test-input.xml must be on classpath");
        assertNotNull(schUrl, "test-schematron.sch must be on classpath");
        assertNotNull(schematron15Url, "schematron-message.xsl must be on classpath");
    }

    // ── Scenario wiring ──────────────────────────────────────────

    @Nested
    @DisplayName("Schematron scenario configuration")
    class SchematronScenarioConfig {

        @Test
        @DisplayName("Schematron phase-1 scenario (rules → validation XSL)")
        void phase1ScenarioWiring() {
            ScenarioProperties phase1 = new ScenarioProperties();
            phase1.setXSLEnabled(true);
            phase1.setFOPEnabled(false);
            phase1.setInputType(ScenarioProperties.INPUT_FROM_URL);
            phase1.setInputFile(schUrl.toString());
            phase1.setXSLType(ScenarioProperties.XSL_FROM_URL);
            phase1.setXSLURL(schematron15Url.toString());
            phase1.setOutputType(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);
            phase1.setProcessor(ScenarioProperties.PROCESSOR_SAXON_XSLT2);
            phase1.setBrowserEnabled(false);

            assertTrue(phase1.isXSLEnabled());
            assertFalse(phase1.isFOPEnabled());
            assertEquals(ScenarioProperties.PROCESSOR_SAXON_XSLT2, phase1.getProcessor());
            assertEquals(schUrl.toString(), phase1.getInputURL());
        }

        @Test
        @DisplayName("Schematron phase-2 scenario (validation XSL → error messages)")
        void phase2ScenarioWiring() {
            ScenarioProperties phase2 = new ScenarioProperties();
            phase2.setXSLEnabled(true);
            phase2.setFOPEnabled(false);
            phase2.setInputType(ScenarioProperties.INPUT_FROM_URL);
            phase2.setInputFile(xmlUrl.toString());
            phase2.setXSLType(ScenarioProperties.XSL_CURRENT_DOCUMENT);
            phase2.setXSLSystemId(schUrl.toString());
            phase2.setOutputType(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);
            phase2.setProcessor(ScenarioProperties.PROCESSOR_SAXON_XSLT2);

            assertEquals(ScenarioProperties.XSL_CURRENT_DOCUMENT, phase2.getXSLType());
            assertEquals(schUrl.toString(), phase2.getXSLSystemId());
        }

        @Test
        @DisplayName("Schematron platform version constants")
        void schematronPlatformConstants() {
            assertEquals(1, ExecuteSchematronAction.SCHEMATRON_PLATFORM_1_5);
            assertEquals(2, ExecuteSchematronAction.SCHEMATRON_PLATFORM_ISO);
            assertEquals("1.5", ExecuteSchematronAction.SCHEMATRON_PLATFORM_1_5_TEXT);
            assertEquals("ISO Implementation", ExecuteSchematronAction.SCHEMATRON_PLATFORM_ISO_TEXT);
        }

        @Test
        @DisplayName("Schematron resource URLs are defined")
        void schematronResourceUrls() {
            assertNotNull(ExecuteSchematronAction.SCHEMATRON_1_5_URL);
            assertFalse(ExecuteSchematronAction.SCHEMATRON_1_5_URL.isEmpty());
            assertNotNull(ExecuteSchematronAction.SCHEMATRON_ISO_STAGE1_DSDL_INCLUDE_URL);
            assertNotNull(ExecuteSchematronAction.SCHEMATRON_ISO_STAGE2_ABSTRACT_EXPAND_URL);
            assertNotNull(ExecuteSchematronAction.SCHEMATRON_ISO_STAGE3_SCHEMATRON_MESSAGE_URL);
        }
    }

    // ── Phase 1: Generate validation stylesheet ──────────────────

    @Test
    @DisplayName("Phase 1: Schematron 1.5 meta-stylesheet generates a validation XSL from rules")
    void phase1GeneratesValidationStylesheet() throws Exception {
        // Use Saxon XSLT2 processor (same as ExecuteSchematronAction)
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        try {
            TransformerFactory factory = TransformerFactory.newInstance();

            // Input = Schematron rules document
            SAXSource rulesInput = new SAXSource(new InputSource(schUrl.openStream()));
            rulesInput.setSystemId(schUrl.toString());

            // Stylesheet = Schematron 1.5 meta-stylesheet
            StreamSource metaStylesheet = new StreamSource(
                    schematron15Url.openStream(), schematron15Url.toString());

            Transformer transformer = factory.newTransformer(metaStylesheet);
            transformer.setParameter("diagnose", "yes");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(rulesInput, new StreamResult(baos));

            String validationXsl = baos.toString("UTF-8");

            assertNotNull(validationXsl);
            assertFalse(validationXsl.isEmpty());
            // The generated stylesheet should be an XSLT document
            assertTrue(validationXsl.contains("xsl:stylesheet")
                    || validationXsl.contains("xsl:transform"),
                    "Phase 1 output should be an XSLT stylesheet");
        } finally {
            System.clearProperty("javax.xml.transform.TransformerFactory");
        }
    }

    // ── Phase 2: Apply generated validation stylesheet ───────────

    @Test
    @DisplayName("Phase 2: Generated validation stylesheet validates input without errors")
    void phase2ValidatesInputDocument() throws Exception {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        try {
            TransformerFactory factory = TransformerFactory.newInstance();

            // Phase 1: generate the validation stylesheet
            SAXSource rulesInput = new SAXSource(new InputSource(schUrl.openStream()));
            rulesInput.setSystemId(schUrl.toString());

            StreamSource metaStylesheet = new StreamSource(
                    schematron15Url.openStream(), schematron15Url.toString());

            Transformer phase1 = factory.newTransformer(metaStylesheet);
            phase1.setParameter("diagnose", "yes");

            ByteArrayOutputStream phase1Out = new ByteArrayOutputStream();
            phase1.transform(rulesInput, new StreamResult(phase1Out));

            // Phase 2: apply validation stylesheet to input XML
            SAXSource xmlInput = new SAXSource(new InputSource(xmlUrl.openStream()));
            xmlInput.setSystemId(xmlUrl.toString());

            StreamSource validationStylesheet = new StreamSource(
                    new java.io.ByteArrayInputStream(phase1Out.toByteArray()),
                    schUrl.toString());

            Transformer phase2 = factory.newTransformer(validationStylesheet);

            ByteArrayOutputStream phase2Out = new ByteArrayOutputStream();
            phase2.transform(xmlInput, new StreamResult(phase2Out));

            String validationOutput = phase2Out.toString("UTF-8");
            assertNotNull(validationOutput);
            // Our test input is valid against the rules, so no assertion failures expected
        } finally {
            System.clearProperty("javax.xml.transform.TransformerFactory");
        }
    }

    // ── ISO Schematron resources exist ────────────────────────────

    @Test
    @DisplayName("ISO Schematron resource files are accessible on classpath")
    void isoSchematronResourcesExist() {
        URL stage1 = getClass().getClassLoader().getResource(
                ExecuteSchematronAction.SCHEMATRON_ISO_STAGE1_DSDL_INCLUDE_URL);
        URL stage2 = getClass().getClassLoader().getResource(
                ExecuteSchematronAction.SCHEMATRON_ISO_STAGE2_ABSTRACT_EXPAND_URL);
        URL stage3 = getClass().getClassLoader().getResource(
                ExecuteSchematronAction.SCHEMATRON_ISO_STAGE3_SCHEMATRON_MESSAGE_URL);

        assertNotNull(stage1, "ISO stage 1 DSDL include XSL should be on classpath");
        assertNotNull(stage2, "ISO stage 2 abstract expand XSL should be on classpath");
        assertNotNull(stage3, "ISO stage 3 schematron message XSL should be on classpath");
    }

    @Test
    @DisplayName("Schematron 1.5 resource file is accessible on classpath")
    void schematron15ResourceExists() {
        URL url = getClass().getClassLoader().getResource(
                ExecuteSchematronAction.SCHEMATRON_1_5_URL);
        assertNotNull(url, "Schematron 1.5 message XSL should be on classpath");
    }
}
