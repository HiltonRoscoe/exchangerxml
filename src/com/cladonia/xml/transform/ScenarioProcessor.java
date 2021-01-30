/**
 * $Id: ScenarioProcessor.java,v 1.11 2005/04/11 14:11:11 gmcgoldrick Exp $
 *
 * Copyright (C) 2002, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xml.transform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;
import java.util.Properties;

import javax.swing.JFrame;
import javax.xml.transform.ErrorListener;
//import javax.xml.transform.OutputKeys;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.serialize.MessageEmitter;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.trans.XPathException;

import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.viewer.SecureResourceBundle;
import org.apache.fop.viewer.UserMessage;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.bounce.util.BrowserLauncher;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xml.XMLUtilities;
import com.cladonia.xngreditor.URLUtilities;
import com.cladonia.xngreditor.scenario.ScenarioProperties;

/**
 * Utilities for transforming XML documents.
 *
 * @version	$Revision: 1.11 $, $Date: 2005/04/11 14:11:11 $
 * @author Dogsbay
 */
public class ScenarioProcessor {
	private static final boolean DEBUG = false;

	public static final String TRANSLATION_PATH = "/org/apache/fop/viewer/resources/";

	public SystemLogger logger 					= null;
	public TransformationErrorListener listener	= null;
	
//	private TransformerFactory factory 		= null;
	
	private SAXSource input					= null;
	private Source stylesheet			= null;
	private StreamResult output				= null;

	private ExchangerDocument document		= null;
	private ScenarioProperties scenario		= null;
	
	private MessageEmitter messageEmitter = null;
	
	public ScenarioProcessor( ScenarioProperties scenario, ExchangerDocument current) {
		this.scenario = scenario;
		this.document = current;
		this.listener = new TransformationErrorListener();
		this.logger = new SystemLogger();
	}
	
	public void init() {
		ScenarioUtilities.setProcessor( scenario);
		
//		factory = TransformerFactory.newInstance();
//		factory.setErrorListener( listener);
	}
	
	private TransformerFactory getTransformerFactory() {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setErrorListener( listener);
		factory.setAttribute(
                FeatureKeys.LINE_NUMBERING,
                Boolean.TRUE);
		return factory;
	}
	
	public void setMessageEmitter(MessageEmitter me) {
		this.messageEmitter = me;
	}
	
	public void setErrorListener(ErrorListener errorListener) {
		getTransformerFactory().setErrorListener(errorListener);
	}
	
	
	public void cleanup() {
		logger 		= null;
		listener	= null;
		
		input		= null;
		stylesheet	= null;
		output		= null;

		document	= null;
		scenario	= null;
	}

	public void openInput() throws IOException, SAXException {
		input = ScenarioUtilities.openScenarioInput( document, scenario);
		
		if ( input == null) {
			throw new IOException( "No input defined.");
		}
	}
	
	public void openStylesheet() throws IOException, SAXException, TransformerException {
		if ( scenario.isXSLEnabled()) {
			stylesheet = ScenarioUtilities.openScenarioStylesheet( document, getTransformerFactory(), input, scenario);
			
			if ( scenario.getXSLType() == ScenarioProperties.XSL_USE_PROCESSING_INSTRUCTIONS) {
				// reopen the input.
				input = ScenarioUtilities.reopenScenarioInput( document, scenario, input);
			}

			if ( stylesheet == null) {
				throw new IOException( "No stylesheet defined.");
			}
		} else if ( scenario.isXQueryEnabled()) {
			stylesheet = ScenarioUtilities.openScenarioXQuery( document, scenario);
		}
	}
	
	public void openStylesheet(ExchangerDocument stylesheetDocument) throws IOException, SAXException, TransformerException {
		if ( scenario.isXSLEnabled()) {
			stylesheet = ScenarioUtilities.openScenarioStylesheet( stylesheetDocument, getTransformerFactory(), input, scenario);
			
			if ( scenario.getXSLType() == ScenarioProperties.XSL_USE_PROCESSING_INSTRUCTIONS) {
				// reopen the input.
				input = ScenarioUtilities.reopenScenarioInput( document, scenario, input);
			}

			if ( stylesheet == null) {
				throw new IOException( "No stylesheet defined.");
			}
		} else if ( scenario.isXQueryEnabled()) {
			stylesheet = ScenarioUtilities.openScenarioXQuery( stylesheetDocument, scenario);
		}
	}

