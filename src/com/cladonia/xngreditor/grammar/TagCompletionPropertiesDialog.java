/*
 * $Id: TagCompletionPropertiesDialog.java,v 1.5 2004/11/04 19:21:50 edankert Exp $
 *
 * Copyright (C) 2002, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xngreditor.grammar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.bounce.DefaultFileFilter;
import org.bounce.FormConstraints;
import org.bounce.FormLayout;

import com.cladonia.xml.XMLGrammar;
import com.cladonia.xngreditor.FileUtilities;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xngreditor.URLUtilities;
import com.cladonia.xngreditor.XngrDialog;

/**
 * The namespace properties dialog.
 *
 * @version	$Revision: 1.5 $, $Date: 2004/11/04 19:21:50 $
 * @author Dogsbay
 */
public class TagCompletionPropertiesDialog extends XngrDialog {
	private static final Dimension SIZE 					= new Dimension( 400, 120);
	private static final FormConstraints LEFT_ALIGN_RIGHT	= new FormConstraints( FormConstraints.LEFT, FormConstraints.RIGHT);

	private TagCompletionProperties properties	= null;

	//private boolean cancelled = false;
	
	//private JButton cancelButton	= null;
	//private JButton okButton		= null;
	
	private DefaultFileFilter xsdFilter = null;
	private DefaultFileFilter dtdFilter = null;
	private DefaultFileFilter rngFilter = null;
	private DefaultFileFilter rncFilter = null;

	private JFileChooser tagCompletionChooser = null;

	// The components that contain the values
	private JTextField tagCompletionLocationField	= null;
	private JButton tagCompletionLocationButton		= null;

	private JRadioButton xsdTagCompletionCheck	= null;
	private JRadioButton dtdTagCompletionCheck	= null;
	private JRadioButton rngTagCompletionCheck	= null;
	private JRadioButton rncTagCompletionCheck	= null;

