/*
 * $Id: ScenarioUtilities.java,v 1.14 2005/06/01 15:15:55 gmcgoldrick Exp $
 *
 * Copyright (C) 2002 - 2004, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd. 
 * Use is subject to license terms.
 */

package com.cladonia.xml.transform;

import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.serialize.MessageEmitter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.trace.InstructionInfo;
import net.sf.saxon.lib.TraceListener;

import org.apache.fop.apps.FOPException;
import org.bounce.util.BrowserLauncher;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.ExchangerView;
import com.cladonia.xngreditor.FileUtilities;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xngreditor.StringUtilities;
import com.cladonia.xngreditor.URLChooserDialog;
import com.cladonia.xngreditor.URLUtilities;
import com.cladonia.xngreditor.properties.ConfigurationProperties;
import com.cladonia.xngreditor.scenario.ParameterProperties;
import com.cladonia.xngreditor.scenario.ScenarioProperties;
import com.cladonia.xngreditor.XMLDocumentChooserDialog;
import com.cladonia.xml.XMLUtilities;
import com.cladonia.xml.schematron.SchematronMessageOutputStream;
import com.cladonia.xml.schematron.SchematronTraceListener;


/**
 * This ScenarioUtilities is used to ...
 *
 * @version $Revision: 1.14 $, $Date: 2005/06/01 15:15:55 $
 * @author Dogsbay
 */
public class ScenarioUtilities {
	private static ExecuteScenarioDialog parent = null;
	private static ConfigurationProperties properties = null;
	private static ExchangerEditor editor = null;
	private static URLChooserDialog urlChooser = null;

	private static XMLDocumentChooserDialog chooserXML = null;
	private static XMLDocumentChooserDialog chooserXSL = null;
	private static XMLDocumentChooserDialog chooserXQuery = null;

	
 	private static URLChooserDialog getURLChooser() {
 		if ( urlChooser == null) {
			urlChooser = new URLChooserDialog( parent, "Open URL", "Specify the URL.");
			urlChooser.setLocationRelativeTo( parent);
 		}

 		return urlChooser;
 	}

	private static String selectURL2( String title, String description) {
		URLChooserDialog chooser = getURLChooser();
		chooser.setTitle( title);
		chooser.setDialogDescription( description);
		chooser.setProperties( null);
		chooser.show();
		
		if ( !chooser.isCancelled()) {
			URL url = chooser.getURL();
			return url.toString();
		}
		
		return null;
	}

	private static String selectXMLURL( String title, String description) {
	
	  String url = "";
	  
		if ( chooserXML == null) {
		  chooserXML = new XMLDocumentChooserDialog( parent, title, description, editor, true);
		  chooserXML.setLocationRelativeTo( parent);
		}
		ExchangerDocument document = editor.getDocument();
		
		if ( document != null) {
		  chooserXML.show( document.isXML());
		} else{
		  chooserXML.show( false);
		}
		
		if ( !chooserXML.isCancelled()) {
			try {
				if ( chooserXML.isOpenDocument()) {				  
				  document = chooserXML.getOpenDocument();		
				  url = document.getURL().toString();
				}  
				else if ( !chooserXML.isCurrentDocument()) {
					url = chooserXML.getInputLocation();

				}
				else
				{
				  url = document.getURL().toString();			  
				}
			}
			catch (Exception ex) {}
		}

	
		return url;
	}

	private static String selectXSLURL( String title, String description) {
		
		  String url = "";
	
		  System.out.println("called selectXSLURL");

			if ( chooserXSL == null) {
			  chooserXSL = new XMLDocumentChooserDialog( parent, title, description, editor, true);
			  chooserXSL.setLocationRelativeTo( parent);
			}
			ExchangerDocument document = editor.getDocument();
			
			if ( document != null) {
			  chooserXSL.show( document.isXML());
			} else{
			  chooserXSL.show( false);
			}
			
			if ( !chooserXSL.isCancelled()) {
				try {
					if ( chooserXSL.isOpenDocument()) {				  
					  document = chooserXSL.getOpenDocument();		
					  url = document.getURL().toString();
					}  
					else if ( !chooserXSL.isCurrentDocument()) {
						url = chooserXSL.getInputLocation();

					}
					else
					{
					  url = document.getURL().toString();			  
					}
				}
				catch (Exception ex) {}
			}

			System.out.println("selectXSLURL returned " +  url); 
		
			return url;
		}	

