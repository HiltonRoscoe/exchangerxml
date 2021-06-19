package com.cladonia.xml.schematron;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.bounce.FormLayout;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xml.XElement;
import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.ExchangerView;
import com.cladonia.xngreditor.FileUtilities;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xngreditor.OpenDocument;
import com.cladonia.xngreditor.URLUtilities;
import com.cladonia.xngreditor.XngrDialog;

public class ExecuteSchematronDialog extends XngrDialog {

	private static final long serialVersionUID = 1L;

	private ExchangerEditor xngr = null;
	
	private JPanel inputPanel = null;
	private JPanel schematronRulesPanel = null;
	private JPanel schematronPlatformPanel = null;

	private JTextField inputLocationField;

	private JButton inputLocationButton;

	private JRadioButton inputFromURLButton;

	private JRadioButton inputCurrentButton;

	private JRadioButton inputFromOpenDocumentButton;

	private JComboBox inputFromOpenDocumentBox;

	private JTextField schematronRulesLocationField;

	private JButton schematronRulesLocationButton;

	private JRadioButton schematronRulesFromURLButton;
	
	//private JRadioButton schematronPlatformUseDefaultButton;
	private JRadioButton schematronPlatformUse15Button;
	private JRadioButton schematronPlatformUseISOButton;
	
	//private JTextField schematronPlatformLocationField;

	//private JButton schematronPlatformLocationButton;

	//private JRadioButton schematronPlatformFromURLButton;

	private JRadioButton schematronRulesCurrentButton;

	private JRadioButton schematronRulesFromOpenDocumentButton;

	private JComboBox schematronRulesFromOpenDocumentBox;

		private JFileChooser schematronRulesFileChooser;

	private ExchangerDocument document;
	//private JFileChooser xslFileChooser = null;
	
	public ExecuteSchematronDialog(ExchangerEditor xngr) {
		super(xngr, true);
		this.xngr = xngr;
		
		setResizable( false);
		setTitle( "Execute Schematron");
		setDialogDescription( "Select the schematron and xml input documents");
		
		JPanel main = new JPanel( new BorderLayout());
		main.setBorder( new EmptyBorder( 5, 5, 5, 5));
		
		
		
		JPanel form = new JPanel( new FormLayout( 10, 2));

		form.add( getInputPanel(), FormLayout.FULL_FILL);
		form.add( getSchematronRulesPanel(), FormLayout.FULL_FILL);
		form.add( getSchematronPlatformPanel(), FormLayout.FULL_FILL);

		main.add( form, BorderLayout.CENTER);
		/*main.add( buttonPanel, BorderLayout.SOUTH);

		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e) {
				cancelled = true;
				hide();
			}
		});*/

		setContentPane( main);
		
		setDefaultCloseOperation( DO_NOTHING_ON_CLOSE);

		pack();
		setSize( new Dimension( 400, getSize().height));
		
		setLocationRelativeTo( xngr);
	}

