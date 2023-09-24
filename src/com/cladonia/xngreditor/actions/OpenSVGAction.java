/*
 * $Id: OpenSVGAction.java,v 1.7 2005/08/29 08:32:14 gmcgoldrick Exp $
 *
 * Copyright (C) 2002, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xngreditor.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.SAXParseException;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xml.svg.SVGViewerDialog;
import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.ExchangerView;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xngreditor.XMLDocumentChooserDialog;

/**
 * An action that can be used to open a XML Document.
 *
 * @version	$Revision: 1.7 $, $Date: 2005/08/29 08:32:14 $
 * @author Dogsbay
 */
public class OpenSVGAction extends AbstractAction {
 	private static final boolean DEBUG = false;
	
	private ExchangerEditor parent = null;
	private XMLDocumentChooserDialog chooser = null;
	private SVGViewerDialog dialog = null;

 	/**
	 * The constructor for the action which allows opening 
	 * of XML Documents.
	 *
	 * @param parent the parent frame.
	 */
 	public OpenSVGAction( ExchangerEditor parent) {
 		super( "Show SVG");

		this.parent = parent;

		putValue( MNEMONIC_KEY, new Integer( 'h'));
		putValue( SHORT_DESCRIPTION, "Show a SVG Document");
 	}
 	
 	private SVGViewerDialog getSVGViewerDialog() {
 		if ( dialog == null) {
			dialog = new SVGViewerDialog( parent);

			JSVGCanvas canvas = dialog.getJSVGCanvas();
			canvas.addGVTTreeRendererListener( new GVTTreeRendererListener() {
				public void gvtRenderingPrepare( GVTTreeRendererEvent e) {}
				public void gvtRenderingStarted( GVTTreeRendererEvent e) {}
				public void gvtRenderingCompleted( GVTTreeRendererEvent e) {
					if ( !dialog.isVisible()) {
						dialog.setLocationRelativeTo( parent);
						dialog.setVisible(true);
			
						parent.setWait( false);
						parent.setStatus( "Done");
					}
				}

				public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
			    	parent.setWait( false);
			    	parent.setStatus( "Done");
				}
		
				public void gvtRenderingFailed(GVTTreeRendererEvent e) {
			    	parent.setWait( false);
			    	parent.setStatus( "Done");
				}
			});

			canvas.addGVTTreeBuilderListener( new GVTTreeBuilderListener() {
				public void gvtBuildStarted( GVTTreeBuilderEvent e) {}
				public void gvtBuildCompleted( GVTTreeBuilderEvent e) {}

				public void gvtBuildCancelled( GVTTreeBuilderEvent e) {
			    	parent.setWait( false);
			    	parent.setStatus( "Done");
				}
		
				public void gvtBuildFailed( GVTTreeBuilderEvent e) {
			    	parent.setWait( false);
			    	parent.setStatus( "Done");
				}
			});
 		}
 		
 		return dialog;
 	}
 	
 	/**
 	 * The implementation of the open grammar action.
 	 *
 	 * @param the action event.
 	 */
 	public void actionPerformed( ActionEvent e) {
		if ( chooser == null) {
			chooser = new XMLDocumentChooserDialog( parent, "Open SVG Document", "Specify SVG Document location", parent, true);
		}
		
		ExchangerView view = parent.getView();
		
		if ( view != null) {
			view.updateModel();
		}

		ExchangerDocument document = parent.getDocument();
		
		if ( document != null) {
			chooser.show( document.isSVG());
		} else{
			chooser.show( false);
		}
		
		if ( !chooser.isCancelled()) {
			try {
				if ( chooser.isOpenDocument()) {				  
				  document = chooser.getOpenDocument();				  
				}  
				else if ( !chooser.isCurrentDocument()) {
					URL url = new URL( chooser.getInputLocation());

					document = new ExchangerDocument( url);
					document.load();
				}

				final SVGViewerDialog svgDialog = getSVGViewerDialog();
				final SVGDocument svg = createSVGDocument( document);

				parent.setWait( true);
				parent.setStatus( "Rendering SVG ...");
				
				Runnable runner = new Runnable() {
					public void run()  {
						try {
							SwingUtilities.invokeLater( new Runnable() {
								public void run() {
									svgDialog.showSVGDocument( svg);
								}
							});
						} catch ( Exception e) {
							e.printStackTrace();

							parent.setWait( false);
							parent.setStatus( "Done");
						}
					}
				};
				
				// Create and start the thread ...
				Thread thread = new Thread( runner);
				thread.start();

			} catch ( IOException x) {
				MessageHandler.showError( "Could not create the Document:\n"+chooser.getInputLocation(), "Document Error");
			} catch ( SAXParseException x) {
				MessageHandler.showError( "Could not parse the Document.", x, "Document Error");
			}
		}
 	}
	
	private SVGDocument createSVGDocument( ExchangerDocument document) throws IOException {
		SVGDocument svg = null;
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory( "org.apache.xerces.parsers.SAXParser");
		factory.setValidating( false);
		
		ByteArrayInputStream stream = new ByteArrayInputStream( document.getText().getBytes( document.getJavaEncoding()));
		InputStreamReader reader = new InputStreamReader( stream, document.getJavaEncoding());
		
		String name = "SVG";
		
		if (document.getURL() != null)
		{
		  name = document.getURL().toString();
		
		  svg = factory.createSVGDocument( name, new BufferedReader( reader));
		}
		else
			svg = factory.createSVGDocument( null, new BufferedReader( reader));
		  
		return svg;
	}
}