	private static String selectXQueryURL( String title, String description) {
		
		  String url = "";
		  
			if ( chooserXQuery == null) {
			  chooserXQuery = new XMLDocumentChooserDialog( parent, title, description, editor, true);
			  chooserXQuery.setLocationRelativeTo( parent);
			}
			ExchangerDocument document = editor.getDocument();
			
			if ( document != null) {
			  chooserXQuery.show( document.isXML());
			} else{
			  chooserXQuery.show( false);
			}
			
			if ( !chooserXQuery.isCancelled()) {
				try {
					if ( chooserXQuery.isOpenDocument()) {				  
					  document = chooserXQuery.getOpenDocument();		
					  url = document.getURL().toString();
					}  
					else if ( !chooserXQuery.isCurrentDocument()) {
						url = chooserXQuery.getInputLocation();

					}
					else
					{
					  url = document.getURL().toString();			  
					}
				}
				catch (Exception ex) {}
			}

			
			
			return url;
		}	
		
	
	
	public static void init( ExchangerEditor _editor, ConfigurationProperties _properties) {
		properties = _properties;
		editor = _editor;
	}
	
	/**
     * Opens the scenarios input document...
	 */
	public static SAXSource openScenarioInput( ExchangerDocument current, ScenarioProperties properties) throws SAXException, IOException {
		SAXSource result = null;

		switch ( properties.getInputType()) {
			case ScenarioProperties.INPUT_CURRENT_DOCUMENT:

				if ( current == null) {
					String url = selectXMLURL( "Select XML Input", "Specify XML Input Document");

					if ( url != null) {
						result = createInputSource( url);
					}
				} else if ( !current.isError()) {
					result = createInputSource( current);
				} else {
					MessageHandler.showMessage( parent, "Make sure the Document is well-formed.");
				}
				break;
			
			case ScenarioProperties.INPUT_FROM_URL:
				String url = properties.getInputURL();
				
				if ( StringUtilities.isEmpty( (String)url)) {
					url = selectXMLURL( "Select XML Input", "Specify XML Input Document");
				}

				if ( url != null) {
					result = createInputSource( url);
				}
				break;

			case ScenarioProperties.INPUT_PROMPT_FOR_DOCUMENT:
				url = selectXMLURL( "Select XML Input", "Specify XML Input Document");

				if ( url != null) {
					result = createInputSource( url);
				}
				
				break;
		}
		
		return result;
	}

