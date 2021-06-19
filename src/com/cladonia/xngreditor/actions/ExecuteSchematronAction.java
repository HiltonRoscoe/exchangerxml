/*
 * $Id: ExecuteSimpleXSLTAction.java,v 1.2 2005/06/01 15:15:34 gmcgoldrick Exp $
 *
 * Copyright (C) 2002, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xngreditor.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXParseException;

import com.cladonia.xml.schematron.ExecuteSchematronDialog;
import com.cladonia.xml.transform.ScenarioUtilities;
import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.scenario.ScenarioProperties;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xngreditor.OpenDocument;
import com.cladonia.xml.ExchangerDocument;
/**
 * An action that can be used to open a XML Document.
 *
 * @version	$Revision: 1.2 $, $Date: 2005/06/01 15:15:34 $
 * @author Dogsbay
 */
public class ExecuteSchematronAction extends AbstractAction {
 	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//private static final boolean DEBUG = false;
	
	private ExchangerEditor parent = null;
	
	private ExecuteSchematronDialog dialog = null;
	
	private int schematronPlatformVersion = SCHEMATRON_PLATFORM_SCHXSLT;
	
	public static final int SCHEMATRON_PLATFORM_SCHXSLT = 1;
	public static final int SCHEMATRON_PLATFORM_ISO = 2;
	
	public static final String SCHEMATRON_PLATFORM_SCHXSLT_TEXT = "SchXslt";
	public static final String SCHEMATRON_PLATFORM_ISO_TEXT = "ISO Implementation";
	
	public static final String SCHEMATRON_SCHXSLT_URL = "com/cladonia/xml/schematron/resources/implementationSchxslt2/compile-for-svrl.xsl";
	
	/*public static final String SCHEMATRON_ISO_STAGE1_DSDL_INCLUDE_URL = "com/cladonia/xml/schematron/resources/implementationISO/iso_dsdl_include.xsl";
	public static final String SCHEMATRON_ISO_STAGE2_ABSTRACT_EXPAND_URL = "com/cladonia/xml/schematron/resources/implementationISO/iso_abstract_expand.xsl";
	public static final String SCHEMATRON_ISO_STAGE3_SCHEMATRON_MESSAGE_URL = "com/cladonia/xml/schematron/resources/implementationISO/iso_schematron_message.xsl";*/
	
	public static final String SCHEMATRON_ISO_STAGE1_DSDL_INCLUDE_URL = "com/cladonia/xml/schematron/resources/implementationISONew/iso_dsdl_include.xsl";
	public static final String SCHEMATRON_ISO_STAGE2_ABSTRACT_EXPAND_URL = "com/cladonia/xml/schematron/resources/implementationISONew/iso_abstract_expand.xsl";
	public static final String SCHEMATRON_ISO_STAGE3_SCHEMATRON_MESSAGE_URL = "com/cladonia/xml/schematron/resources/implementationISONew/iso_schematron_message_xslt2.xsl";
	
 	/**
	 * The constructor for the action which allows opening 
	 * of XML Documents.
	 *
	 * @param parent the parent frame.
	 */
 	public ExecuteSchematronAction( ExchangerEditor parent) {
 		super( "Execute Schematron ...");

		this.parent = parent;

		putValue( MNEMONIC_KEY, new Integer( 'S'));
		putValue( SHORT_DESCRIPTION, "Execute Schematron");
 	}
 	
