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
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.cladonia.xngreditor.scenario.ParameterProperties;
import com.cladonia.xngreditor.scenario.ScenarioProperties;
import com.cladonia.xngreditor.properties.ConfigurationProperties;

/**
 * Tests validating the core transformation flow used by
 * <b>Execute Advanced XSLT</b> from the Transform menu.
 *
 * <p>Advanced XSLT adds parameters and processor choice on top of simple XSLT.</p>
 */
class ExecuteAdvancedXSLTTest {

    private URL xmlUrl;
    private URL xslUrl;

    @BeforeEach
    void setUp() {
        xmlUrl = getClass().getClassLoader().getResource("test-input.xml");
        xslUrl = getClass().getClassLoader().getResource("test-advanced.xsl");
        assertNotNull(xmlUrl, "test-input.xml must be on classpath");
        assertNotNull(xslUrl, "test-advanced.xsl must be on classpath");
    }

    // ── Helper ────────────────────────────────────────────────────

    private String transform(String processorClass, String[][] params) throws Exception {
        System.setProperty("javax.xml.transform.TransformerFactory", processorClass);
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            SAXSource input = new SAXSource(new InputSource(xmlUrl.openStream()));
            input.setSystemId(xmlUrl.toString());

            StreamSource stylesheet = new StreamSource(xslUrl.openStream(), xslUrl.toString());
            Transformer transformer = factory.newTransformer(stylesheet);

            if (params != null) {
                for (String[] p : params) {
                    transformer.setParameter(p[0], p[1]);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(input, new StreamResult(baos));
            return baos.toString("UTF-8");
        } finally {
            // Reset to default
            System.clearProperty("javax.xml.transform.TransformerFactory");
        }
    }

    // ── Xalan processor ──────────────────────────────────────────

    @Test
    @DisplayName("Xalan processor: transform with no params returns all books")
    void xalanDefaultParams() throws Exception {
        String output = transform(ConfigurationProperties.XSLT_PROCESSOR_XALAN, null);
        assertTrue(output.contains("Book List"), "default heading expected");
        assertTrue(output.contains("The Great Gatsby"));
        assertTrue(output.contains("A Brief History of Time"));
        assertTrue(output.contains("1984"));
    }

    @Test
    @DisplayName("Xalan processor: filterCategory=fiction returns only fiction books")
    void xalanFilterFiction() throws Exception {
        String output = transform(ConfigurationProperties.XSLT_PROCESSOR_XALAN,
                new String[][]{{"filterCategory", "fiction"}});
        assertTrue(output.contains("The Great Gatsby"));
        assertTrue(output.contains("1984"));
        assertFalse(output.contains("A Brief History of Time"));
    }

    @Test
    @DisplayName("Xalan processor: heading parameter overrides default")
    void xalanCustomHeading() throws Exception {
        String output = transform(ConfigurationProperties.XSLT_PROCESSOR_XALAN,
                new String[][]{{"heading", "Custom Title"}});
        assertTrue(output.contains("Custom Title"));
        assertFalse(output.contains("Book List"));
    }

    // ── Saxon XSLT1 processor ────────────────────────────────────

    @Test
    @DisplayName("Saxon XSLT1 processor: transform with filter param")
    void saxonXslt1FilterScience() throws Exception {
        String output = transform(ConfigurationProperties.XSLT_PROCESSOR_SAXON_XSLT1,
                new String[][]{{"filterCategory", "science"}});
        assertTrue(output.contains("A Brief History of Time"));
        assertFalse(output.contains("The Great Gatsby"));
        assertFalse(output.contains("1984"));
    }

    // ── Saxon XSLT2 processor ────────────────────────────────────

    @Test
    @DisplayName("Saxon XSLT2 processor: transform produces valid output")
    void saxonXslt2DefaultParams() throws Exception {
        String output = transform(ConfigurationProperties.XSLT_PROCESSOR_SAXON_XSLT2, null);
        assertTrue(output.contains("Book List"));
        assertTrue(output.contains("The Great Gatsby"));
    }

    @Test
    @DisplayName("Saxon XSLT2 processor: multiple parameters combined")
    void saxonXslt2MultipleParams() throws Exception {
        String output = transform(ConfigurationProperties.XSLT_PROCESSOR_SAXON_XSLT2,
                new String[][]{
                        {"filterCategory", "fiction"},
                        {"heading", "Fiction Only"}
                });
        assertTrue(output.contains("Fiction Only"));
        assertTrue(output.contains("The Great Gatsby"));
        assertFalse(output.contains("A Brief History of Time"));
    }

    // ── Scenario wiring ──────────────────────────────────────────

    @Test
    @DisplayName("advanced XSLT scenario with parameters is wired correctly")
    void advancedScenarioWiring() {
        ScenarioProperties scenario = new ScenarioProperties();
        scenario.setInputType(ScenarioProperties.INPUT_FROM_URL);
        scenario.setInputFile(xmlUrl.toString());
        scenario.setXSLEnabled(true);
        scenario.setXSLType(ScenarioProperties.XSL_FROM_URL);
        scenario.setXSLURL(xslUrl.toString());
        scenario.setOutputType(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);
        scenario.setProcessor(ScenarioProperties.PROCESSOR_XALAN);
        scenario.addParameter(new ParameterProperties("filterCategory", "fiction"));
        scenario.addParameter(new ParameterProperties("heading", "My Books"));

        assertTrue(scenario.isXSLEnabled());
        assertEquals(ScenarioProperties.PROCESSOR_XALAN, scenario.getProcessor());
        assertEquals(2, scenario.getParameters().size());
        assertEquals(ScenarioProperties.XSL_FROM_URL, scenario.getXSLType());
    }

    @Test
    @DisplayName("setProcessor constants map correctly to ScenarioProperties values")
    void processorConstantsMapping() {
        ScenarioProperties s = new ScenarioProperties();

        s.setProcessor(ScenarioProperties.PROCESSOR_DEFAULT);
        assertEquals(0, s.getProcessor());

        s.setProcessor(ScenarioProperties.PROCESSOR_XALAN);
        assertEquals(1, s.getProcessor());

        s.setProcessor(ScenarioProperties.PROCESSOR_SAXON_XSLT1);
        assertEquals(2, s.getProcessor());

        s.setProcessor(ScenarioProperties.PROCESSOR_SAXON_XSLT2);
        assertEquals(3, s.getProcessor());
    }
}