	/**
     * Returns the url for the input ...
	 */
/*
	public static URL resolveInputURL( ScenarioProperties properties) {
		URL result = null;

		switch ( properties.getInputType()) {
			case ScenarioProperties.INPUT_PROMPT_FOR_DOCUMENT:
			case ScenarioProperties.INPUT_CURRENT_DOCUMENT:
				String url = selectURL( "Select XML Input", "Specify XML Input Document");

				if ( url != null) {
					result = URLUtilities.toURL( url);
				}
				break;
			
			
			case ScenarioProperties.INPUT_FROM_URL:
				url = properties.getInputURL();
				
				if ( StringUtilities.isEmpty( (String)url)) {
					url = selectURL( "Select XML Input", "Specify XML Input Document");
				}

				if ( url != null) {
					result = URLUtilities.toURL( url);
				}
				break;
		}
		
		return result;
	}
*/
	/**
     * Resolves the stylesheet url ...
	 */
/*
	public static URL resolveStylesheetURL( ScenarioProperties properties) {
		URL result = null;

		switch ( properties.getXSLType()) {
			case ScenarioProperties.XSL_PROMPT_FOR_DOCUMENT:
			case ScenarioProperties.XSL_USE_PROCESSING_INSTRUCTIONS:
			case ScenarioProperties.XSL_CURRENT_DOCUMENT:
				String url = selectURL( "Select XSL Input", "Specify XSL Stylesheet");
				
				if ( url != null) {
					result = URLUtilities.toURL( url);
				}
				break;
			
			case ScenarioProperties.XSL_FROM_URL:
				url = properties.getXSLURL();
				
				if ( StringUtilities.isEmpty( url)) {
					url = selectURL( "Select XSL Input", "Specify XSL Stylesheet");
				}

				if ( url != null) {
					result = URLUtilities.toURL( url);
				}
				break;
		}
		
		return result;
	}
*/
	public static SAXSource reopenScenarioInput( ExchangerDocument current, ScenarioProperties properties, Source input) throws SAXException, IOException {
		SAXSource result = null;
		String url = input.getSystemId();


		switch ( properties.getInputType()) {
			case ScenarioProperties.INPUT_CURRENT_DOCUMENT:
				if ( current == null) {
					if ( url != null) {
						result = createInputSource( url);
					}
				} else if ( !current.isError()) {
					result = createInputSource( current);
				} else {
					MessageHandler.showMessage( parent, "Make sure the Document is well-formed.");
				}
				break;
			
			case ScenarioProperties.INPUT_PROMPT_FOR_DOCUMENT:
			case ScenarioProperties.INPUT_FROM_URL:
				if ( url != null) {
					result = createInputSource( url);
				}
				break;
		}
		
		return result;
	}

	public static void setProcessor( ScenarioProperties scenario) {
		switch ( scenario.getProcessor()) {
			case ScenarioProperties.PROCESSOR_DEFAULT:
				System.setProperty( "javax.xml.transform.TransformerFactory", properties.getXSLTProcessor());
				break;
			case ScenarioProperties.PROCESSOR_XALAN:
				System.setProperty( "javax.xml.transform.TransformerFactory", ConfigurationProperties.XSLT_PROCESSOR_XALAN);
				break;
			case ScenarioProperties.PROCESSOR_SAXON_XSLT1:
				System.setProperty( "javax.xml.transform.TransformerFactory", ConfigurationProperties.XSLT_PROCESSOR_SAXON_XSLT1);
				break;
			case ScenarioProperties.PROCESSOR_SAXON_XSLT2:
				System.setProperty( "javax.xml.transform.TransformerFactory", ConfigurationProperties.XSLT_PROCESSOR_SAXON_XSLT2);
				break;
		}
	}

	/**
     * Opens the scenario's stylesheet...
	 * TODO: Should be renamed to openTransformationStylesheet.
	 *
	 */
	public static Source openScenarioStylesheet( ExchangerDocument current, TransformerFactory factory, Source input, ScenarioProperties properties) throws TransformerException, IOException, SAXException {
		Source result = null;

		switch ( properties.getXSLType()) {
			case ScenarioProperties.XSL_CURRENT_DOCUMENT:
				if ( current == null) {
					String url = selectXSLURL( "Select XSL Input", "Specify XSL Stylesheet");
					
					if ( url != null) {
						result = createStylesheetSource( url);
					}
				} else if ( !current.isError()) {
					result = createStylesheetSource( current, properties);
				} else {
					MessageHandler.showMessage( parent, "Make sure the Document is well-formed.");
				}
				break;
			
			case ScenarioProperties.XSL_FROM_URL:
				String url = properties.getXSLURL();
				
				if ( StringUtilities.isEmpty( url)) {
					url = selectXSLURL( "Select XSL Input", "Specify XSL Stylesheet");
				}

				if ( url != null) {
					result = createStylesheetSource( url);
				}
				break;

			case ScenarioProperties.XSL_USE_PROCESSING_INSTRUCTIONS:
				result = createPIStylesheetSource( factory, input);
				break;
			
			case ScenarioProperties.XSL_PROMPT_FOR_DOCUMENT:
				url = selectXSLURL( "Select XSL Input", "Specify XSL Stylesheet");

				if ( url != null) {
					result = createStylesheetSource( url);
				}
				break;
		}
		
		return result;
	}
	
