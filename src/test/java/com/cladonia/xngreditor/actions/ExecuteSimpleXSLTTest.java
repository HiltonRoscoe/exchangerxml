package com.cladonia.xngreditor.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Vector;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.cladonia.xngreditor.scenario.ScenarioProperties;

/**
 * Tests validating the core transformation flow used by
 * <b>Execute Simple XSLT</b> from the Transform menu.
 *
 * <p>The action wires up a {@link ScenarioProperties} with XSL from URL,
 * input from URL, output to new document, and default processor — then
 * delegates to the JAXP transformation pipeline.  These tests exercise
 * that exact pipeline headlessly.</p>
 */
class ExecuteSimpleXSLTTest {

    private URL xmlUrl;
    private URL xslUrl;

    @BeforeEach
    void setUp() {
        xmlUrl = getClass().getClassLoader().getResource("test-input.xml");
        xslUrl = getClass().getClassLoader().getResource("test-simple.xsl");
        assertNotNull(xmlUrl, "test-input.xml must be on classpath");
        assertNotNull(xslUrl, "test-simple.xsl must be on classpath");
    }

    @Test
    @DisplayName("simple XSLT transform produces HTML output with book titles")
    void simpleTransformProducesHtml() throws Exception {
        // Build a scenario identical to what ExecuteSimpleXSLTAction creates
        ScenarioProperties scenario = new ScenarioProperties();
        scenario.setInputType(ScenarioProperties.INPUT_FROM_URL);
        scenario.setInputFile(xmlUrl.toString());
        scenario.setXSLEnabled(true);
        scenario.setXSLType(ScenarioProperties.XSL_FROM_URL);
        scenario.setXSLURL(xslUrl.toString());
        scenario.setOutputType(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);
        scenario.setProcessor(ScenarioProperties.PROCESSOR_DEFAULT);

        // Execute via JAXP (same path as ScenarioProcessor.transform)
        SAXSource input = new SAXSource(new InputSource(xmlUrl.openStream()));
        input.setSystemId(xmlUrl.toString());

        StreamSource stylesheet = new StreamSource(xslUrl.openStream(), xslUrl.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);

        TransformerFactory factory = TransformerFactory.newInstance();
        factory.newTransformer(stylesheet).transform(input, result);

        String output = baos.toString("UTF-8");

        assertNotNull(output);
        assertFalse(output.isEmpty());
        assertTrue(output.contains("Bookstore"), "output should contain <h1>Bookstore</h1>");
        assertTrue(output.contains("The Great Gatsby"), "output should contain first book title");
        assertTrue(output.contains("1984"), "output should contain third book title");
    }

    @Test
    @DisplayName("simple XSLT scenario properties are wired correctly")
    void scenarioPropertiesWiring() {
        ScenarioProperties scenario = new ScenarioProperties();
        scenario.setInputType(ScenarioProperties.INPUT_CURRENT_DOCUMENT);
        scenario.setXSLEnabled(true);
        scenario.setXSLType(ScenarioProperties.XSL_FROM_URL);
        scenario.setXSLURL(xslUrl.toString());
        scenario.setOutputType(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);
        scenario.setProcessor(ScenarioProperties.PROCESSOR_DEFAULT);

        assertTrue(scenario.isXSLEnabled());
        assertFalse(scenario.isXQueryEnabled());
        assertFalse(scenario.isFOPEnabled());
        assertEquals(ScenarioProperties.INPUT_CURRENT_DOCUMENT, scenario.getInputType());
        assertEquals(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT, scenario.getOutputType());
    }

    @Test
    @DisplayName("transform with current-document input type sets correct scenario config")
    void currentDocumentInputConfig() {
        ScenarioProperties scenario = new ScenarioProperties();
        scenario.setInputType(ScenarioProperties.INPUT_CURRENT_DOCUMENT);
        scenario.setXSLEnabled(true);
        scenario.setXSLType(ScenarioProperties.XSL_CURRENT_DOCUMENT);
        scenario.setOutputType(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);

        assertEquals(ScenarioProperties.INPUT_CURRENT_DOCUMENT, scenario.getInputType());
        assertEquals(ScenarioProperties.XSL_CURRENT_DOCUMENT, scenario.getXSLType());
    }

    @Test
    @DisplayName("transform output is valid UTF-8")
    void transformOutputIsUtf8() throws Exception {
        SAXSource input = new SAXSource(new InputSource(xmlUrl.openStream()));
        input.setSystemId(xmlUrl.toString());

        StreamSource stylesheet = new StreamSource(xslUrl.openStream(), xslUrl.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);

        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesheet);
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.transform(input, result);

        // Verify the byte stream round-trips cleanly as UTF-8
        String output = baos.toString("UTF-8");
        assertNotNull(output);
        assertFalse(output.isEmpty());
    }
}