	public void execute() throws SAXException, TransformerException, IOException, FOPException {
		if ( !scenario.isFOPEnabled()) {
			output = new StreamResult( new ByteArrayOutputStream());
			
			Vector params = ScenarioUtilities.getParameters( scenario);

			if ( scenario.isXQueryEnabled()) {
				query( input, (StreamSource)stylesheet, output);
			} else {
				transform( input, stylesheet, params, output);
			}
		}
	}
	
	public void executeWithTraceListener(TraceListener traceListener) throws SAXException, TransformerException, IOException, FOPException {
		if ( !scenario.isFOPEnabled()) {
			output = new StreamResult( new ByteArrayOutputStream());
			
			Vector params = ScenarioUtilities.getParameters( scenario);

			if ( scenario.isXQueryEnabled()) {
				query( input, (StreamSource)stylesheet, output);
			} else {
				transform( input, stylesheet, params, output, traceListener);
			}
		}
	}

	public void stop() {
//		System.out.println( "ScenarioProperties.stop()");
		try {
			input.getInputSource().getByteStream().close();
			if ( stylesheet instanceof StreamSource) {
				((StreamSource)stylesheet).getInputStream().close();
			}
			output.getOutputStream().close();
		} catch ( Exception e) {
			// do nothing with the exceptions.
//			e.printStackTrace();
		}
	}
	
	public void save( JFrame parent) throws IOException, SAXException, FOPException, TransformerException {
		if ( scenario.isFOPEnabled()) {
			output = new StreamResult( new ByteArrayOutputStream());
			Vector params = ScenarioUtilities.getParameters( scenario);

			if ( scenario.getFOPOutputType() == ScenarioProperties.FOP_OUTPUT_TO_VIEWER) {
				format( parent, input, stylesheet, params);
			} else {
				format( input, stylesheet, params, output, convertFOPType( scenario.getFOPType()));
		
				ScenarioUtilities.saveFormatOutput( scenario, (ByteArrayOutputStream)output.getOutputStream());
			}
		} else {
			ScenarioUtilities.saveScenarioOutput( scenario, document, input, (ByteArrayOutputStream)output.getOutputStream());
		}
	}

	public void save() throws IOException, SAXException {
		ScenarioUtilities.saveScenarioOutput( scenario, document, input, (ByteArrayOutputStream)output.getOutputStream());
	
	}
	
	public String getOutputText()
	{
	  String text = null;
	  
	  try
	  {
	    text = ((ByteArrayOutputStream)output.getOutputStream()).toString("UTF-8");
	  }
	  catch(Exception ex)
	  {}
	  
	  return text;
	}
	
	public void browse() throws IOException {
		String location = ScenarioUtilities.substituteMacros( input.getSystemId(), scenario.getBrowserURL());

		if ( location != null && location.trim().length() > 0) {
			URL url = URLUtilities.toURL( location);
			
			if ( url != null) {
				BrowserLauncher.openURL( URLUtilities.encodeURL( url.toString()));
			}
		} else {
			location = ScenarioUtilities.substituteMacros( input.getSystemId(), scenario.getOutputFile());

			if ( scenario.getOutputType() == ScenarioProperties.OUTPUT_TO_FILE) {
				ScenarioUtilities.openInBrowser( location, (ByteArrayOutputStream)output.getOutputStream());
			} else {
				ScenarioUtilities.openInBrowser( null, (ByteArrayOutputStream)output.getOutputStream());
			}
		}
	}

	/**
	 * Transforms the Document using the stylesheet and parameters supplied,
	 * puts the result in the OutputStream.
	 *
	 * @param document the XML document source.
	 * @param stylesheet the XSL document source, 
	 *        if the stylesheet is null, it will try to get the stylesheet for the PIs in the XML document.
	 *        if the stylesheet is still not found, the default stylesheet is used!
	 * @param parameters list of parameters.
	 * @param result the output stream result.
	 */
	public void transform( SAXSource document, Source stylesheet, Vector parameters, Result result) throws TransformerException, SAXException, IOException {
	
		if ( stylesheet == null) { // no associated stylesheet in the document, use internal stylesheet!
			stylesheet = ScenarioUtilities.createStylesheetSource();
		}
		
//		Templates templates = getTransformerFactory().newTransformer();
		
		Transformer transformer = getTransformerFactory().newTransformer( stylesheet);
		CatalogResolver resolver = XMLUtilities.getCatalogResolver();

		transformer.setURIResolver(resolver);
		
		if ( parameters != null) {
			for ( int i = 0; i < parameters.size(); i++) {
				String[] parameter = (String[])parameters.elementAt(i);
				transformer.setParameter( parameter[0],	parameter[1]);
			}
		}
		transformer.transform( document, result);
	}
	