	/**
     * Opens the scenario's xquery...
	 * TODO: Should be renamed to openTransformationStylesheet.
	 *
	 */
	public static StreamSource openScenarioXQuery( ExchangerDocument current, ScenarioProperties properties) throws IOException, SAXException {
		System.out.println( "ScenarioUtilities.openScenarioXQuery( "+current+", "+properties+")");
		StreamSource result = null;

		switch ( properties.getXQueryType()) {
			case ScenarioProperties.XQUERY_CURRENT_DOCUMENT:
				if ( current == null) {
					String url = selectXQueryURL( "Select XQuery Input", "Specify XQuery Document");
					
					if ( url != null) {
						result = createStylesheetSource( url);
					}
				} else {
					result = createStylesheetSource( current, properties);
				}
				break;
			
			case ScenarioProperties.XQUERY_FROM_URL:
				String url = properties.getXQueryURL();
				
				if ( StringUtilities.isEmpty( url)) {
					url = selectXQueryURL( "Select XQuery Input", "Specify XQuery Document");
				}

				if ( url != null) {
					result = createStylesheetSource( url);
				}
				break;

			case ScenarioProperties.XQUERY_PROMPT_FOR_DOCUMENT:
				url = selectXQueryURL( "Select XQuery Input", "Specify XQuery Document");

				if ( url != null) {
					result = createStylesheetSource( url);
				}
				break;
		}
		
		return result;
	}

	
	public static String getEncoding( ByteArrayOutputStream output)
	{
	  String encoding = null;
	  
	  ByteArrayInputStream bais = new ByteArrayInputStream(output.toByteArray()); 
	  try
	  {
	  				  Object[] objects = XMLUtilities.preParseXML(new BufferedInputStream(bais));
	  				  encoding = (String)objects[1];
	  				  //System.out.println("Encoding: " + encoding);
	  }
	  catch (Exception ex) {}	  

		if (encoding == null || encoding.equals(""))
		{
		  try
		  {
		    encoding = XMLUtilities.getStreamEncoding(bais);
		  }
		  catch (Exception ex) {}
		}
		
		if (encoding == null || encoding.equals(""))
		  encoding = "UTF-8";

	  return encoding;
	}
	
	
	
	/**
     * Saves the scenario's output stream...
	 */
	public static void saveScenarioOutput( ScenarioProperties properties, final ExchangerDocument current, Source input, final ByteArrayOutputStream output) throws IOException {
		switch ( properties.getOutputType()) {
			case ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT:
				if ( editor != null) {
				  String encoding = getEncoding(output);
				   
				   //System.out.println("Encoding: " + encoding);
					ExchangerDocument document = new ExchangerDocument( output.toString( encoding));

//					SwingUtilities.invokeLater( new Runnable() {
//						public void run() {
//							try {
								open( document);
//							} catch( Exception e) {}
//						}
//					});
					break;
				}

			case ScenarioProperties.OUTPUT_TO_INPUT:
				switch ( properties.getInputType()) {
					case ScenarioProperties.INPUT_CURRENT_DOCUMENT:
						if ( current != null && editor != null) {
							SwingUtilities.invokeLater( new Runnable() {
								public void run() {
									try {
										ExchangerView view = editor.getView( current);

										if ( view != null) {
											editor.switchToEditor();
											  String encoding = getEncoding(output);
											  view.getEditor().setText( output.toString( encoding));
											editor.setView( view);
										}
									} catch( Exception e) {}
								}
							});
							break;
						}
					case ScenarioProperties.INPUT_FROM_URL:
					case ScenarioProperties.INPUT_PROMPT_FOR_DOCUMENT:
						FileOutputStream fileOutput = null;
						File file = URLUtilities.toFile( input.getSystemId());

						if ( file != null) {
							fileOutput = new FileOutputStream( file);
							
							// Save to file...
							output.writeTo( fileOutput);
						} else {
							throw new IOException( "Could not write to file!");
						}
						break;
				}
				break;

			case ScenarioProperties.OUTPUT_PROMPT_FOR_FILE:
			case ScenarioProperties.OUTPUT_TO_FILE:
				String location = substituteMacros( input.getSystemId(), properties.getOutputFile());
				
				File file = null;

				if ( StringUtilities.isEmpty( location) || (properties.getOutputType() == ScenarioProperties.OUTPUT_PROMPT_FOR_FILE)) {
					file = FileUtilities.selectOutputFile( (File)null, (String)null);

					if ( file == null) {
						return;
					}
				} else {
					file = new File( location);
				}

				FileOutputStream fileOutput = null;

				fileOutput = new FileOutputStream( file);
				
				// Save to file...
				output.writeTo( fileOutput);

				fileOutput.flush();
				fileOutput.close();
				break;
			
		}
	}
	