 	/**
 	 * The implementation of the execute XSLT action.
 	 *
 	 * @param the action event.
 	 */
 	public void actionPerformed( ActionEvent event) {
		
		if(dialog == null) {
			dialog = new ExecuteSchematronDialog(parent);
		}
		
		dialog.show(parent.getDocument());
		
		if(!dialog.isCancelled()) {
        	
        	parent.setWait( true);
    	 	parent.setStatus( "Executing Schematron ...");

	 		// Run in Thread!!!
	 		Runnable runner = new Runnable() {
	 			public void run()  {
			 		try {
						URL inputURL = null;
						int inputType = -1;
						// set XML Input stuff
						if ( dialog.getInputCurrentButton().isSelected()) {
							inputURL = parent.getDocument().getURL();
							inputType = ScenarioProperties.INPUT_CURRENT_DOCUMENT;
						} else if ( dialog.getInputFromOpenDocumentButton().isSelected()) {
							inputURL = ((OpenDocument)dialog.getInputFromOpenDocumentBox().getSelectedItem()).getDocument().getURL();
							inputType = ScenarioProperties.INPUT_FROM_URL;
						} else {
							inputURL = new URL(dialog.getInputLocationField().getText());
							inputType = ScenarioProperties.INPUT_FROM_URL;
						}
						
						URL schematronRulesURL = null;
						int schematronRulesType = -1;
						// set XML Input stuff
						if ( dialog.getSchematronRulesCurrentButton().isSelected()) {
							parent.getDocument().getURL();
							schematronRulesType = ScenarioProperties.XSL_CURRENT_DOCUMENT;
						} else if ( dialog.getSchematronRulesFromOpenDocumentButton().isSelected()) {
							schematronRulesURL = ((OpenDocument)dialog.getSchematronRulesFromOpenDocumentBox().getSelectedItem()).getDocument().getURL();
							schematronRulesType = ScenarioProperties.XSL_FROM_URL;
						} else {
							schematronRulesURL = new URL(dialog.getSchematronRulesLocationField().getText());
							schematronRulesType = ScenarioProperties.XSL_FROM_URL;
						}
						
						
						//open input document if its not already open
						if(parent.getDocument() != null) {
							
							String name = parent.getDocument().getURL().toString();
			 				
			 				if ( !inputURL.toString().endsWith( name)) {
			 					// need to do this in a thread.
			 					parent.open( inputURL, null, false);
			 				}
			 				else {
			 					//its already open
			 				}
						}
						else {
							//no document open so open the input
							parent.open( inputURL, null, false);
						}
						
						//schematron platform
						
						if(dialog.getSchematronPlatformUse15Button().isSelected() == true) {
							//default
							//file:///c:/TempSchematron/schematron-message.xsl
							//schematronPlatformURL = ExchangerEditor.getStaticExtensionClassLoader().getResource("com/cladonia/xml/schematron/resources/schematron-message.xsl");
							//XngrImageLoader.get().getImage("com/cladonia/xml/schematron/resources/schematron-message.xsl");
							schematronPlatformVersion = SCHEMATRON_PLATFORM_SCHXSLT;
						}
						else {
							/*String platformLocationField = dialog.getSchematronPlatformLocationField().getText();
							if((platformLocationField != null) && (platformLocationField.length() > 0)) {
								schematronPlatformURL = new URL(platformLocationField);
							}
							else {
								MessageHandler.showError("The schematron platform location field is null of empty, please retry", "Execute Schematron");
							}*/
							schematronPlatformVersion = SCHEMATRON_PLATFORM_ISO;
						}
						
						//if(schematronPlatformURL != null) {
							
						
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								String schematronVersionText = "";
								if(schematronPlatformVersion == SCHEMATRON_PLATFORM_SCHXSLT) {
									schematronVersionText = SCHEMATRON_PLATFORM_SCHXSLT_TEXT;
								}
								else {
									schematronVersionText = SCHEMATRON_PLATFORM_ISO_TEXT;
								}
								
								parent.getOutputPanel().startCheck( "SCHEMATRON", "Schematron Version ["+schematronVersionText+"]...");
							}						
						});
						
						if(schematronPlatformVersion == SCHEMATRON_PLATFORM_SCHXSLT) {
							
							URL schematronPlatformURL = ExchangerEditor.getStaticExtensionClassLoader().getResource(SCHEMATRON_SCHXSLT_URL);
							ScenarioProperties phase1ScenarioProperties = createPhase1ScenarioProperties(schematronRulesURL, schematronPlatformURL, schematronRulesType);
							
							parent.getExecutePreviousXSLTAction().setScenario( phase1ScenarioProperties);
							
							ExchangerDocument phase1ResultDocument = ScenarioUtilities.schematronPhase1Transform( parent, parent.getDocument(), phase1ScenarioProperties);
							
							if(phase1ResultDocument != null) {
								
								ScenarioProperties phase2ScenarioProperties = createPhase2ScenarioProperties( inputURL, schematronRulesURL, inputType);
								
								parent.getExecutePreviousXSLTAction().setScenario( phase2ScenarioProperties);
								
															
								final int errorCounter = ScenarioUtilities.schematronPhase2Transform( parent, getDocumentByInputType(inputURL, inputType), phase1ResultDocument, phase2ScenarioProperties, true);
								
								SwingUtilities.invokeLater(new Runnable() {
	
									@Override
									public void run() {
										// TODO Auto-generated method stub
										if(errorCounter == 0) {
											parent.getOutputPanel().endCheck("SCHEMATRON", "Finished");
										}
										else if(errorCounter == 1){
											parent.getOutputPanel().endCheck("SCHEMATRON", "1 Error");
										}
										else {
											parent.getOutputPanel().endCheck("SCHEMATRON", errorCounter+ " Errors");
										}
									}
									
								});
								
								//parent.getOutputPanel().sortErrorListByLineNumber();
							}
						}
						else if(schematronPlatformVersion == SCHEMATRON_PLATFORM_ISO) {
							URL schematronStage1URL = ExchangerEditor.getStaticExtensionClassLoader().getResource(SCHEMATRON_ISO_STAGE1_DSDL_INCLUDE_URL);
							
							ScenarioProperties phase1ScenarioProperties = createPhase1ScenarioProperties(schematronRulesURL, schematronStage1URL, inputType );
							
							parent.getExecutePreviousXSLTAction().setScenario( phase1ScenarioProperties);
							
							ExchangerDocument schematronDocument = getDocumentByInputType(schematronRulesURL, schematronRulesType);
							if(schematronDocument != null) {
							
							
								ExchangerDocument phase1ResultDocument = ScenarioUtilities.schematronPhase1Transform( parent, schematronDocument, phase1ScenarioProperties);
								
								if(phase1ResultDocument != null) {
									
									URL schematronStage2URL = ExchangerEditor.getStaticExtensionClassLoader().getResource(SCHEMATRON_ISO_STAGE2_ABSTRACT_EXPAND_URL);
									ScenarioProperties phase2ScenarioProperties = createPhase1ScenarioProperties(schematronRulesURL, schematronStage2URL, ScenarioProperties.INPUT_CURRENT_DOCUMENT);
									
									parent.getExecutePreviousXSLTAction().setScenario( phase2ScenarioProperties);
									ExchangerDocument phase2ResultDocument = ScenarioUtilities.schematronPhase1Transform( parent, phase1ResultDocument, phase2ScenarioProperties);
	
									if(phase2ResultDocument != null) {
										URL schematronStage3URL = ExchangerEditor.getStaticExtensionClassLoader().getResource(SCHEMATRON_ISO_STAGE3_SCHEMATRON_MESSAGE_URL);
										ScenarioProperties phase3ScenarioProperties = createPhase1ScenarioProperties(schematronRulesURL, schematronStage3URL, ScenarioProperties.INPUT_CURRENT_DOCUMENT);
										
										parent.getExecutePreviousXSLTAction().setScenario( phase3ScenarioProperties);
										ExchangerDocument phase3ResultDocument = ScenarioUtilities.schematronPhase1Transform( parent, phase2ResultDocument, phase3ScenarioProperties);
										
									
										if(phase3ResultDocument != null) {
											//parent.open(phase3ResultDocument, null);
											
											ScenarioProperties phase4ScenarioProperties = createPhase2ScenarioProperties( inputURL, schematronRulesURL, inputType);
											
											parent.getExecutePreviousXSLTAction().setScenario( phase4ScenarioProperties);
											
																		
											final int errorCounter = ScenarioUtilities.schematronPhase2Transform( parent, getDocumentByInputType(inputURL, inputType), phase3ResultDocument, phase4ScenarioProperties, true);
											
											SwingUtilities.invokeLater(new Runnable() {
				
												@Override
												public void run() {
													// TODO Auto-generated method stub
													if(errorCounter == 0) {
														parent.getOutputPanel().endCheck("SCHEMATRON", "Finished");
													}
													else if(errorCounter == 1){
														parent.getOutputPanel().endCheck("SCHEMATRON", "1 Error");
													}
													else {
														parent.getOutputPanel().endCheck("SCHEMATRON", errorCounter+ " Errors");
													}
												}
												
											});
											
											//parent.getOutputPanel().sortErrorListByLineNumber();
										}
									}
								}
							}
							else {
								MessageHandler.showError("Cannot load the schematron rules document: \n"+schematronRulesURL+", please retry", "Execute Schematron");
							}
						}
						/*}
						else {
							MessageHandler.showError("Cannot load the schematron platform, please retry", "Execute Schematron");
						}*/
						
			 		} catch(MalformedURLException e) {
						e.printStackTrace();
					} catch ( Exception e) {
			 			// This should never happen, just report and continue
	                    MessageHandler.showError( parent, "Cannot Execute Schematron","Execute Schematron Error");
	                    e.printStackTrace();
			 		} finally {
				 		parent.setStatus( "Done");
				 		parent.setWait( false);
			 		}
	 			}
	 		};
	 		
	 		// Create and start the thread ...
	 		Thread thread = new Thread( runner);
	 		thread.start();
				
				
			
		}
		
	}
 	
 	private ExchangerDocument getDocumentByInputType(URL url, int type) {
 		
 		if(type == ScenarioProperties.INPUT_CURRENT_DOCUMENT) {
	 		//open input document if its not already open
			if(parent.getDocument() != null) {
				return(parent.getDocument());
			}
 		}
 		else if(type == ScenarioProperties.INPUT_FROM_URL) {
 			ExchangerDocument document;
			try {
				document = new ExchangerDocument(url);
				document.load();
				
				return(document);
			} catch (SAXParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			
 		}
 		return(null);
 	}
 	
 	private ScenarioProperties createPhase1ScenarioProperties(URL inputURL, URL schematronPlatformString, int inputType) {
 		ScenarioProperties properties = new ScenarioProperties();
 		
 		properties.setXSLEnabled( true);
		properties.setFOPEnabled( false);
		
		properties.setInputType( inputType);
		if(inputURL != null) {
			properties.setInputFile( inputURL.toString());
		}
		
			
		// set XSL stuff
		
		properties.setXSLType( ScenarioProperties.XSL_FROM_URL);
		properties.setXSLURL( schematronPlatformString.toString());
		
		
		properties.setOutputType( ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);
		
		properties.setProcessor(ScenarioProperties.PROCESSOR_SAXON_XSLT2);

		properties.setBrowserEnabled( false);
		
 		
 		return(properties);
 	}
 	
 	private ScenarioProperties createPhase2ScenarioProperties(URL inputURL, URL schematronRulesURL, int inputType) {
 		ScenarioProperties properties = new ScenarioProperties();
 		
 		properties.setXSLEnabled( true);
		properties.setFOPEnabled( false);
		
		properties.setInputType( inputType);
		if(inputURL != null) {
			properties.setInputFile( inputURL.toString());
		}
		
		
			
		// set XSL stuff
		
		properties.setXSLType( ScenarioProperties.XSL_CURRENT_DOCUMENT);
		properties.setXSLSystemId(schematronRulesURL.toString());
				
		properties.setOutputType( ScenarioProperties.OUTPUT_TO_NEW_DOCUMENT);
		
		properties.setProcessor(ScenarioProperties.PROCESSOR_SAXON_XSLT2);

		properties.setBrowserEnabled( false);
		
 		
 		return(properties);
 	}
}