	public void transform( SAXSource document, Source stylesheet, Vector parameters, Result result, TraceListener traceListener) throws TransformerException, SAXException, IOException {
		
		if ( stylesheet == null) { // no associated stylesheet in the document, use internal stylesheet!
			stylesheet = ScenarioUtilities.createStylesheetSource();
		}
		
//		Templates templates = getTransformerFactory().newTransformer();
		
		Transformer transformer = getTransformerFactory().newTransformer( stylesheet);
		
		CatalogResolver resolver = XMLUtilities.getCatalogResolver();

		transformer.setURIResolver(resolver);
		
		if ( parameters != null) {
			for ( int i = 0; i < parameters.size(); i++) {
				String[] parameter = (String[])parameters.elementAt(i);
				transformer.setParameter( parameter[0],	parameter[1]);
			}
		}
		if(traceListener != null) {
			
			transformer.setParameter(FeatureKeys.TRACE_LISTENER, traceListener);
			((Controller)transformer).addTraceListener(traceListener);
			((Controller)transformer).setMessageEmitter(messageEmitter);
			 
			
		}		
		
		transformer.transform( document, result);
	}

	/**
	 * Formats the ExchangerDocument using the fo file and parameters supplied,
	 * stores the result in the output file specfied.
	 *
	 * @param document the source document.
	 * @param stylesheet the stylesheet source.
	 * @param parameters list of parameters.
	 * @param output the location of the output file.
	 * @param format the output format: PDF, SVG or TXT.
	 */
	public void format( SAXSource document, Source stylesheet, Vector parameters, StreamResult output, int format) throws TransformerException, SAXException, IOException, FOPException	{
		if (DEBUG) System.out.println( "ScenarioProcessor.format( "+document+", "+stylesheet+", "+parameters+", "+output+", "+format+")");
	
		Driver driver = new Driver();

		MessageHandler.setScreenLogger( logger);

		driver.setLogger( logger);
		driver.setRenderer( format);
		driver.setOutputStream( output.getOutputStream());
		
		if ( stylesheet != null) {
			transform( document, stylesheet, parameters, new SAXResult( driver.getContentHandler()));
		} else { // process fo directly
		    driver.setInputSource( ((SAXSource)input).getInputSource());
			driver.run();
		}
	}

	/**
	 * Formats the ExchangerDocument using the fo file and parameters supplied,
	 * stores the result in the output file specfied.
	 *
	 * @param document the source document.
	 * @param stylesheet the stylesheet source.
	 * @param parameters list of parameters.
	 * @param output the location of the output file.
	 * @param format the output format: PDF, SVG or TXT.
	 */
	public void format( JFrame parent, SAXSource document, Source stylesheet, Vector parameters) throws TransformerException, SAXException, IOException, FOPException	{
		if (DEBUG) System.out.println( "ScenarioProcessor.format( "+document+", "+stylesheet+", "+parameters+")");
	
		Driver driver = new Driver();
		driver.setLogger( logger);

		MessageHandler.setScreenLogger( logger);

		UserMessage.setTranslator( Translator.get().getTranslator());
		
		AWTRenderer renderer = new AWTRenderer( Translator.get().getTranslator());
		PreviewDialog dialog = new PreviewDialog( parent, renderer);
		dialog.setModal( false);
		dialog.validate();
		dialog.setLocationRelativeTo( parent);

		renderer.setProgressListener( dialog);
		renderer.setComponent( dialog);

		driver.setRenderer( renderer);
		
		if ( stylesheet != null) {
			transform( document, stylesheet, parameters, new SAXResult( driver.getContentHandler()));
		} else { // process fo directly
		    driver.setInputSource( ((SAXSource)input).getInputSource());
			driver.run();
		}

		dialog.showPage();
		//dialog.setVisible(true);
		dialog.show();
	}