	public static String substituteMacros( String inputLocation, String outputLocation) {
		if ( !StringUtilities.isEmpty( inputLocation)) {
			String path = URLUtilities.getPath( inputLocation);
			String file = URLUtilities.getFileNameWithoutExtension( inputLocation);
			String extension = URLUtilities.getExtension( inputLocation);
			
			outputLocation = StringUtilities.replace( outputLocation, "${path}", path);
			outputLocation = StringUtilities.replace( outputLocation, "${file}", file);
			outputLocation = StringUtilities.replace( outputLocation, "${ext}", extension);
		}
		
		return outputLocation;
	}
	
	/**
     * Saves the scenario's output stream...
	 */
	public static void saveFormatOutput( ScenarioProperties properties, final ByteArrayOutputStream output) throws IOException {
		String location = properties.getFOPOutputFile();
		File file = null;

		if ( StringUtilities.isEmpty( location) || (properties.getFOPOutputType() == ScenarioProperties.FOP_OUTPUT_PROMPT_FOR_FILE)) {
			file = FileUtilities.selectOutputFile( (File)null, (String)null);

			if ( file == null) {
				return;
			}
		} else {
			file = new File( location);
		}

		FileOutputStream fileOutput = null;

		fileOutput = new FileOutputStream( file);
		
		// Save to file...
		output.writeTo( fileOutput);

		fileOutput.flush();
		fileOutput.close();
	}

	private static ExecuteScenarioDialog getExecuteScenarioDialog() {
		if ( parent == null) {
			parent = new ExecuteScenarioDialog( properties);
			parent.setLocationRelativeTo( editor);
		}
		
		return parent;
	}
	
