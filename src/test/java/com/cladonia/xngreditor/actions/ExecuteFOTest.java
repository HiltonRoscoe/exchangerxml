package com.cladonia.xngreditor.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.cladonia.xngreditor.scenario.ScenarioProperties;

/**
 * Tests validating the functionality used by <b>Execute FO</b> from the
 * Transform menu.
 *
 * <p>Execute FO either transforms XML→XSL-FO→PDF or directly renders
 * an FO document to PDF/PS/TXT/SVG via Apache FOP.</p>
 */
class ExecuteFOTest {

    private URL xmlUrl;
    private URL foXslUrl;
    private URL foUrl;

    @BeforeEach
    void setUp() {
        xmlUrl = getClass().getClassLoader().getResource("test-input.xml");
        foXslUrl = getClass().getClassLoader().getResource("test-fo-transform.xsl");
        foUrl = getClass().getClassLoader().getResource("test-input.fo");
        assertNotNull(xmlUrl);
        assertNotNull(foXslUrl);
        assertNotNull(foUrl);
    }

    // ── Scenario wiring (FO-specific) ────────────────────────────

    @Nested
    @DisplayName("FOP scenario configuration")
    class FopScenarioConfig {

        @Test
        @DisplayName("FOP scenario with XSLT + PDF output is wired correctly")
        void xsltPlusFopPdfScenario() {
            ScenarioProperties scenario = new ScenarioProperties();
            scenario.setInputType(ScenarioProperties.INPUT_FROM_URL);
            scenario.setInputFile(xmlUrl.toString());
            scenario.setXSLEnabled(true);
            scenario.setXSLType(ScenarioProperties.XSL_FROM_URL);
            scenario.setXSLURL(foXslUrl.toString());
            scenario.setFOPEnabled(true);
            scenario.setFOPType(ScenarioProperties.FOP_TYPE_PDF);
            scenario.setFOPOutputType(ScenarioProperties.FOP_OUTPUT_TO_FILE);
            scenario.setFOPOutputFile("/tmp/output.pdf");

            assertTrue(scenario.isFOPEnabled());
            assertTrue(scenario.isXSLEnabled());
            assertEquals(ScenarioProperties.FOP_TYPE_PDF, scenario.getFOPType());
            assertEquals(ScenarioProperties.FOP_OUTPUT_TO_FILE, scenario.getFOPOutputType());
            assertEquals("/tmp/output.pdf", scenario.getFOPOutputFile());
        }

        @Test
        @DisplayName("FOP scenario for direct FO processing (no XSL)")
        void directFoProcessingScenario() {
            ScenarioProperties scenario = new ScenarioProperties();
            scenario.setInputType(ScenarioProperties.INPUT_FROM_URL);
            scenario.setInputFile(foUrl.toString());
            scenario.setXSLEnabled(false);
            scenario.setFOPEnabled(true);
            scenario.setFOPType(ScenarioProperties.FOP_TYPE_PDF);
            scenario.setFOPOutputType(ScenarioProperties.FOP_OUTPUT_TO_VIEWER);

            assertTrue(scenario.isFOPEnabled());
            assertFalse(scenario.isXSLEnabled());
            assertEquals(ScenarioProperties.FOP_OUTPUT_TO_VIEWER, scenario.getFOPOutputType());
        }

        @Test
        @DisplayName("FOP type constants cover all output formats")
        void fopTypeConstants() {
            assertEquals(0, ScenarioProperties.FOP_TYPE_PDF);
            assertEquals(1, ScenarioProperties.FOP_TYPE_PS);
            assertEquals(2, ScenarioProperties.FOP_TYPE_TXT);
            assertEquals(3, ScenarioProperties.FOP_TYPE_SVG);
        }
    }

    // ── XSLT→FO intermediate transformation ──────────────────────

    @Test
    @DisplayName("XSLT produces valid FO output from bookstore XML")
    void xsltProducesFoOutput() throws Exception {
        SAXSource input = new SAXSource(new InputSource(xmlUrl.openStream()));
        input.setSystemId(xmlUrl.toString());

        StreamSource stylesheet = new StreamSource(foXslUrl.openStream(), foXslUrl.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);

        TransformerFactory factory = TransformerFactory.newInstance();
        factory.newTransformer(stylesheet).transform(input, result);

        String foOutput = baos.toString("UTF-8");

        assertNotNull(foOutput);
        assertFalse(foOutput.isEmpty());
        assertTrue(foOutput.contains("fo:root"), "output must be FO document");
        assertTrue(foOutput.contains("fo:page-sequence"));
        assertTrue(foOutput.contains("Bookstore"));
        assertTrue(foOutput.contains("The Great Gatsby"));
    }

    // ── FOP rendering (direct FO to PDF) ─────────────────────────

    @Test
    @DisplayName("FOP renders direct FO document to PDF bytes")
    void fopRendersFoToPdf() throws Exception {
        Driver driver = new Driver();
        driver.setRenderer(Driver.RENDER_PDF);

        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        driver.setOutputStream(pdfOut);

        InputSource foInput = new InputSource(foUrl.openStream());
        foInput.setSystemId(foUrl.toString());
        driver.setInputSource(foInput);
        driver.run();

        byte[] pdfBytes = pdfOut.toByteArray();
        assertTrue(pdfBytes.length > 0, "PDF output should not be empty");
        // PDF files start with %PDF
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
    }

    // ── XSLT→FO→PDF full pipeline ────────────────────────────────

    @Test
    @DisplayName("full XSLT→FO→PDF pipeline produces valid PDF")
    void fullXsltFoPdfPipeline() throws Exception {
        // Step 1: XSLT transform XML to FO
        SAXSource input = new SAXSource(new InputSource(xmlUrl.openStream()));
        input.setSystemId(xmlUrl.toString());

        StreamSource stylesheet = new StreamSource(foXslUrl.openStream(), foXslUrl.toString());

        // Step 2: Pipe XSLT output directly into FOP
        Driver driver = new Driver();
        driver.setRenderer(Driver.RENDER_PDF);

        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        driver.setOutputStream(pdfOut);

        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
        transformer.transform(input, new SAXResult(driver.getContentHandler()));

        byte[] pdfBytes = pdfOut.toByteArray();
        assertTrue(pdfBytes.length > 100, "PDF output should be substantial");
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
    }

    // ── FOP TXT rendering ────────────────────────────────────────

    @Test
    @DisplayName("FOP renders direct FO document to TXT")
    void fopRendersFoToTxt() throws Exception {
        Driver driver = new Driver();
        driver.setRenderer(Driver.RENDER_TXT);

        ByteArrayOutputStream txtOut = new ByteArrayOutputStream();
        driver.setOutputStream(txtOut);

        InputSource foInput = new InputSource(foUrl.openStream());
        foInput.setSystemId(foUrl.toString());
        driver.setInputSource(foInput);
        driver.run();

        String txt = txtOut.toString("UTF-8");
        assertTrue(txt.length() > 0, "TXT output should not be empty");
    }
}
