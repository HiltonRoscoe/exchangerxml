/*
 * $Id: SetXMLDoctypeAction.java,v 1.6 2004/10/11 08:38:30 edankert Exp $
 *
 * Copyright (C) 2004, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xngreditor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.ExchangerView;
import com.cladonia.xngreditor.XMLDoctypeDialog;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xml.XMLUtilities;


/**
 * An action that can be used to set the XML DOCTYPE
 *
 * @version	$Revision: 1.6 $, $Date: 2004/10/11 08:38:30 $
 * @author Dogs bay
 */
public class SetXMLDoctypeAction extends AbstractAction {
 	private static final boolean DEBUG = false;
	
 	private ExchangerEditor parent = null;
 	private XMLDoctypeDialog dialog = null;
 	
 	

 	/**
	 * The constructor for the action which sets the XML Doctype
	 *
	 * @param parent the parent frame.
	 */
 	public SetXMLDoctypeAction( ExchangerEditor parent) 
	{
 		super( "Set DOCTYPE Declaration...");

		this.parent = parent;

		putValue( MNEMONIC_KEY, new Integer( 'O'));
		putValue( SHORT_DESCRIPTION, "Set DOCTYPE Declaration");
		
		// initally don't have this option available
		setEnabled( false);
 	}
 	
 	/**
 	 * The implementation of the Set XML DOCTYPE action
 	 *
 	 * @param the action event.
 	 */
 	public void actionPerformed( ActionEvent e) 
	{
 		// make sure the document is up to date
 		final ExchangerView view = parent.getView();
		view.updateModel();

		// get the document
		ExchangerDocument document = parent.getDocument();
		
		if ( document.isError()) {
			MessageHandler.showError( "Please make sure the document is well-formed.", "Parser Error");
			return;
		}

		if (dialog == null)
		{
			dialog = new XMLDoctypeDialog(parent);
		}
				
		if (document != null)
		{
			dialog.show(document);
		}
		
		if (!dialog.isCancelled()) 
		{
			try
			{
				
				// get the element name
				String name = dialog.getName();
				
				// get the keyword (SYSTEM or PUBLIC)
				String type = dialog.getType2();
				
				// get the public identifier
				String publicID = dialog.getPublicID();
				
				// get the system identifier
				String systemID = dialog.getSystemID();
				
				// Need to crete a temp exchangerdocument as if we update the existing one
				// before we set text on the editor, the undo functionality is not available
				// This event modele should be looked at again in future version.
				ExchangerDocument tempDoc =  new ExchangerDocument(document.getText());
				
				// update the dom with the new doctype
				XMLUtilities.setXMLDoctype(tempDoc,name,type,publicID,systemID);
				
				// update the text in this temp doc
				tempDoc.update();
				
				// get the text
				final String updatedText = tempDoc.getText();
				
				SwingUtilities.invokeLater( new Runnable(){
					public void run() {
						try{
						parent.switchToEditor();
						view.getEditor().setText(updatedText);
						view.updateModel();
						}
						catch ( Exception e) 
						{
							e.printStackTrace();
						}
					}
				});
			
			} 
			catch ( final Exception err) {
				MessageHandler.showError( err, "Set Doctype Declaration Error");
				err.printStackTrace();
			} 
			catch ( final Throwable t) {
				t.printStackTrace();
			}
		}
		
 	}
} 	