	public static void execute( ExchangerDocument document, ScenarioProperties scenario) {
		ExecuteScenarioDialog dialog = getExecuteScenarioDialog();
		dialog.execute( scenario, document);
	}
	
	
	public static ExchangerDocument schematronPhase1Transform(ExchangerEditor editor, ExchangerDocument document, ScenarioProperties scenario) {
		
		ScenarioProcessor processor = new ScenarioProcessor( scenario, document);
		processor.init();

		ExchangerDocument outputDocument = null;
		try {
			try {
				
				processor.openInput();
			} catch ( SAXException e) {
				throw e;
			} catch ( IOException e) {
				throw e;
			} catch ( Exception e) {
				throw e;
			}
			
			
			try {
				
				processor.openStylesheet();
				processor.execute();
				
			} catch ( TransformerException e) {
				throw e;
			} catch ( SAXException e) {
				throw e;
			//} catch ( FOPException e) {
			//	scenarioModel.setState( 1, scenarioModel.STATUS_ERROR);
			//	throw e;
			} catch ( IOException e) {
				throw e;
			} catch ( Exception e) {
				throw e;
			}
			
			try {
				outputDocument = new ExchangerDocument(processor.getOutputText());
							
			} catch ( Exception e) {
				throw e;
			}
		} catch ( IOException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( FOPException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( SAXException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( TransformerException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( Exception e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} finally {
			processor.cleanup();
			processor = null;
			System.gc();
		}
		
		return(outputDocument); 		
	}

	//return number of errors
	public static int schematronPhase2Transform(ExchangerEditor editor, ExchangerDocument inputDocument, ExchangerDocument stylesheetDocument, ScenarioProperties scenario, boolean listenToErrors) {
		
		int returnNumber = 0;
		
		ScenarioProcessor processor = new ScenarioProcessor( scenario, inputDocument);
		processor.init();

		try {
			try {
				
				processor.openInput();
				
			} catch ( SAXException e) {
				throw e;
			} catch ( IOException e) {
				throw e;
			} catch ( Exception e) {
				throw e;
			}
			
			SchematronTraceListener schematronTraceListener = null;
			//OutputStream messageOutputStream = null;
			SchematronMessageOutputStream messageOutputStream = null;
			
			if(listenToErrors == true) {
				
				schematronTraceListener = new SchematronTraceListener(editor);
				//messageOutputStream = new ByteArrayOutputStream();
				messageOutputStream = new SchematronMessageOutputStream(editor, schematronTraceListener, scenario.getInputURL());
				MessageEmitter me = new MessageEmitter();
				//me.setWriter(messageOutputStream);
				me.setOutputStream(messageOutputStream);
				
				Properties props = new Properties(); 
				props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
				me.setOutputProperties(props);
				
				schematronTraceListener.setMessageEmitter(me);
				
				schematronTraceListener.setMessageOutputStream(messageOutputStream);

				
				processor.setMessageEmitter(me);
				//processor.executeWithTraceListener(traceListener);
				
			}
			
			
			try {
				
				processor.openStylesheet(stylesheetDocument);
				
				if(listenToErrors == true) {
					processor.executeWithTraceListener(schematronTraceListener);
				}
				else {
					processor.execute();
				}
				
			} catch ( TransformerException e) {
				throw e;
			} catch ( SAXException e) {
				throw e;
			//} catch ( FOPException e) {
			//	scenarioModel.setState( 1, scenarioModel.STATUS_ERROR);
			//	throw e;
			} catch ( IOException e) {
				throw e;
			} catch ( Exception e) {
				throw e;
			}
			
			try {

				ExchangerDocument outputDocument = new ExchangerDocument(processor.getOutputText());
				//processor.save(outputDocument);
				outputDocument = new ExchangerDocument(processor.getOutputText());
				
				if((listenToErrors == true) && (messageOutputStream != null)) {
					System.out.println("messages: "+(messageOutputStream).toString());
				}
				returnNumber = schematronTraceListener.getErrorCounter();
					
			} catch ( Exception e) {
				throw e;
			}
		} catch ( IOException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( FOPException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( SAXException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( TransformerException e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} catch ( Exception e) {
			System.err.println( "FATAL ERROR: "+e.getMessage());
			e.printStackTrace();
		} finally {
			processor.cleanup();
			processor = null;
			System.gc();
		}
		
		return(returnNumber); 		
	}

	public static void openInBrowser( String outputSystemId, final ByteArrayOutputStream output) throws IOException {
		// System.out.println( "ScenarioUtilities.openInBrowser( "+systemId+", "+output+")");
		
		File file = null;

		if ( !StringUtilities.isEmpty( outputSystemId)) {
			file = URLUtilities.toFile( outputSystemId);

			if ( !outputSystemId.toLowerCase().endsWith( "htm") && !outputSystemId.toLowerCase().endsWith( "html")) {
				if ( !file.isDirectory()) {
					file = file.getParentFile();
				}
				
				try {
					file = File.createTempFile( "temp", ".htm", file);
					file.deleteOnExit();
				} catch ( IOException e) { 
					// could not create file, try in temp dir...
					file = File.createTempFile( "temp", ".htm", new File( System.getProperty( "java.io.tmpdir")));
					file.deleteOnExit();
				}
			}
		} else {
			file = File.createTempFile( "temp", ".htm", new File( System.getProperty( "java.io.tmpdir")));
			file.deleteOnExit();
		}
	
		FileOutputStream outputFile = new FileOutputStream( file);
		output.writeTo( outputFile);
		outputFile.flush();
		outputFile.close();
		
		URL url = com.cladonia.xml.XngrURLUtilities.getURLFromFile(file);
		URL newUrl = new URL( url.getProtocol(), "localhost", url.getFile());
		BrowserLauncher.openURL( URLUtilities.encodeURL( newUrl.toString()));
	}

	public static Vector getParameters( ScenarioProperties scenario) {
		Vector paramprops = scenario.getParameters();
		Vector parameters = new Vector();

		for ( int i = 0; i < paramprops.size(); i++) {
			ParameterProperties properties = (ParameterProperties)paramprops.elementAt(i);

			String[] parameter = new String[2];
			parameter[0] = properties.getName();
			parameter[1] = properties.getValue();
			
			parameters.addElement( parameter);
		}
		
		return parameters;
	}

	private static void open( ExchangerDocument document) {
		if ( editor != null) {
			editor.open( document, null);
		}
	}

	private static SAXSource createInputSource( String systemId) throws UnsupportedEncodingException, SAXException, IOException {
		InputStream stream = URLUtilities.open( URLUtilities.toURL(systemId));
		
		return createInputSource( stream, systemId);
	}

	private static SAXSource createInputSource( ExchangerDocument document) throws UnsupportedEncodingException, SAXException {
		ByteArrayInputStream stream = new ByteArrayInputStream( document.getText().getBytes( document.getJavaEncoding()));

		URL url = document.getURL();
		String systemId = null;

		if ( url != null) {
			systemId = url.toString();
		} 

		return createInputSource( stream, systemId);
	}

	private static SAXSource createInputSource( InputStream stream, String systemId) throws UnsupportedEncodingException, SAXException {
		InputSource input = new InputSource( stream);
		SAXSource source = new SAXSource( input);

		input.setSystemId( systemId);
		source.setSystemId( systemId);

		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setFeature( "http://xml.org/sax/features/validation", false);
		reader.setEntityResolver( XMLUtilities.getCatalogResolver());
		source.setXMLReader( reader);
		
		return source;
	}
	
	private static Source createPIStylesheetSource( TransformerFactory factory, Source input) throws IOException, SAXException, TransformerException {
		String systemId = URLUtilities.encodeURL( input.getSystemId());
		((SAXSource)input).setSystemId( systemId);

		return factory.getAssociatedStylesheet( input, null, null, null);
	}

	public static StreamSource createStylesheetSource() throws UnsupportedEncodingException, SAXException {
		return new StreamSource( ScenarioUtilities.class.getResourceAsStream( "com/cladonia/xml/transform/resources/default.xsl"));
	}

	private static StreamSource createStylesheetSource( ExchangerDocument document, ScenarioProperties properties) throws UnsupportedEncodingException, SAXException {
		ByteArrayInputStream stream = new ByteArrayInputStream( document.getText().getBytes( document.getJavaEncoding()));

		URL url = document.getURL();
		String systemId = null;

		if ( url != null) {
			systemId = url.toString();
		} 
		else {
			if(properties.getXSLSystemId() != null) {
				systemId = properties.getXSLSystemId();
			}
		}

		return createStylesheetSource( stream, systemId);
	}

	private static StreamSource createStylesheetSource( String systemId) throws UnsupportedEncodingException, SAXException, IOException {
		InputStream stream = URLUtilities.open( URLUtilities.toURL(systemId));
		
		return createStylesheetSource( stream, systemId);
	}

	private static StreamSource createStylesheetSource( InputStream stream, String systemId) throws UnsupportedEncodingException, SAXException {
		return new StreamSource( stream, systemId);
	}
}