	public void query( SAXSource input, StreamSource query, StreamResult output) throws TransformerException, IOException {
//		System.out.println( "ScenarioProcessor.query( "+input+", "+query+", "+output+")");
//		System.out.println( "query: "+query.getSystemId());

		InputStreamReader queryReader = new InputStreamReader( query.getInputStream());
		boolean wrap = false;
		
		// >>> start
		Configuration config = new Configuration();
		StaticQueryContext xquery = new StaticQueryContext(config);
		XQueryExpression exp = null;
		
	    try {
	    	exp = xquery.compileQuery( queryReader);
	    } catch ( XPathException err) {
	        throw new TransformerException( err);
	    }
	    
        DynamicQueryContext dynamicContext = new DynamicQueryContext( config);

        if ( input != null) {
	    	dynamicContext.setContextNode( xquery.buildDocument( input));
        }
        
        try {
            // The next line actually executes the query
            SequenceIterator results = exp.iterator( dynamicContext);

            Properties props = new Properties();
//            props.setProperty( OutputKeys.OMIT_XML_DECLARATION, "no");
//            props.setProperty( OutputKeys.INDENT, "yes");

            exp.run( dynamicContext, output, props);
            
//            if (wrap) {
//                DocumentInfo resultDoc = DynamicQueryContext.wrap( results, NamePool.getDefaultNamePool());
//                QueryResult.serialize( resultDoc, output, outputProps);
//                output.getOutputStream().close();
//            } else {
//                PrintWriter writer = new PrintWriter( output.getOutputStream());
//                while ( true) {
//                    Item item = results.next();
//                    
//                    if ( item == null) {
//                    	break;
//                    }
//                    
//                    if ( item instanceof NodeInfo) {
//	                    switch ( ((NodeInfo)item).getNodeKind()) {
//	                        case Type.DOCUMENT:
//	                        case Type.ELEMENT:
//	                            // TODO: this is OK for constructed elements, but
//	                            // if the query retrieves an element from a source doc,
//	                            // we are outputting the whole document.
//	                            QueryResult.serialize((NodeInfo)item, new StreamResult( writer), outputProps);
//	                            writer.println("");
//	                            break;
//	                        default:
//	                            writer.println(item.getStringValue());
//	                    }
//                    }
//                }
//
//                writer.close();
//            }
        } catch (TerminationException err) {
            throw err;
        } catch (TransformerException err) {
            throw new TransformerException("Run-time errors were reported");
        }
	}

	private static class TransformationErrorListener implements ErrorListener {
		public void warning( TransformerException e) {
			System.err.println( "WARNING: "+e.getMessageAndLocation());
		}

		public void error( TransformerException e) {
			System.err.println( "ERROR: "+e.getMessageAndLocation());
		}

		public void fatalError( TransformerException e) {
			System.err.println( "FATAL ERROR: "+e.getMessageAndLocation());
		}
	}

	private static class SystemLogger implements Logger{
	    /** Typecode for debugging messages. */
	    public static final int LEVEL_DEBUG = 0;

	    /** Typecode for informational messages. */
	    public static final int LEVEL_INFO = 1;

	    /** Typecode for warning messages. */
	    public static final int LEVEL_WARN = 2;

	    /** Typecode for error messages. */
	    public static final int LEVEL_ERROR = 3;

	    /** Typecode for fatal error messages. */
	    public static final int LEVEL_FATAL = 4;

	    /** Typecode for disabled log levels. */
	    public static final int LEVEL_DISABLED = 5;

	    private final int m_logLevel;

	    /**
	     * Creates a new ConsoleLogger with the priority set to DEBUG.
	     */
	    public SystemLogger() {
	        this( LEVEL_WARN);
	    }

	    /**
	     * Creates a new ConsoleLogger.
	     * @param logLevel log level typecode
	     */
	    public SystemLogger( final int logLevel ) {
	        m_logLevel = logLevel;
	    }

	    /**
	     * Logs a debugging message.
	     *
	     * @param message a <code>String</code> value
	     */
	    public void debug( final String message ) {
	        debug( message, null );
	    }

