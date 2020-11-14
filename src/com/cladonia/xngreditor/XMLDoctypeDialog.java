/*
 * $Id: XMLDoctypeDialog.java,v 1.7 2004/10/27 13:26:16 edankert Exp $
 *
 * Copyright (C) 2004, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xngreditor;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JFileChooser;

import org.bounce.FormLayout;

import org.dom4j.DocumentType;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xml.XDocument;
import com.cladonia.xngreditor.MessageHandler;


/**
 * The XML Doctype dialog.
 *
 * @version	$Revision: 1.7 $, $Date: 2004/10/27 13:26:16 $
 * @author Dogs bay
 */
public class XMLDoctypeDialog extends XngrDialog {
	
	private JFrame parent		= null;
	private JButton inputLocationButton = null;
	private JPanel doctypePanel	= null;
	private JTextField systemField = null;
	private JTextField publicField = null;
	private JComboBox typeField = null;
	private ExchangerDocument document	= null;
	private JTextField nameField = null;
	private JLabel publicLabel = null;
	private JLabel systemLabel = null;
	
	private static final String SYSTEM = "SYSTEM";
	private static final String PUBLIC = "PUBLIC";
	private static final String INTERNAL = "INTERNAL";	

	/**
	 * The XML Doctype dialog.
	 *
	 * @param parent the parent frame.
	 */
	public XMLDoctypeDialog( JFrame parent) {
		super( parent, true);
		
		this.parent = parent;
		
		setResizable( false);
		setTitle( "Set Document Type Declaration");
		setDialogDescription( "Specify System and Public ID.");
		
		JPanel main = new JPanel( new BorderLayout());
		main.setBorder( new EmptyBorder( 5, 5, 5, 5));
		
		//removed for xngr-dialog
		/*
		cancelButton = new JButton( "Cancel");
		cancelButton.setMnemonic( 'C');
		cancelButton.setFont( cancelButton.getFont().deriveFont( Font.PLAIN));
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e) {
				cancelButtonPressed();
			}
		});

		okButton = new JButton( "OK");
		okButton.setMnemonic( 'O');
		okButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e) {
				updateButtonPressed();
			}
		});

		getRootPane().setDefaultButton( okButton);

		JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0));
		buttonPanel.setBorder( new EmptyBorder( 10, 0, 3, 0));
		buttonPanel.add( okButton);
		buttonPanel.add( cancelButton);
		*/
		JPanel form = new JPanel( new FormLayout( 10, 2));

		// fill the panel...
		form.add( getDoctypePanel(), FormLayout.FULL_FILL);

		main.add( form, BorderLayout.CENTER);
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
	}
	
	
	/**
	 *  Called when the cancel button is pushed
	 */
	protected void cancelButtonPressed() {
		super.cancelButtonPressed();
	}
	
	/**
	 *  Called when the ok button is pushed, validates that the required text fields have a value
	 */
	protected void okButtonPressed() {
		
		int index = typeField.getSelectedIndex();
		
		switch (index) 
		{
			case 0:
				if ((systemField.getText().equals("")) || (systemField.getText() == null))
				{
					MessageHandler.showError( "Please enter a value for the System ID.", "Set Document Type Declaration Error");
					return;
				}
				
				super.okButtonPressed();
				return;
			case 1:
				
				if (((publicField.getText().equals("")) || (publicField.getText() == null)) && 
					((systemField.getText().equals("")) || (systemField.getText() == null)))
				{
					MessageHandler.showError( "Please enter a value for both the Public ID \n and the System ID", "Set Document Type Declaration Error");
					return;
				}
				
				if ((publicField.getText().equals("")) || (publicField.getText() == null))
				{
					MessageHandler.showError( "Please enter a value for the Public ID.", "Set Document Type Declaration Error");
					return;
				}
				
				if ((systemField.getText().equals("")) || (systemField.getText() == null))
				{
					MessageHandler.showError( "Please enter a value for the System ID.", "Set Document Type Declaration Error");
					return;
				}
				
				super.okButtonPressed();
				return;
			case 2:
				super.okButtonPressed();
				return;
		}
		
		super.okButtonPressed();
	}
	
