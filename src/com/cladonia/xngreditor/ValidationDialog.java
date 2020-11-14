/*
 * $Id: ValidationDialog.java,v 1.8 2004/11/04 19:21:49 edankert Exp $
 *
 * Copyright (C) 2002, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xngreditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.bounce.DefaultFileFilter;
import org.bounce.FormLayout;
import org.bounce.QLabel;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xml.XMLGrammar;

/**
 * The grammar properties dialog.
 *
 * @version	$Revision: 1.8 $, $Date: 2004/11/04 19:21:49 $
 * @author Dogsbay
 */
public class ValidationDialog extends JDialog {
	private static final Dimension SIZE = new Dimension( 400, 600);

	private ExchangerEditor parent = null;
	private XMLGrammarImpl grammar	= null;
	
	private boolean cancelled = false;
	
	private JFileChooser grammarChooser = null;

	private DefaultFileFilter xsdFilter = null;
	private DefaultFileFilter dtdFilter = null;
	private DefaultFileFilter rngFilter = null;
	private DefaultFileFilter rncFilter = null;
	private DefaultFileFilter nrlFilter = null;
	
	private JTabbedPane tabPanel = null;

	private QLabel errorLabel	= null;

	private JButton cancelButton	= null;
	private JButton okButton		= null;
	
	private JCheckBox useForCompletionCheck	= null;
	private JCheckBox useForSchemaCheck		= null;

	// VALIDATION
	private JCheckBox xmlValidationLocationCheck	= null;
	private JTextField validationLocationField		= null;
	private JButton validationLocationButton		= null;

	private JComboBox validationGrammarBox			= null;

	/**
	 * The dialog that displays the preferences for the editor.
	 *
	 * @param frame the parent frame.
	 */
	public ValidationDialog( ExchangerEditor parent) {
		this( parent, null);
	}

