package com.cladonia.xngreditor.actions;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import com.cladonia.xngreditor.scenario.ScenarioProperties;

/**
 * Tests validating the XQuery execution flow used by
 * <b>Execute XQuery</b> from the Transform menu.
 *
 * <p>Execute XQuery uses Saxon's XQuery engine — the same path as
 * {@code ScenarioProcessor.query()}.</p>
 */
class ExecuteXQueryTest {

    private URL xmlUrl;
    private URL xqUrl;

    @BeforeEach
    void setUp() {
        xmlUrl = getClass().getClassLoader().getResource("test-input.xml");
        xqUrl = getClass().getClassLoader().getResource("test-query.xq");
        assertNotNull(xmlUrl, "test-input.xml must be on classpath");
        assertNotNull(xqUrl, "test-query.xq must be on classpath");
    }

    // ── Scenario wiring ──────────────────────────────────────────

    @Nested
    @DisplayName("XQuery scenario configuration")
    class XQueryScenarioConfig {

        @Test
        @DisplayName("XQuery scenario is wired with correct types")
        void xqueryScenarioWiring() {
            ScenarioProperties scenario = new ScenarioProperties();
            scenario.setInputType(ScenarioProperties.INPUT_FROM_URL);
            scenario.setInputFile(xmlUrl.toString());
            scenario.setXSLEnabled(false);
            scenario.setXQueryEnabled(true);
            scenario.setXQueryType(ScenarioProperties.XQUERY_FROM_URL);
            scenario.setXQueryURL(xqUrl.toString());
            scenario.setOutputType(ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);

            assertTrue(scenario.isXQueryEnabled());
            assertFalse(scenario.isXSLEnabled());
            assertFalse(scenario.isFOPEnabled());
            assertEquals(ScenarioProperties.XQUERY_FROM_URL, scenario.getXQueryType());
            assertEquals(xqUrl.toString(), scenario.getXQueryURL());
        }

        @Test
        @DisplayName("XQuery current-document type")
        void xqueryCurrentDocument() {
            ScenarioProperties scenario = new ScenarioProperties();
            scenario.setXQueryEnabled(true);
            scenario.setXQueryType(ScenarioProperties.XQUERY_CURRENT_DOCUMENT);
            assertEquals(ScenarioProperties.XQUERY_CURRENT_DOCUMENT, scenario.getXQueryType());
        }

        @Test
        @DisplayName("XQuery prompt-for-document type")
        void xqueryPromptForDocument() {
            ScenarioProperties scenario = new ScenarioProperties();
            scenario.setXQueryEnabled(true);
            scenario.setXQueryType(ScenarioProperties.XQUERY_PROMPT_FOR_DOCUMENT);
            assertEquals(ScenarioProperties.XQUERY_PROMPT_FOR_DOCUMENT, scenario.getXQueryType());
        }
    }

    // ── XQuery execution (same path as ScenarioProcessor.query) ──

    @Test
    @DisplayName("XQuery extracts book titles sorted alphabetically")
    void xqueryExtractsTitlesSorted() throws Exception {
        Configuration config = new Configuration();
        StaticQueryContext sqc = new StaticQueryContext(config);

        InputStreamReader queryReader = new InputStreamReader(xqUrl.openStream());
        XQueryExpression exp = sqc.compileQuery(queryReader);

        DynamicQueryContext dqc = new DynamicQueryContext(config);

        SAXSource inputSource = new SAXSource(new InputSource(xmlUrl.openStream()));
        inputSource.setSystemId(xmlUrl.toString());
        dqc.setContextItem(sqc.buildDocument(inputSource).getRootNode());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        exp.run(dqc, result, new Properties());

        String output = baos.toString("UTF-8");

        assertNotNull(output);
        assertFalse(output.isEmpty());
        assertTrue(output.contains("<titles>"), "should have root <titles> element");
        assertTrue(output.contains("1984"));
        assertTrue(output.contains("A Brief History of Time"));
        assertTrue(output.contains("The Great Gatsby"));

        // Verify alphabetical order
        int idx1984 = output.indexOf("1984");
        int idxBrief = output.indexOf("A Brief History");
        int idxGatsby = output.indexOf("The Great Gatsby");
        assertTrue(idx1984 < idxBrief, "1984 should come before Brief History");
        assertTrue(idxBrief < idxGatsby, "Brief History should come before Gatsby");
    }

    @Test
    @DisplayName("XQuery with no context document (standalone query)")
    void standaloneXquery() throws Exception {
        Configuration config = new Configuration();
        StaticQueryContext sqc = new StaticQueryContext(config);

        String query = "xquery version \"1.0\"; <hello>{ 1 + 2 }</hello>";
        XQueryExpression exp = sqc.compileQuery(query);

        DynamicQueryContext dqc = new DynamicQueryContext(config);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        exp.run(dqc, result, new Properties());

        String output = baos.toString("UTF-8");
        assertTrue(output.contains("<hello>3</hello>"));
    }

    @Test
    @DisplayName("invalid XQuery fails to compile")
    void invalidXqueryFails() {
        assertThrows(Exception.class, () -> {
            Configuration config = new Configuration();
            StaticQueryContext sqc = new StaticQueryContext(config);
            sqc.compileQuery("this is not valid xquery!!!");
        });
    }
}