	private JPanel getInputPanel() {
		if ( inputPanel == null) {
			inputPanel = new JPanel( new FormLayout( 10, 2));
			inputPanel.setBorder( new CompoundBorder( 
										new TitledBorder( "Input"),
										new EmptyBorder( 0, 5, 5, 5)));

			setInputLocationField(new JTextField());

			inputLocationButton = new JButton( "...");
			inputLocationButton.setMargin( new Insets( 0, 10, 0, 10));
			inputLocationButton.setPreferredSize( new Dimension( inputLocationButton.getPreferredSize().width, getInputLocationField().getPreferredSize().height));
			inputLocationButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e) {
					inputLocationButtonPressed();
				}
			});
			
			getInputLocationField().setEnabled( false);
			inputLocationButton.setEnabled( false);

			JPanel locationPanel = new JPanel( new BorderLayout());

			setInputFromURLButton(new JRadioButton( "From URL:"));
			getInputFromURLButton().setPreferredSize( new Dimension( getInputFromURLButton().getPreferredSize().width, getInputLocationField().getPreferredSize().height));
			getInputFromURLButton().addItemListener( new ItemListener() {
				public void itemStateChanged( ItemEvent event) {
					getInputLocationField().setEnabled( getInputFromURLButton().isSelected());
					inputLocationButton.setEnabled( getInputFromURLButton().isSelected());
				}
			});

			locationPanel.add( getInputLocationField(), BorderLayout.CENTER);
			locationPanel.add( inputLocationButton, BorderLayout.EAST);

			setInputCurrentButton(new JRadioButton( "Current Document"));
			getInputCurrentButton().setPreferredSize( new Dimension( getInputCurrentButton().getPreferredSize().width, getInputLocationField().getPreferredSize().height));

			setInputFromOpenDocumentButton(new JRadioButton( "Open Document"));
			getInputFromOpenDocumentButton().setPreferredSize( new Dimension( getInputFromOpenDocumentButton().getPreferredSize().width, getInputLocationField().getPreferredSize().height));

			setInputFromOpenDocumentBox(new JComboBox());
			//inputFromOpenDocumentBox.setFont( inputFromOpenDocumentBox.getFont().deriveFont( Font.PLAIN));
			getInputFromOpenDocumentBox().setPreferredSize( new Dimension( getInputFromOpenDocumentBox().getPreferredSize().width, getInputLocationField().getPreferredSize().height));
			getInputFromOpenDocumentBox().setEditable(true);
			
			ButtonGroup group = new ButtonGroup();
			group.add( getInputCurrentButton());
			group.add( getInputFromOpenDocumentButton());
			group.add( getInputFromURLButton());
			
			inputPanel.add( getInputCurrentButton(), FormLayout.FULL);
			inputPanel.add( getInputFromOpenDocumentButton(), FormLayout.LEFT);
			inputPanel.add( getInputFromOpenDocumentBox(), FormLayout.RIGHT_FILL);				
			inputPanel.add( getInputFromURLButton(), FormLayout.LEFT);
			inputPanel.add( locationPanel, FormLayout.RIGHT_FILL);
		}

		return inputPanel;
	}
	
	private JPanel getSchematronPlatformPanel() {
		
		if ( schematronPlatformPanel == null) {
			schematronPlatformPanel = new JPanel( new FormLayout( 10, 2));
			schematronPlatformPanel.setBorder( new CompoundBorder( 
										new TitledBorder( "Schematron Platform"),
										new EmptyBorder( 0, 5, 5, 5)));
			//JPanel schematronPlatformLocationPanel = new JPanel( new BorderLayout());
			
			//offer just SchXslt or ISO
			setSchematronPlatformUse15Button(new JRadioButton("Use SchXslt (XSLT 2.0)"));
			setSchematronPlatformUseISOButton(new JRadioButton("Use ISO Implementation"));
			
			getSchematronPlatformUse15Button().setSelected(true);
			getSchematronPlatformUseISOButton().setSelected(false);
			
			schematronPlatformPanel.add( getSchematronPlatformUse15Button(), FormLayout.FULL_FILL);
			schematronPlatformPanel.add( getSchematronPlatformUseISOButton(), FormLayout.FULL_FILL);
			
			ButtonGroup schematronPlatformButtonGroup = new ButtonGroup();
			schematronPlatformButtonGroup.add(getSchematronPlatformUse15Button());
			schematronPlatformButtonGroup.add(getSchematronPlatformUseISOButton());
			
			/*setSchematronPlatformUseDefaultButton(new JRadioButton("Use Default [1.5]"));
			
			
			setSchematronPlatformLocationField(new JTextField());

			schematronPlatformLocationButton = new JButton( "...");
			schematronPlatformLocationButton.setMargin( new Insets( 0, 10, 0, 10));
			schematronPlatformLocationButton.setPreferredSize( new Dimension( schematronPlatformLocationButton.getPreferredSize().width, getSchematronPlatformLocationField().getPreferredSize().height));
			schematronPlatformLocationButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e) {
					schematronPlatformLocationButtonPressed();
				}
			});
			
			getSchematronPlatformLocationField().setEnabled( false);
			schematronPlatformLocationButton.setEnabled( false);

			setSchematronPlatformFromURLButton(new JRadioButton( "From URL:"));
			getSchematronPlatformFromURLButton().setPreferredSize( new Dimension( getSchematronPlatformFromURLButton().getPreferredSize().width, getSchematronPlatformLocationField().getPreferredSize().height));
			getSchematronPlatformFromURLButton().addItemListener( new ItemListener() {
				public void itemStateChanged( ItemEvent event) {
					getSchematronPlatformLocationField().setEnabled( getSchematronPlatformFromURLButton().isSelected());
					schematronPlatformLocationButton.setEnabled( getSchematronPlatformFromURLButton().isSelected());
				}
			});
			
			ButtonGroup schematronPlatformButtonGroup = new ButtonGroup();
			schematronPlatformButtonGroup.add(getSchematronPlatformUseDefaultButton());
			schematronPlatformButtonGroup.add(schematronPlatformFromURLButton);

			schematronPlatformLocationPanel.add( getSchematronPlatformLocationField(), BorderLayout.CENTER);
			schematronPlatformLocationPanel.add( schematronPlatformLocationButton, BorderLayout.EAST);
			
			schematronPlatformPanel.add( getSchematronPlatformUseDefaultButton(), FormLayout.FULL_FILL);
			schematronPlatformPanel.add( getSchematronPlatformFromURLButton(), FormLayout.LEFT);
			schematronPlatformPanel.add( schematronPlatformLocationPanel, FormLayout.RIGHT_FILL);
			
			getSchematronPlatformUseDefaultButton().setSelected(true);
			getSchematronPlatformFromURLButton().setSelected(false);*/
		}
		
		return(schematronPlatformPanel);
	}
	
	private JPanel getSchematronRulesPanel() {
		if ( schematronRulesPanel == null) {
			schematronRulesPanel = new JPanel( new FormLayout( 10, 2));
			schematronRulesPanel.setBorder( new CompoundBorder( 
										new TitledBorder( "Schematron Rules"),
										new EmptyBorder( 0, 5, 5, 5)));
			JPanel schematronRulesLocationPanel = new JPanel( new BorderLayout());

			setSchematronRulesLocationField(new JTextField());

			schematronRulesLocationButton = new JButton( "...");
			schematronRulesLocationButton.setMargin( new Insets( 0, 10, 0, 10));
			schematronRulesLocationButton.setPreferredSize( new Dimension( schematronRulesLocationButton.getPreferredSize().width, getSchematronRulesLocationField().getPreferredSize().height));
			schematronRulesLocationButton.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e) {
					schematronRulesLocationButtonPressed();
				}
			});
			
			getSchematronRulesLocationField().setEnabled( false);
			schematronRulesLocationButton.setEnabled( false);

			setSchematronRulesFromURLButton(new JRadioButton( "From URL:"));
			getSchematronRulesFromURLButton().setPreferredSize( new Dimension( getSchematronRulesFromURLButton().getPreferredSize().width, getSchematronRulesLocationField().getPreferredSize().height));
			getSchematronRulesFromURLButton().addItemListener( new ItemListener() {
				public void itemStateChanged( ItemEvent event) {
					getSchematronRulesLocationField().setEnabled( getSchematronRulesFromURLButton().isSelected());
					schematronRulesLocationButton.setEnabled( getSchematronRulesFromURLButton().isSelected());
				}
			});

			schematronRulesLocationPanel.add( getSchematronRulesLocationField(), BorderLayout.CENTER);
			schematronRulesLocationPanel.add( schematronRulesLocationButton, BorderLayout.EAST);

			setSchematronRulesCurrentButton(new JRadioButton( "Current Document"));
			getSchematronRulesCurrentButton().setPreferredSize( new Dimension( getSchematronRulesCurrentButton().getPreferredSize().width, getSchematronRulesLocationField().getPreferredSize().height));

			setSchematronRulesFromOpenDocumentButton(new JRadioButton( "Open Document"));
			getSchematronRulesFromOpenDocumentButton().setPreferredSize( new Dimension( getSchematronRulesFromOpenDocumentButton().getPreferredSize().width, getInputLocationField().getPreferredSize().height));

			setSchematronRulesFromOpenDocumentBox(new JComboBox());
			//inputFromOpenDocumentBox.setFont( inputFromOpenDocumentBox.getFont().deriveFont( Font.PLAIN));
			getSchematronRulesFromOpenDocumentBox().setPreferredSize( new Dimension( getSchematronRulesFromOpenDocumentBox().getPreferredSize().width, getInputLocationField().getPreferredSize().height));
			getSchematronRulesFromOpenDocumentBox().setEditable(true);
			
			ButtonGroup schematronRulesGroup = new ButtonGroup();
			schematronRulesGroup.add( getSchematronRulesCurrentButton());
			schematronRulesGroup.add( getSchematronRulesFromOpenDocumentButton());
			schematronRulesGroup.add( getSchematronRulesFromURLButton());
			
			schematronRulesPanel.add( getSchematronRulesCurrentButton(), FormLayout.FULL);
			schematronRulesPanel.add( getSchematronRulesFromOpenDocumentButton(), FormLayout.LEFT);
			schematronRulesPanel.add( getSchematronRulesFromOpenDocumentBox(), FormLayout.RIGHT_FILL);				
			schematronRulesPanel.add( getSchematronRulesFromURLButton(), FormLayout.LEFT);
			schematronRulesPanel.add( schematronRulesLocationPanel, FormLayout.RIGHT_FILL);
			
			getSchematronRulesCurrentButton().setSelected( false);
			getSchematronRulesFromOpenDocumentButton().setSelected( false);
			getSchematronRulesFromURLButton().setSelected( true);
			
		}

		return schematronRulesPanel;
	}
	
	private void schematronRulesLocationButtonPressed() {
		JFileChooser chooser = getschematronRulesFileChooser();

		int value = chooser.showOpenDialog( getParent());

		if ( value == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			URL url = null;

			try {
				url = com.cladonia.xml.XngrURLUtilities.getURLFromFile(file);
			} catch ( MalformedURLException x) {
				x.printStackTrace(); // should never happen
			}

			setText( getSchematronRulesLocationField(), url.toString());
		}
	}
	
	/*private void schematronPlatformLocationButtonPressed() {
		JFileChooser chooser = getXSLFileChooser();

		int value = chooser.showOpenDialog( getParent());

		if ( value == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			URL url = null;

			try {
				url = com.cladonia.xml.XngrURLUtilities.getURLFromFile(file);
			} catch ( MalformedURLException x) {
				x.printStackTrace(); // should never happen
			}

			setText( schematronPlatformLocationField, url.toString());
		}
	}*/
	
	// The creation of the file chooser causes an inspection of all 
	// the drives, this can take some time so only do this when necessary.
	/*private JFileChooser getXSLFileChooser() {
		if ( xslFileChooser == null) {
			xslFileChooser = FileUtilities.createFileChooser();

			xslFileChooser.addChoosableFileFilter( new DefaultFileFilter( "xsl", "XSL Document"));
		} 

		/*File file = URLUtilities.toFile( schematronPlatformLocationField.getText());

		if ( file == null && inputLocationField != null && inputLocationField.isEnabled() && inputLocationField.isVisible()) {
			file = URLUtilities.toFile( inputLocationField.getText());
		}*/
		
		/*File file = null;
		
		if ( file == null && schematronRulesLocationField.isEnabled() && !isEmpty( schematronRulesLocationField.getText())) {
			file = new File( schematronRulesLocationField.getText());
		}
		
		if ( file == null) {
			if ( document != null) {
				file = URLUtilities.toFile( document.getURL());
			} 
			
			if ( file == null) {
				file = FileUtilities.getLastOpenedFile();
			}
		}

		xslFileChooser.setCurrentDirectory( file);
		xslFileChooser.rescanCurrentDirectory();
		
		return xslFileChooser;
	}*/
	
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

			setText( getInputLocationField(), url.toString());
		}
	}
	
	// The creation of the file chooser causes an inspection of all 
	// the drives, this can take some time so only do this when necessary.
	private JFileChooser getschematronRulesFileChooser() {
		if ( schematronRulesFileChooser == null) {
			schematronRulesFileChooser = FileUtilities.createFileChooser();

			//schematronRulesFileChooser.addChoosableFileFilter( new DefaultFileFilter( "Schematron Rules", "schematronRules Document"));
		} 

		File file = URLUtilities.toFile( getSchematronRulesLocationField().getText());

		if ( file == null && getInputLocationField() != null && getInputLocationField().isEnabled() && getInputLocationField().isVisible()) {
			file = URLUtilities.toFile( getInputLocationField().getText());
		}
		
		//if ( file == null && outputLocationField.isEnabled() && !isEmpty( outputLocationField.getText())) {
		//	file = new File( outputLocationField.getText());
		//}
		
		if ( file == null) {
			if ( document != null) {
				file = URLUtilities.toFile( document.getURL());
			} 
			
			if ( file == null) {
				file = FileUtilities.getLastOpenedFile();
			}
		}

		schematronRulesFileChooser.setCurrentDirectory( file);
		schematronRulesFileChooser.rescanCurrentDirectory();
		
		return schematronRulesFileChooser;
	}
	
	private boolean setOpenDocuments()
	{
	  boolean exists = false;
	  
	  getInputFromOpenDocumentBox().removeAllItems();
	  getSchematronRulesFromOpenDocumentBox().removeAllItems();
		
		
		Vector views = ((ExchangerEditor)xngr).getViews();

		//int result = 0;

		for ( int i = 0; i < views.size(); i++) {
			ExchangerView view = (ExchangerView) views.elementAt(i);
			//URL url = view.getDocument().getURL();
			//if (url != null)
			//{
			  //schematronRulesFromOpenDocumentField.addItem(url.toString());
			  //schematronRulesFromOpenDocumentField.addItem(view.getDocument().getName());
			//}
			
			URL url = view.getDocument().getURL();
			if (url != null)
			{
			  exists = true;
			  getInputFromOpenDocumentBox().addItem(new OpenDocument(view.getDocument().getName(),view.getDocument())) ;
			  getSchematronRulesFromOpenDocumentBox().addItem(new OpenDocument(view.getDocument().getName(),view.getDocument())) ;
			}
		}
		
		getInputFromOpenDocumentBox().setSelectedIndex(-1);
		getSchematronRulesFromOpenDocumentBox().setSelectedIndex(-1);
		
		return exists;

	}		
	
	public void show( ExchangerDocument document) {
				
		boolean existsOpenDocuments = setOpenDocuments();
		//getSchematronPlatformFromURLButton().setSelected(false);
		//getSchematronPlatformUseDefaultButton().setSelected(true);
		
		
		if ( document != null && !document.isError()) {
			this.document = document;

			getInputCurrentButton().setEnabled( true);
			
			if (existsOpenDocuments)
			  getInputFromOpenDocumentButton().setEnabled( true);
			else
			  getInputFromOpenDocumentButton().setEnabled( false);
			  
			  
			if ( isStylesheet( document)) { // schematronRules
				getSchematronRulesCurrentButton().setEnabled( true);
				
				if (existsOpenDocuments)
				  getSchematronRulesFromOpenDocumentButton().setEnabled( true);
				else
				  getSchematronRulesFromOpenDocumentButton().setEnabled( false);

				getSchematronRulesCurrentButton().setSelected( true);
				getInputFromURLButton().setSelected( false);
			} else { // normal XML
				
				getSchematronRulesCurrentButton().setEnabled( false);
				if (existsOpenDocuments)
				  getSchematronRulesFromOpenDocumentButton().setEnabled( true);
				else
				  getSchematronRulesFromOpenDocumentButton().setEnabled( false);
				  
				getInputCurrentButton().setSelected( true);
			}
		} else {
			//if ( schematronRulesCurrentButton.isSelected()) {
			//	schematronRulesFromPIsButton.setSelected( true);
			//}
		  
			getSchematronRulesCurrentButton().setEnabled( false);
			if (existsOpenDocuments)
			  getSchematronRulesFromOpenDocumentButton().setEnabled( true);
			else
			  getSchematronRulesFromOpenDocumentButton().setEnabled( false);
			getSchematronRulesCurrentButton().setSelected( false);

			getInputFromURLButton().setSelected( false);
			getInputCurrentButton().setEnabled( false);
			
			if (existsOpenDocuments)
			  getInputFromOpenDocumentButton().setEnabled( true);
			else
			  getInputFromOpenDocumentButton().setEnabled( false);
		}

//		outputToBrowserButton.setSelected( false);

		
		//getParameterManagementDialog().setParameters( new Vector());
		//getProcessorDialog();
		//setProcessor(ScenarioProperties.PROCESSOR_DEFAULT );

		
		super.show();
	}
	
	@Override
	protected void okButtonPressed() {
		boolean okToContinue = false;
		
		
		//it has to be one of these three since its a buttongroup
		if(getInputFromURLButton().isSelected() == true) {
			String text = getInputLocationField().getText();
			if(text == null) {
				
				MessageHandler.showError("Please enter an input document", "Execute Schematron");
				okToContinue = false;
			}
			else if(text.length() == 0) {
				
				MessageHandler.showError("Please enter an input document", "Execute Schematron");
				okToContinue = false;
			}
			else {
				okToContinue = true;
			}
		}
		else if(getInputFromOpenDocumentButton().isSelected() == true) {
			Object selectedObj = getInputFromOpenDocumentBox().getSelectedItem();
			if(selectedObj != null) {
				okToContinue = true;
			}
			else {
				MessageHandler.showError("Please select an input document", "Execute Schematron");
				okToContinue = false;	
			}
		}
		else if(getInputCurrentButton().isSelected() == true) {
			okToContinue = true;
		}
		
		
		//again it has to one of the three because its a buttongroup
		if(getSchematronRulesFromURLButton().isSelected() == true) {
			String text = getSchematronRulesLocationField().getText();
			if(text == null) {
				
				MessageHandler.showError("Please enter an schematron rules document", "Execute Schematron");
				okToContinue = false;
			}
			else if(text.length() == 0) {
				
				MessageHandler.showError("Please enter an schematron rules document", "Execute Schematron");
				okToContinue = false;
			}
			else {
				okToContinue = true;
			}
		}
		else if(getSchematronRulesFromOpenDocumentButton().isSelected() == true) {
			Object selectedObj = getSchematronRulesFromOpenDocumentBox().getSelectedItem();
			if(selectedObj != null) {
				okToContinue = true;
			}
			else {
				MessageHandler.showError("Please select a schematron rules document", "Execute Schematron");
				okToContinue = false;	
			}
		}
		else if(getSchematronRulesCurrentButton().isSelected() == true) {
			okToContinue = true;
		}
		
		/*if(getSchematronPlatformFromURLButton().isSelected() == true) {
			String text = getSchematronPlatformLocationField().getText();
			if(text == null) {
				
				MessageHandler.showError("Please enter an schematron platform document", "Execute Schematron");
				okToContinue = false;
			}
			else if(text.length() == 0) {
				
				MessageHandler.showError("Please enter an schematron platform document", "Execute Schematron");
				okToContinue = false;
			}
			else {
				okToContinue = true;
			}
		}*/
		
		if(okToContinue == true) {
			super.okButtonPressed();
		}
	}
	
	private boolean isStylesheet( ExchangerDocument doc) {
		XElement root = doc.getRoot();

		return (root != null && root.getName().equals( "stylesheet") && root.getNamespaceURI().equals( "http://www.w3.org/1999/schematronRules/Transform"));
	}
	
	// The creation of the file chooser causes an inspection of all 
	// the drives, this can take some time so only do this when necessary.
	private JFileChooser getInputFileChooser() {
		JFileChooser chooser = FileUtilities.getCurrentFileChooser();
		chooser.setFileFilter( chooser.getAcceptAllFileFilter());
		
		File file = URLUtilities.toFile( getInputLocationField().getText());

		if ( file == null && getSchematronRulesLocationField() != null && getSchematronRulesLocationField().isEnabled() && getSchematronRulesLocationField().isVisible()) {
			file = URLUtilities.toFile( getSchematronRulesLocationField().getText());
		}
		
		//if ( file == null && outputLocationField.isEnabled() && !isEmpty( outputLocationField.getText())) {
		//	file = new File( outputLocationField.getText());
		//}
		
		if ( file != null) {
			chooser.setCurrentDirectory( file);
		}

		chooser.rescanCurrentDirectory();
		
		return chooser;
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

	public void setInputFromURLButton(JRadioButton inputFromURLButton) {
		this.inputFromURLButton = inputFromURLButton;
	}

	public JRadioButton getInputFromURLButton() {
		return inputFromURLButton;
	}

	public void setInputCurrentButton(JRadioButton inputCurrentButton) {
		this.inputCurrentButton = inputCurrentButton;
	}

	public JRadioButton getInputCurrentButton() {
		return inputCurrentButton;
	}

	public void setInputFromOpenDocumentButton(
			JRadioButton inputFromOpenDocumentButton) {
		this.inputFromOpenDocumentButton = inputFromOpenDocumentButton;
	}

	public JRadioButton getInputFromOpenDocumentButton() {
		return inputFromOpenDocumentButton;
	}

	public void setInputLocationField(JTextField inputLocationField) {
		this.inputLocationField = inputLocationField;
	}

	public JTextField getInputLocationField() {
		return inputLocationField;
	}

	public void setInputFromOpenDocumentBox(JComboBox inputFromOpenDocumentBox) {
		this.inputFromOpenDocumentBox = inputFromOpenDocumentBox;
	}

	public JComboBox getInputFromOpenDocumentBox() {
		return inputFromOpenDocumentBox;
	}

	public void setSchematronRulesLocationField(
			JTextField schematronRulesLocationField) {
		this.schematronRulesLocationField = schematronRulesLocationField;
	}

	public JTextField getSchematronRulesLocationField() {
		return schematronRulesLocationField;
	}

	public void setSchematronRulesFromURLButton(
			JRadioButton schematronRulesFromURLButton) {
		this.schematronRulesFromURLButton = schematronRulesFromURLButton;
	}

	public JRadioButton getSchematronRulesFromURLButton() {
		return schematronRulesFromURLButton;
	}

	public void setSchematronRulesCurrentButton(
			JRadioButton schematronRulesCurrentButton) {
		this.schematronRulesCurrentButton = schematronRulesCurrentButton;
	}

	public JRadioButton getSchematronRulesCurrentButton() {
		return schematronRulesCurrentButton;
	}

	public void setSchematronRulesFromOpenDocumentButton(
			JRadioButton schematronRulesFromOpenDocumentButton) {
		this.schematronRulesFromOpenDocumentButton = schematronRulesFromOpenDocumentButton;
	}

	public JRadioButton getSchematronRulesFromOpenDocumentButton() {
		return schematronRulesFromOpenDocumentButton;
	}

	public void setSchematronRulesFromOpenDocumentBox(
			JComboBox schematronRulesFromOpenDocumentBox) {
		this.schematronRulesFromOpenDocumentBox = schematronRulesFromOpenDocumentBox;
	}

	public JComboBox getSchematronRulesFromOpenDocumentBox() {
		return schematronRulesFromOpenDocumentBox;
	}

	/*public void setSchematronPlatformLocationField(
			JTextField schematronPlatformLocationField) {
		this.schematronPlatformLocationField = schematronPlatformLocationField;
	}

	public JTextField getSchematronPlatformLocationField() {
		return schematronPlatformLocationField;
	}

	public void setSchematronPlatformFromURLButton(
			JRadioButton schematronPlatformFromURLButton) {
		this.schematronPlatformFromURLButton = schematronPlatformFromURLButton;
	}

	public JRadioButton getSchematronPlatformFromURLButton() {
		return schematronPlatformFromURLButton;
	}

	public void setSchematronPlatformUseDefaultButton(
			JRadioButton schematronPlatformUseDefaultButton) {
		this.schematronPlatformUseDefaultButton = schematronPlatformUseDefaultButton;
	}

	public JRadioButton getSchematronPlatformUseDefaultButton() {
		return schematronPlatformUseDefaultButton;
	}*/

	public void setSchematronPlatformUse15Button(
			JRadioButton schematronPlatformUse15Button) {
		this.schematronPlatformUse15Button = schematronPlatformUse15Button;
	}

	public JRadioButton getSchematronPlatformUse15Button() {
		return schematronPlatformUse15Button;
	}

	public void setSchematronPlatformUseISOButton(
			JRadioButton schematronPlatformUseISOButton) {
		this.schematronPlatformUseISOButton = schematronPlatformUseISOButton;
	}

	public JRadioButton getSchematronPlatformUseISOButton() {
		return schematronPlatformUseISOButton;
	}
	
}