	private JPanel getDoctypePanel() {
		if ( doctypePanel == null) {
			doctypePanel = new JPanel( new FormLayout( 10, 10));
			doctypePanel.setBorder( new CompoundBorder( 
										new TitledBorder( "Document Type Declaration"),
										new EmptyBorder( 0, 5, 5, 5)));
			nameField = new JTextField();
			doctypePanel.add( new JLabel( "Name:"), FormLayout.LEFT);
			doctypePanel.add( nameField, FormLayout.RIGHT_FILL);
			
			// set the type combo
			typeField = new JComboBox();
			typeField.addItem(SYSTEM);
			typeField.addItem(PUBLIC);
			typeField.addItem(INTERNAL);
			
			typeField.setFont( typeField.getFont().deriveFont( Font.PLAIN));
			typeField.setPreferredSize( new Dimension( typeField.getPreferredSize().width, nameField.getPreferredSize().height));
			typeField.addItemListener(new ItemListener() {
				public void itemStateChanged( ItemEvent event) {
					
					int index = typeField.getSelectedIndex();
					
					switch (index) 
					{
						case 0:
							systemField.setEnabled(true);
							systemLabel.setEnabled(true);
							inputLocationButton.setEnabled(true);
							publicField.setEnabled(false);
							publicLabel.setEnabled(false);
							return;
						case 1:
							systemField.setEnabled(true);
							systemLabel.setEnabled(true);
							inputLocationButton.setEnabled(true);
							publicField.setEnabled(true);
							publicLabel.setEnabled(true);
							return;
						case 2:
							systemField.setEnabled(false);
							systemLabel.setEnabled(false);
							publicField.setEnabled(false);
							publicLabel.setEnabled(false);
							inputLocationButton.setEnabled(false);
							return;
					}
					return;
				}
			});

			doctypePanel.add( new JLabel( "Type:"), FormLayout.LEFT);
			doctypePanel.add( typeField, FormLayout.RIGHT);
			
			publicField  = new JTextField();
			publicLabel = new JLabel( "Public ID:");
			doctypePanel.add( publicLabel, FormLayout.LEFT);
			doctypePanel.add( publicField, FormLayout.RIGHT_FILL);
			
			JPanel locationPanel = new JPanel( new BorderLayout());
			
			systemLabel = new JLabel("System ID:");
			systemField  = new JTextField();
			inputLocationButton = new JButton( "...");
			inputLocationButton.setMargin( new Insets( 0, 10, 0, 10));
			inputLocationButton.setPreferredSize( new Dimension( inputLocationButton.getPreferredSize().width, systemField.getPreferredSize().height));
			inputLocationButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e) {
					inputLocationButtonPressed();
				}
			});

			//locationPanel.add( label, BorderLayout.WEST);
			locationPanel.add( systemField, BorderLayout.CENTER);
			locationPanel.add( inputLocationButton, BorderLayout.EAST);
			
			doctypePanel.add(systemLabel,FormLayout.LEFT);
			doctypePanel.add( locationPanel, FormLayout.RIGHT_FILL);
		}

		return doctypePanel;
	}
	
	/**
	 * Displays the dialog
	 *
	 * @param document The ExchangeDocument
	 */
	public void show(ExchangerDocument document) 
	{
		JPanel form = new JPanel( new FormLayout(10, 2));
		
		this.document = document;
		
		// set the values that the dialog needs to display
		setCurrentValues();
		
		pack();
		setSize( new Dimension( 420, getSize().height));

		setLocationRelativeTo( parent);

		super.show();
	}
		
	/**
	 * Gets the name of the DOCTYPE (should be the root element name) 
	 *
	 * @return String The DOCTYPE name
	 */
	public String getName() 
	{
		return nameField.getText(); 
	}
	
	/**
	 * Gets the DOCTYPE type (SYSTEM,PUBLIC or INTERNAL) 
	 *
	 * @return String The DOCTYPE type
	 */
	public String getType2()
	{
		int index = typeField.getSelectedIndex();
		
		switch (index) 
		{
			case 0:
				return SYSTEM;
			case 1:
				return PUBLIC;
			case 2:
				return INTERNAL;
		}

		return SYSTEM;	
	}
	
	/**
	 * Gets the Public ID 
	 *
	 * @return String The public ID
	 */
	public String getPublicID()
	{
		return publicField.getText();
	}
	
	/**
	 * Gets the System ID 
	 *
	 * @return String The system ID
	 */
	public String getSystemID()
	{
		return systemField.getText();
	}
	
	/**
	 * Sets the values diaplayed by the dialog, plus what should be enabled\disabled
	 */
	private void setCurrentValues()
	{
		XDocument xdoc = document.getDocument();
		DocumentType dt = xdoc.getDocType();
		if (dt == null)
		{	
			// set the defaults
			String name = xdoc.getRootElement().getName();
			nameField.setText(name);
			
			// set the type to be internal
			typeField.setSelectedIndex(2);
			
			publicField.setText("");
			publicField.setEnabled(false);
			publicLabel.setEnabled(false);
			
			systemField.setText("");
			systemField.setEnabled(false);
			systemLabel.setEnabled(false);
			
			inputLocationButton.setEnabled(false);
		}
		else
		{	
			String name = dt.getElementName();
			nameField.setText(name);
			
			String publicId = dt.getPublicID();
			if (publicId != null)
			{
				typeField.setSelectedIndex(1);
				publicField.setText(publicId);
				publicField.setEnabled(true);
				publicLabel.setEnabled(true);
				systemField.setText(dt.getSystemID());
				systemField.setEnabled(true);
				systemLabel.setEnabled(true);
				inputLocationButton.setEnabled(true);
			}
			else
			{
				publicField.setText("");
				publicField.setEnabled(false);
				publicLabel.setEnabled(false);
				String systemId = dt.getSystemID();
				if (systemId != null)
				{
					systemField.setText(systemId);
					systemField.setEnabled(true);
					systemLabel.setEnabled(true);
					typeField.setSelectedIndex(0);
					inputLocationButton.setEnabled(true);
				}
				else
				{	
					systemField.setText("");
					systemField.setEnabled(false);
					systemLabel.setEnabled(false);
					typeField.setSelectedIndex(2);
					inputLocationButton.setEnabled(false);
				}
			}
		}
	}
		
	private void inputLocationButtonPressed() {
		JFileChooser chooser = getInputFileChooser();

		int value = chooser.showOpenDialog( getParent());

		if ( value == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			URL url = null;

			try {
				url = com.cladonia.xml.XngrURLUtilities.getURLFromFile(file);
			} catch ( MalformedURLException x) {
				x.printStackTrace(); // should never happen
			}

			setText( systemField, url.toString());
		}
	}
	
	//	The creation of the file chooser causes an inspection of all 
	// the drives, this can take some time so only do this when necessary.
	private JFileChooser getInputFileChooser() {
		JFileChooser chooser = FileUtilities.getCurrentFileChooser();
		chooser.setFileFilter( chooser.getAcceptAllFileFilter());
		
		File file = URLUtilities.toFile(systemField.getText());

		/*if ( file == null && outputLocationField.isEnabled() && !isEmpty( outputLocationField.getText())) {
			file = new File( outputLocationField.getText());
		}*/
		
		if ( file != null) {
			chooser.setCurrentDirectory( file);
		}

		chooser.rescanCurrentDirectory();
		
		return chooser;
	}
	
	private void setText( JTextField field, String text) {
		field.setText(text);
		field.setCaretPosition(0);
	}

	
}