	/**
	 * The dialog that displays the preferences for the editor.
	 *
	 * @param frame the parent frame.
	 */
	public TagCompletionPropertiesDialog( JFrame parent) {
		super( parent, true);
		
		setResizable( false);
		setTitle( "Tag Completion Properties");
		setDialogDescription( "Specify a Tag Completion Schema");
		
		JPanel main = new JPanel( new BorderLayout());
		main.setBorder( new EmptyBorder( 5, 5, 5, 5));
		
		// GENERAL
		JPanel generalPanel = new JPanel( new FormLayout( 10, 2));
		generalPanel.setBorder( new EmptyBorder( 5, 5, 15, 5));
	
		// prefix
		ButtonGroup group = new ButtonGroup();

		dtdTagCompletionCheck = new JRadioButton( "DTD");
		dtdTagCompletionCheck.setFont( dtdTagCompletionCheck.getFont().deriveFont( Font.PLAIN));
		group.add( dtdTagCompletionCheck);

		xsdTagCompletionCheck = new JRadioButton( "XSD");
		xsdTagCompletionCheck.setFont( xsdTagCompletionCheck.getFont().deriveFont( Font.PLAIN));
		group.add( xsdTagCompletionCheck);
		
		rngTagCompletionCheck = new JRadioButton( "RNG");
		rngTagCompletionCheck.setFont( rngTagCompletionCheck.getFont().deriveFont( Font.PLAIN));
		group.add( rngTagCompletionCheck);

		rncTagCompletionCheck = new JRadioButton( "RNC");
		rncTagCompletionCheck.setFont( rncTagCompletionCheck.getFont().deriveFont( Font.PLAIN));
		group.add( rncTagCompletionCheck);

		JPanel tagCompletionGrammarPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0));
		tagCompletionGrammarPanel.add( xsdTagCompletionCheck);
		tagCompletionGrammarPanel.add( dtdTagCompletionCheck);
		tagCompletionGrammarPanel.add( rngTagCompletionCheck);
		tagCompletionGrammarPanel.add( rncTagCompletionCheck);

		generalPanel.add( new JLabel( "Grammar:"), FormLayout.LEFT);
		generalPanel.add( tagCompletionGrammarPanel, FormLayout.RIGHT_FILL);
		
		tagCompletionLocationField = new JTextField();

		tagCompletionLocationButton = new JButton( "...");
		tagCompletionLocationButton.setMargin( new Insets( 0, 10, 0, 10));
		tagCompletionLocationButton.setPreferredSize( new Dimension( tagCompletionLocationButton.getPreferredSize().width, tagCompletionLocationField.getPreferredSize().height));
		tagCompletionLocationButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e) {
				tagCompletionLocationButtonPressed();
			}
		});
		
		JPanel tempPanel = new JPanel( new BorderLayout());
		tempPanel.add( tagCompletionLocationField, BorderLayout.CENTER);
		tempPanel.add( tagCompletionLocationButton, BorderLayout.EAST);

		generalPanel.add( new JLabel( "Location:"), FormLayout.LEFT);
		generalPanel.add( tempPanel, FormLayout.RIGHT_FILL);
		
		/*cancelButton = new JButton( "Cancel");
		cancelButton.setMnemonic( 'C');
		cancelButton.setFont( cancelButton.getFont().deriveFont( Font.PLAIN));
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e) {
				cancelButtonPressed();
			}
		});

		okButton = new JButton( "OK");
		okButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e) {
				okButtonPressed();
			}
		});

		getRootPane().setDefaultButton( okButton);

		JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0));
		buttonPanel.setBorder( new EmptyBorder( 10, 0, 3, 0));
		buttonPanel.add( okButton);
		buttonPanel.add( cancelButton);*/

		main.add( generalPanel, BorderLayout.CENTER);
		/*main.add( buttonPanel, BorderLayout.SOUTH);

		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e) {
				cancelled = true;
				hide();
			}
		});
*/
		setContentPane( main);
		
		setDefaultCloseOperation( HIDE_ON_CLOSE);
		
		pack();
		setSize( new Dimension( Math.max( SIZE.width, getSize().width), getSize().height));

		setLocationRelativeTo( parent);
	}
	
	protected void okButtonPressed() {
		String location = tagCompletionLocationField.getText();
		
		if ( checkLocation( location)) {
			
			properties.setLocation( location);
			properties.setType( getType2());
	
			super.okButtonPressed();
		}
	}

	public void show( TagCompletionProperties properties) {
		this.properties = properties;
		
		setText( tagCompletionLocationField, properties.getLocation());
		setType( properties.getType());
		
		super.show();
	}
	
	private int getType2() {
		int result = XMLGrammar.TYPE_DTD;

		if ( xsdTagCompletionCheck.isSelected()) {
			result = XMLGrammar.TYPE_XSD;
		} else if ( rngTagCompletionCheck.isSelected()) {
			result = XMLGrammar.TYPE_RNG;
		} else if ( rncTagCompletionCheck.isSelected()) {
			result = XMLGrammar.TYPE_RNC;
		}
		
		return result;
	}

	private void setType( int type) {
		if ( type == XMLGrammar.TYPE_XSD) {
			xsdTagCompletionCheck.setSelected( true);
		} else if ( type == XMLGrammar.TYPE_RNG) {
			rngTagCompletionCheck.setSelected( true);
		} else if ( type == XMLGrammar.TYPE_RNC) {
			rncTagCompletionCheck.setSelected( true);
		} else {
			dtdTagCompletionCheck.setSelected( true);
		}
	}

	private void tagCompletionLocationButtonPressed() {
		JFileChooser chooser = getTagCompletionFileChooser();
		
		int value = chooser.showOpenDialog( getParent());
	
		if ( value == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			URL url = null;
	
			try {
				url = com.cladonia.xml.XngrURLUtilities.getURLFromFile(file);
			} catch ( MalformedURLException x) {
				x.printStackTrace(); // should never happen
			}
	
			setText( tagCompletionLocationField, url.toString());
		}
	}	

	private JFileChooser getTagCompletionFileChooser() {
		if ( tagCompletionChooser == null) {
			tagCompletionChooser = FileUtilities.createFileChooser();

			if ( xsdFilter == null) {
				xsdFilter = new DefaultFileFilter( "xsd", "XML Schema Document");
			}
			
			if ( dtdFilter == null) {
				dtdFilter = new DefaultFileFilter( "dtd", "Document Type Definition");
			}
			
			if ( rngFilter == null) {
				rngFilter = new DefaultFileFilter( "rng", "RelaxNG");
			}

			if ( rncFilter == null) {
				rncFilter = new DefaultFileFilter( "rnc", "RelaxNG Compact Format");
			}

			tagCompletionChooser.addChoosableFileFilter( xsdFilter);
			tagCompletionChooser.addChoosableFileFilter( dtdFilter);
			tagCompletionChooser.addChoosableFileFilter( rngFilter);
			tagCompletionChooser.addChoosableFileFilter( rncFilter);
		} 
		
		if ( xsdTagCompletionCheck.isSelected()) {
			tagCompletionChooser.setFileFilter( xsdFilter);
		} else if ( dtdTagCompletionCheck.isSelected()) {
			tagCompletionChooser.setFileFilter( dtdFilter);
		} else if ( rngTagCompletionCheck.isSelected()) {
			tagCompletionChooser.setFileFilter( rngFilter);
		} else if ( rncTagCompletionCheck.isSelected()) {
			tagCompletionChooser.setFileFilter( rncFilter);
		}

		File file = URLUtilities.toFile( tagCompletionLocationField.getText());
		
		if ( file != null) {
			tagCompletionChooser.setCurrentDirectory( file);
		}
		
		tagCompletionChooser.rescanCurrentDirectory();
		
		return tagCompletionChooser;
	}

	protected void cancelButtonPressed() {
		super.cancelButtonPressed();
	}

	/**
	 * When the dialog is cancelled and no selection has been made, 
	 * this method returns true.
	 *
	 * @return true when the dialog has been cancelled.
	 */
	public boolean isCancelled() {
		return cancelled;
	}
	
	private void setText( JTextField field, String text) {
		field.setText( text);
		field.setCaretPosition( 0);
	}
	
	protected boolean isEmpty( String string) {
		if ( string != null && string.trim().length() > 0) {
			return false;
		}
		
		return true;
	}

	private boolean checkLocation( String value) {
		if ( isEmpty( value)) {
			MessageHandler.showMessage( "Please specify a Tag Completion Grammar Location.");
			return false;
		}
		
		return true;
	}
} 