	    /**
	     * Logs a debugging message and an exception.
	     *
	     * @param message a <code>String</code> value
	     * @param throwable a <code>Throwable</code> value
	     */
	    public void debug( final String message, final Throwable throwable ) {
	        if( m_logLevel <= LEVEL_DEBUG ) {
	            System.err.print( "[DEBUG] " );
	            System.err.println( message );

	            if( null != throwable ) {
	                throwable.printStackTrace( System.out );
	            }
	        }
	    }

	    /**
	     * Returns <code>true</code> if debug-level logging is enabled, false otherwise.
	     *
	     * @return <code>true</code> if debug-level logging
	     */
	    public boolean isDebugEnabled() {
	        return m_logLevel <= LEVEL_DEBUG;
	    }

	    /**
	     * Logs an informational message.
	     *
	     * @param message a <code>String</code> value
	     */
	    public void info( final String message ) {
	        info( message, null );
	    }

	    /**
	     * Logs an informational message and an exception.
	     *
	     * @param message a <code>String</code> value
	     * @param throwable a <code>Throwable</code> value
	     */
	    public void info( final String message, final Throwable throwable ) {
	        if( m_logLevel <= LEVEL_INFO ) {
	            System.err.print( "[INFO] " );
	            System.err.println( message );

	            if( null != throwable ) {
	                throwable.printStackTrace( System.out );
	            }
	        }
	    }

	    /**
	     * Returns <code>true</code> if info-level logging is enabled, false otherwise.
	     *
	     * @return <code>true</code> if info-level logging is enabled
	     */
	    public boolean isInfoEnabled() {
	        return m_logLevel <= LEVEL_INFO;
	    }

	    /**
	     * Logs a warning message.
	     *
	     * @param message a <code>String</code> value
	     */
	    public void warn( final String message ) {
	        warn( message, null );
	    }

	    /**
	     * Logs a warning message and an exception.
	     *
	     * @param message a <code>String</code> value
	     * @param throwable a <code>Throwable</code> value
	     */
	    public void warn( final String message, final Throwable throwable ) {
	        if( m_logLevel <= LEVEL_WARN ) {
	            System.err.print( "[WARNING] " );
	            System.err.println( message );

	            if( null != throwable ) {
	                throwable.printStackTrace( System.out );
	            }
	        }
	    }

	    /**
	     * Returns <code>true</code> if warn-level logging is enabled, false otherwise.
	     *
	     * @return <code>true</code> if warn-level logging is enabled
	     */
	    public boolean isWarnEnabled() {
	        return m_logLevel <= LEVEL_WARN;
	    }

	    /**
	     * Logs an error message.
	     *
	     * @param message a <code>String</code> value
	     */
	    public void error( final String message ) {
	        error( message, null );
	    }

	    /**
	     * Logs an error message and an exception.
	     *
	     * @param message a <code>String</code> value
	     * @param throwable a <code>Throwable</code> value
	     */
	    public void error( final String message, final Throwable throwable ) {
	        if( m_logLevel <= LEVEL_ERROR ) {
	            System.err.print( "[ERROR] " );
	            System.err.println( message );

	            if( null != throwable ) {
	                throwable.printStackTrace( System.out );
	            }
	        }
	    }

	    /**
	     * Returns <code>true</code> if error-level logging is enabled, false otherwise.
	     *
	     * @return <code>true</code> if error-level logging is enabled
	     */
	    public boolean isErrorEnabled() {
	        return m_logLevel <= LEVEL_ERROR;
	    }

	    /**
	     * Logs a fatal error message.
	     *
	     * @param message a <code>String</code> value
	     */
	    public void fatalError( final String message ) {
	        fatalError( message, null );
	    }

	    /**
	     * Logs a fatal error message and an exception.
	     *
	     * @param message a <code>String</code> value
	     * @param throwable a <code>Throwable</code> value
	     */
	    public void fatalError( final String message, final Throwable throwable ) {
	        if( m_logLevel <= LEVEL_FATAL ) {
	            System.err.print( "[FATAL ERROR] " );
	            System.err.println( message );

	            if( null != throwable ) {
	                throwable.printStackTrace( System.out );
	            }
	        }
	    }

	    public boolean isFatalErrorEnabled() {
	        return m_logLevel <= LEVEL_FATAL;
	    }