	/**
	 * The dialog that displays the preferences for the editor.
	 *
	 * @param frame the parent frame.
	 */
	public ValidationDialog( ExchangerEditor parent, String text) {
		super( parent, true);
		
		this.parent = parent;
		
		setResizable( false);
		setTitle( "Set Validation Grammar");
		
		JPanel main = new JPanel( new BorderLayout());
		main.setBorder( new EmptyBorder( 5, 5, 5, 5));
		
		// >>> GENERAL
		JPanel generalPanel = new JPanel( new FormLayout( 10, 2));
		generalPanel.setBorder( new EmptyBorder( 5, 5, 5, 5));
		
		if ( text != null) {
			JPanel textPanel = new JPanel( new BorderLayout());
			JPanel errorPanel = new JPanel( new FormLayout( 10, 2));
			errorLabel = new QLabel( "test");
			errorLabel.setLines( 2);
			errorLabel.setHorizontalAlignment( QLabel.LEFT);

			JLabel resolutionLabel = new JLabel( text);
			
			errorPanel.add( errorLabel, FormLayout.FULL_FILL);
			errorPanel.add( resolutionLabel, FormLayout.FULL_FILL);
			
			textPanel.add( errorPanel, BorderLayout.CENTER);
			
			JLabel iconLabel = new JLabel( UIManager.getIcon("OptionPane.informationIcon"));
			textPanel.add( iconLabel, BorderLayout.WEST);
			iconLabel.setBorder( new EmptyBorder( 2, 20, 2, 20));
			iconLabel.setVerticalAlignment( QLabel.TOP);
	
			generalPanel.add( textPanel, FormLayout.FULL_FILL);
			generalPanel.add( Box.createVerticalStrut(10), FormLayout.FULL_FILL);
		}

		// >>> Validation
		JPanel validationPanel = new JPanel( new FormLayout( 10, 2));
		validationPanel.setBorder( new CompoundBorder( 
									new TitledBorder( "Validation"),
									new EmptyBorder( 0, 5, 5, 5)));

		// grammar
//		ButtonGroup group = new ButtonGroup();
//		dtdCheck = new JRadioButton( "DTD");
//		dtdCheck.setFont( dtdCheck.getFont().deriveFont( Font.PLAIN));
//		group.add( dtdCheck);
//
//		xsdCheck = new JRadioButton( "XSD");
//		xsdCheck.setFont( xsdCheck.getFont().deriveFont( Font.PLAIN));
//		group.add( xsdCheck);
//		
//		rngCheck = new JRadioButton( "RNG");
//		rngCheck.setFont( rngCheck.getFont().deriveFont( Font.PLAIN));
//		group.add( rngCheck);
//
//		rncCheck = new JRadioButton( "RNC");
//		rncCheck.setFont( rncCheck.getFont().deriveFont( Font.PLAIN));
//		group.add( rncCheck);
//
//		nrlCheck = new JRadioButton( "NRL");
//		nrlCheck.setFont( nrlCheck.getFont().deriveFont( Font.PLAIN));
//		group.add( nrlCheck);

		validationGrammarBox = new JComboBox();
		
		validationGrammarBox.addItem( "Document Type Definition");
		validationGrammarBox.addItem( "XML Schema");
		validationGrammarBox.addItem( "RelaxNG");
		validationGrammarBox.addItem( "RelaxNG Compact Syntax");
		validationGrammarBox.addItem( "Namespace Routing Language");
//		validationGrammarBox.addItem( "Schematron");
		
		validationGrammarBox.setSelectedIndex( 0);

//		JPanel grammarPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0));
//		grammarPanel.add( xsdCheck);
//		grammarPanel.add( dtdCheck);
//		grammarPanel.add( rngCheck);
//		grammarPanel.add( rncCheck);
//		grammarPanel.add( nrlCheck);

		JLabel grammarLabel = new JLabel("Grammar:");
		validationPanel.add( grammarLabel, FormLayout.LEFT);
		validationPanel.add( validationGrammarBox, FormLayout.RIGHT_FILL);
		
		// location
		validationLocationField = new JTextField();

		validationLocationButton = new JButton( "...");
		validationLocationButton.setMargin( new Insets( 0, 10, 0, 10));
		validationLocationButton.setPreferredSize( new Dimension( validationLocationButton.getPreferredSize().width, validationLocationField.getPreferredSize().height));
		validationLocationButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e) {
				validationLocationButtonPressed();
			}
		});
		
		validationGrammarBox.setPreferredSize( new Dimension( validationGrammarBox.getPreferredSize().width, validationLocationField.getPreferredSize().height));
		validationGrammarBox.setFont( validationGrammarBox.getFont().deriveFont( Font.PLAIN));
		if ( text == null) {
			validationGrammarBox.addItemListener( new ItemListener() {
				public void itemStateChanged( ItemEvent event) {
					int index = validationGrammarBox.getSelectedIndex();

					useForSchemaCheck.setEnabled( index == 1);
					useForSchemaCheck.setSelected( index == 1);

					if ( index <= 3 && index >= 0) {
						useForCompletionCheck.setEnabled( true);
						useForCompletionCheck.setSelected( true);
					} else {
						useForCompletionCheck.setSelected( false);
						useForCompletionCheck.setEnabled( false);
					}
				}
			});
		}

		JPanel locPanel = new JPanel( new BorderLayout());
		locPanel.add( validationLocationField, BorderLayout.CENTER);
		locPanel.add( validationLocationButton, BorderLayout.EAST);

		JLabel validationLocationLabel = new JLabel( "Location:");
		validationPanel.add( validationLocationLabel, FormLayout.LEFT);

		validationPanel.add( locPanel, FormLayout.RIGHT_FILL);

		xmlValidationLocationCheck = createPlainCheckBox( "Location defined in Document");
		xmlValidationLocationCheck.addItemListener( new ItemListener() {
			public void itemStateChanged( ItemEvent event) {
//				dtdCheck.setEnabled( !xmlValidationLocationCheck.isSelected());
//				xsdCheck.setEnabled( !xmlValidationLocationCheck.isSelected());
//				rngCheck.setEnabled( !xmlValidationLocationCheck.isSelected());
//				rncCheck.setEnabled( !xmlValidationLocationCheck.isSelected());
//				nrlCheck.setEnabled( !xmlValidationLocationCheck.isSelected());
				
				if ( useForSchemaCheck != null) {
					useForSchemaCheck.setEnabled( !xmlValidationLocationCheck.isSelected());
					useForCompletionCheck.setEnabled( !xmlValidationLocationCheck.isSelected());
				}

				validationGrammarBox.setEnabled( !xmlValidationLocationCheck.isSelected());
				validationLocationField.setEnabled( !xmlValidationLocationCheck.isSelected());
				validationLocationButton.setEnabled( !xmlValidationLocationCheck.isSelected());
			}
		});

		validationPanel.add( xmlValidationLocationCheck, FormLayout.RIGHT_FILL);

		if ( text == null) {
			validationPanel.add( getSeparator(), FormLayout.FULL_FILL);

			useForCompletionCheck = new JCheckBox( "Use for Tag Completion");
			validationPanel.add( useForCompletionCheck, FormLayout.RIGHT);
	
			useForSchemaCheck = new JCheckBox( "Use for Outliner/Schema Viewer");
			validationPanel.add( useForSchemaCheck, FormLayout.RIGHT);
	
			useForSchemaCheck.setEnabled( false);
			useForCompletionCheck.setEnabled( false);
		}

		generalPanel.add( validationPanel, FormLayout.FULL_FILL);
		// <<< Validation
		
		cancelButton = new JButton( "Cancel");
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
		buttonPanel.setBorder( new EmptyBorder( 5, 0, 3, 0));
		buttonPanel.add( okButton);
		buttonPanel.add( cancelButton);

		main.add( generalPanel, BorderLayout.NORTH);
		main.add( buttonPanel, BorderLayout.SOUTH);

		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e) {
				cancelled = true;
				//setVisible(false);
				hide();
			}
		});

		setContentPane( main);
		
		setDefaultCloseOperation( HIDE_ON_CLOSE);
		
		pack();
		setSize( new Dimension( Math.max( SIZE.width, getSize().width), getSize().height));
		setLocationRelativeTo( parent);
	}

	private void validationLocationButtonPressed() {
		JFileChooser chooser = getGrammarFileChooser();
		
		int value = chooser.showOpenDialog( getParent());

		if ( value == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			URL url = null;

			try {
				url = com.cladonia.xml.XngrURLUtilities.getURLFromFile(file);
			} catch ( MalformedURLException x) {
				x.printStackTrace(); // should never happen
			}

			setText( validationLocationField, url.toString());
			
			FileFilter filter = chooser.getFileFilter();
			
			if ( filter instanceof DefaultFileFilter) {
				if ( filter == xsdFilter) {
					validationGrammarBox.setSelectedIndex( 1);
				} else if ( filter == dtdFilter) {
					validationGrammarBox.setSelectedIndex( 0);
				} else if ( filter == rngFilter) {
					validationGrammarBox.setSelectedIndex( 2);
				} else if ( filter == rncFilter) {
					validationGrammarBox.setSelectedIndex( 3);
				} else if ( filter == nrlFilter) {
					validationGrammarBox.setSelectedIndex( 4);
				}
			}
			
		}
	}	

	private void okButtonPressed() {
		grammar.setType( getValidationGrammar());
		
		String validationLocation = validationLocationField.getText();

		if ( !StringUtilities.isEmpty( validationLocation)) {
			grammar.setExternal( !xmlValidationLocationCheck.isSelected());
			grammar.setLocation( validationLocation);
		} else {
			grammar.setExternal( false);
			grammar.setLocation( null);
		}

		cancelled = false;
		//setVisible(false);
		hide();
	}
	
	private int getValidationGrammar() {
		int result = XMLGrammar.TYPE_DTD;
		int index = validationGrammarBox.getSelectedIndex();

		if ( index == 1) {
			result = XMLGrammar.TYPE_XSD;
		} else if ( index == 2) {
			result = XMLGrammar.TYPE_RNG;
		} else if ( index == 3) {
			result = XMLGrammar.TYPE_RNC;
		} else if ( index == 4) {
			result = XMLGrammar.TYPE_NRL;
		}
		
		return result;
	}

	private void setValidationGrammar( int type) {
		if ( type == XMLGrammar.TYPE_XSD) {
			validationGrammarBox.setSelectedIndex( 1);
		} else if ( type == XMLGrammar.TYPE_RNG) {
			validationGrammarBox.setSelectedIndex( 2);
		} else if ( type == XMLGrammar.TYPE_RNC) {
			validationGrammarBox.setSelectedIndex( 3);
		} else if ( type == XMLGrammar.TYPE_NRL) {
			validationGrammarBox.setSelectedIndex( 4);
		} else {
			validationGrammarBox.setSelectedIndex( 0);
		}
	}

	public URL getURL() {
		URL result = null;
		
		if ( !StringUtilities.isEmpty( validationLocationField.getText())) {
			result = URLUtilities.toURL( validationLocationField.getText());
		}

		return result;
	}
	
	 public int getType2() {
		return getValidationGrammar();
	 }
	
	public boolean useForCompletion() {
		if ( useForCompletionCheck.isEnabled()) {
			return useForCompletionCheck.isSelected();
		}
		
		return false;
	}
	 
	public boolean useForSchema() {
		if ( useForSchemaCheck.isEnabled()) {
			return useForSchemaCheck.isSelected();
		}
		
		return false;
	}

	public void show( XMLGrammarImpl grammar) {
		show( grammar, null);
	}

	public void show( XMLGrammarImpl grammar, String error) {
		this.grammar = grammar;
		
		if ( error != null) {	
			errorLabel.setText( error);
		}
		String location = grammar.getLocation();
		
		setValidationGrammar( grammar.getType());
		setText( validationLocationField, location);
		
		if ( location != null && location.length() > 0) {
			xmlValidationLocationCheck.setSelected( !grammar.useExternal());
		} else {
			xmlValidationLocationCheck.setSelected( false);
		}
		
		//super.setVisible(true);
		super.show();
	}

	private void cancelButtonPressed() {
		cancelled = true;
		//setVisible(false);
		hide();
	}

	private JComponent getSeparator() {
		JComponent separator = new JPanel();
		separator.setPreferredSize( new Dimension( 100, 10));

		return separator;
	}

	private JLabel createPlainLabel( String text) {
		JLabel label = new JLabel( text);
		label.setFont( label.getFont().deriveFont( Font.PLAIN));

		return label;
	}

	private JCheckBox createPlainCheckBox( String text) {
		JCheckBox check = new JCheckBox( text);
		check.setFont( check.getFont().deriveFont( Font.PLAIN));

		return check;
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
	
	private File getSelectedChooserFile() {
		ExchangerDocument doc = parent.getDocument();
		URL url = null;
		
		if ( doc != null) {
			url = doc.getURL();
		}

		File file = URLUtilities.toFile( validationLocationField.getText());

		if ( file != null) {
			return file;
		} else if ( url != null && url.getProtocol().equals( "file")) {
			return new File( url.getFile());
		} 
	
		return FileUtilities.getLastOpenedFile();
	}
	
	// The creation of the file chooser causes an inspection of all 
	// the drives, this can take some time so only do this when necessary.
	private JFileChooser getGrammarFileChooser() {
		if ( grammarChooser == null) {
			grammarChooser = FileUtilities.createFileChooser();
			xsdFilter = new DefaultFileFilter( "xsd", "XML Schema Document");
			dtdFilter = new DefaultFileFilter( "dtd", "Document Type Definition");
			rngFilter = new DefaultFileFilter( "rng", "RelaxNG");
			rncFilter = new DefaultFileFilter( "rnc", "RelaxNG Compact Format");
			nrlFilter = new DefaultFileFilter( "nrl", "Namespace Routing Language");

			grammarChooser.addChoosableFileFilter( xsdFilter);
			grammarChooser.addChoosableFileFilter( dtdFilter);
			grammarChooser.addChoosableFileFilter( rngFilter);
			grammarChooser.addChoosableFileFilter( rncFilter);
			grammarChooser.addChoosableFileFilter( nrlFilter);
		} 

		int index = validationGrammarBox.getSelectedIndex();
		
		if ( index == 1) {
			grammarChooser.setFileFilter( xsdFilter);
		} else if ( index == 0) {
			grammarChooser.setFileFilter( dtdFilter);
		} else if ( index == 2) {
			grammarChooser.setFileFilter( rngFilter);
		} else if ( index == 3) {
			grammarChooser.setFileFilter( rncFilter);
		} else if ( index == 4) {
			grammarChooser.setFileFilter( nrlFilter);
		}
		
		grammarChooser.setCurrentDirectory( getSelectedChooserFile());
		grammarChooser.rescanCurrentDirectory();
		
		return grammarChooser;
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
} 