	    public Logger getChildLogger( final String name ) {
	        return this;
	    }
	}
	
	private static class ExternalDTDHandler implements DTDHandler {
		private DTDHandler parent = null;
		
		public ExternalDTDHandler( DTDHandler parent) {
			this.parent = parent;
		}
		
		public void notationDecl( String name, String publicId, String systemId) throws SAXException {
			try { 
				parent.notationDecl( name, publicId, systemId);
			} catch ( SAXException e) {
				System.err.println( "ERROR: File "+systemId+" Not Found ");
			}
		}
				  
		public void unparsedEntityDecl( String name, String publicId, String systemId, String notationName) throws SAXException {
			try { 
				parent.unparsedEntityDecl( name, publicId, systemId, notationName);
			} catch ( SAXException e) {
				System.err.println( "ERROR: File "+systemId+" Not Found ");
			}
		}
	}
	
//	private static class ClosableByteArrayOutputStream extends OutputStream {
//		private boolean closed = false;
//		private ByteArrayOutputStream output = null;
//		
//		public ClosableByteArrayOutputStream() {
//			this.output = new ByteArrayOutputStream();
//		}
//		
//		public void write( int b) throws IOException {
//			if ( closed) {
//				throw new IOException( "OutputStream closed!");
//			}
//
//			output.write( b);
//		}
//		
//		public void write( byte[] b, int off, int len) throws IOException {
//			if ( closed) {
//				throw new IOException( "OutputStream closed!");
//			}
//			
//			output.write( b, off, len);
//		}
//		
//		public String toString() {
//			return output.toString();
//		}
//
//		public void close() {
//			closed = true;
//		}
//		
//		public ByteArrayOutputStream getByteArrayOutputStream() {
//			return output;
//		}
//	}

	private static class Translator {
		static Translator translator = null;
		
		public static Translator get() {
			if ( translator == null) {
				translator = new Translator();
			}
			
			return translator;
		}
	
		private SecureResourceBundle getTranslator() {
		    String language = null;

		    try {
		        language = System.getProperty("user.language");
		    } catch(SecurityException se) {
		        // if this is running in a secure place
		    }

		    String path = TRANSLATION_PATH+"resources."+language;

		    InputStream in = null;

		    try {
		        URL url = getClass().getResource(path);

		        /* The following code was added by Alex Alishevskikh [alex@openmechanics.net]
		           to fix for crashes on machines with unsupported user languages */
		    if (url == null) {
		            // if the given resource file not found, the english resource uses as default
		            path = path.substring(0, path.lastIndexOf(".")) + ".en";
		            url = getClass().getResource(path);
		    }

		        in = url.openStream();
		    } catch (Exception ex) {
				ex.printStackTrace();
		    }
			
			SecureResourceBundle bundle = new SecureResourceBundle(in);
		    bundle.setMissingEmphasized(false);

		    return bundle;
		}
	}
	
	private static int convertFOPType( int type) {
		int result = Driver.RENDER_PDF;
		
		if ( type == ScenarioProperties.FOP_TYPE_PDF) {
			result = Driver.RENDER_PDF;
		} else if ( type == ScenarioProperties.FOP_TYPE_PS) {
			result = Driver.RENDER_PS;
		} else if ( type == ScenarioProperties.FOP_TYPE_SVG) {
			result = Driver.RENDER_SVG;
		} else if ( type == ScenarioProperties.FOP_TYPE_TXT) {
			result = Driver.RENDER_TXT;
		}
		
		return result;
	}

//	public static class DummyEntityResolver implements EntityResolver {
//		public InputSource resolveEntity( String publicId, String systemId) throws SAXException, IOException {
//			InputSource result = null; 
//
//			try {
//				URL url = null;
//
//				try {
//					url = new URL( systemId);
//				} catch ( MalformedURLException x) {
//					url = new URL( "file", null, systemId);
//				}
//				
//				result = new InputSource( url.openStream());
//			} catch ( IOException e) {
////				System.out.println( "publicId = "+publicId);
//				result = new InputSource( new StringReader( ""));
////				System.err.println( "ERROR: The system cannot find the file specified \""+systemId+"\"");
//			}
//			
//			return result;
//		}
//	}
} 
