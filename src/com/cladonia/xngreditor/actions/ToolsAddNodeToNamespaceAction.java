/*
 * $Id: ToolsAddNodeToNamespaceAction.java,v 1.10 2004/10/28 07:46:19 tcurley Exp $ 
 *
 * Copyright (C) 2004, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.xngreditor.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

import com.cladonia.xml.ExchangerDocument;
import com.cladonia.xml.XAttribute;
import com.cladonia.xml.XElement;
import com.cladonia.xml.editor.Editor;
import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xngreditor.ToolsAddNodeToNamespaceDialog;
import com.cladonia.xngreditor.properties.ConfigurationProperties;



/**
 * An action that can be used to add a namespace to elements or attributes.
 *
 * @version	$Revision: 1.10 $, $Date: 2004/10/28 07:46:19 $
 * @author Thomas Curley <tcurley@cladonia.com>
 */
public class ToolsAddNodeToNamespaceAction extends AbstractAction {
    
    private static final boolean DEBUG = false;
    private ExchangerEditor parent = null;
    private ToolsAddNodeToNamespaceDialog dialog = null;
    private Editor editor = null;
    private ConfigurationProperties props;
    
    /**
     * The constructor for the action which allows to add a namespace to elements or attributes.
     *
     * @param parent the parent frame.
     */
    public ToolsAddNodeToNamespaceAction( ExchangerEditor parent, Editor editor, ConfigurationProperties props) {
        super( "Add Nodes to Namespace ...");
        
        this.parent = parent;
        this.props = props;
        
        //this.properties = props;
        
        putValue( MNEMONIC_KEY, new Integer( 'D'));
        putValue( SHORT_DESCRIPTION, "Add Nodes to Namespace ...");
    }
    
    /**
     * Sets the current view.
     *
     * @param view the current view.
     */
    public void setView( Object view) {
        if ( view instanceof Editor) {
            editor = (Editor)view;
        } else {
            editor = null;
        }
        
        setDocument( parent.getDocument());
    }
    
    /**
     * set the current document
     * @param doc
     */
    public void setDocument( ExchangerDocument doc) {
        if ( doc != null && doc.isXML()) {
            setEnabled( editor != null);
        } else {
            setEnabled( false);
        }
    }
    
    
    /**
     * The implementation of the validate action, called 
     * after a user action.
     *
     * @param event the action event.
     */
    public void actionPerformed( ActionEvent event) {
        if ( dialog == null) {
            dialog = new ToolsAddNodeToNamespaceDialog( parent,props);
        }
        
        //called to make sure that the model is up to date to 
        //prevent any problems found when undo-ing etc.
        parent.getView().updateModel();
        
        //get the document
        final ExchangerDocument document = parent.getDocument();
        
        if ( document.isError()) {
            MessageHandler.showError(parent, "Please make sure the document is well-formed.", "Parser Error");
            return;
        }
        String currentXPath = null;
        Node node = (Node)document.getLastNode( parent.getView().getEditor().getCursorPosition(), true);

        if ( props.isUniqueXPath()) {
            currentXPath = node.getUniquePath();
        } else {
            currentXPath = node.getPath();
        }
        //create temporary document
        dialog.show(document.getDeclaredNamespaces(),currentXPath);
        
        if(!dialog.isCancelled()) {
            parent.setWait( true);
            parent.setStatus( "Adding Nodes To Namespace ...");
            
            // Run in Thread!!!
            Runnable runner = new Runnable() {
                public void run()  {
                    try {
                        ExchangerDocument tempDoc =  new ExchangerDocument(document.getText());
                        String uri = (String)dialog.nsURICombo.getSelectedItem();
                        String prefix = dialog.nsPrefixTextField.getText();
                        if(uri.equalsIgnoreCase("none")) {
                            uri = "";
                        }
                        if(prefix.equalsIgnoreCase("none")) {
                            prefix = "";
                        }
                        Namespace ns = new Namespace(prefix,uri);
                        
                        String newString = ToolsAddNodeToNamespaceAction.this.addNamespaceToNode(tempDoc,dialog.xpathPanel.getXpathPredicate(),ns);
                        
                        if(newString!=null) {
                            
                            //need to parse the new document to make sure that it
                            //will produce well-formed xml.
                            ExchangerDocument newDocument =  new ExchangerDocument(newString);
                            boolean createDocument=true;
                            
                            if(newDocument.isError()) {
                                int questionResult = MessageHandler.showConfirm(parent,"The resulting document will not be well-formed\n"+
                                        "Do you wish to continue?");
                                
                                if(questionResult==MessageHandler.CONFIRM_NO_OPTION) {
                                    createDocument=false;
                                }
                            }
                            
                            if(createDocument) {
	                            if(dialog.toNewDocumentRadio.isSelected()) {
	                                //user has selected to create the result as a new document
	                                parent.open(newDocument, null);
	                            }
	                            else {
	                                parent.getView().getEditor().setText(newString);
	                                SwingUtilities.invokeLater(new Runnable() {
	                                    public void run() {
	                                        parent.switchToEditor();
	                                        
	                                        parent.getView().updateModel();
	                                    }
	                                });
	                            }
                            }
                                                                              
                            
                        }
                    } catch ( Exception e) {
                        // This should never happen, just report and continue
                        MessageHandler.showError( parent, "Cannot Add Nodes To Namespace", "Tools Add Nodes To Namespace Error");
                    } finally {
                        parent.setStatus( "Done");
                        parent.setWait( false);
                    }
                }
            };
            
            // Create and start the thread ...
            Thread thread = new Thread( runner);
            thread.start();
//          }
        }
    }
    
    /**
     * Adds the namespace to the xpath - selected nodes
     * 
     * @param document the exchanger document
     * @param xpathPredicate - the xpath predicate to resolve
     * @param newNs - the new namespace for the nodes
     * @return the documents text
     */
    public String addNamespaceToNode(ExchangerDocument document, String xpathPredicate, Namespace newNs) throws Exception {
        
        Vector nodeList = document.search(xpathPredicate);
        Vector attributeList = new Vector();
        String warning = "";
        if(nodeList.size()>0) {
            try {
                
                for(int cnt=0;cnt<nodeList.size();++cnt) {
                    
                    Node node = (Node)nodeList.get(cnt);
                    if (node instanceof Element) {
                        
                        XElement e = (XElement) node;
                        if(newNs!=null) {
                            e.setNamespace(newNs);
                        }
                    }
                    else if(node instanceof Attribute) {
                        
                        attributeList.add((XAttribute) node);
                    }
                    
                }
                //now change all the attributes
                changeAttribute(document,attributeList,newNs);
                
            }catch (NullPointerException e) {
                MessageHandler.showError(parent, "XPath: "+xpathPredicate+"\nCannot be resolved","Tools Add Nodes To Namespace Error");
                return(null);
            }
            catch (Exception e) {
                MessageHandler.showError(parent, "Error adding namespaces to nodes","Tools Add Nodes To Namespace Error");
                return(null);
            }
            
            document.update();
        }
        else {
            MessageHandler.showError(parent, "No nodes could be found for:\n"+xpathPredicate,"Tools Add Nodes To Namespace Error");
            return(null);
        }
        
        return(document.getText());
    }
    
    /**
     * Walk through the document tree and change the attributes if they are the selected ones
     * 
     * @param document the exchanger document
     * @param attributeList the list of attributes that match the xpath result
     * @param newNs the new namespace for the nodes
     * @throws Exception
     */
    public void changeAttribute(ExchangerDocument document, List attributeList, Namespace newNs) throws Exception {
        treeWalk( document.getDocument().getRootElement(),attributeList,newNs );
    }
    
    /**
     * Walk through the document tree and change the attributes if they are the selected ones
     * 
     * @param document the exchanger document
     * @param attributeList the list of attributes that match the xpath result
     * @param newNs the new namespace for the nodes
     * @throws Exception
     */
    public void treeWalk(Element element, List attributeList, Namespace newNs) throws Exception {
        
        for ( int i = 0, size = element.nodeCount(); i < size; i++ ) {
            Node node = element.node(i);
            
            if ( node instanceof Element ) {
                //if it's an element
                XElement e = (XElement)node;
                if(e.attributeCount()>0) {
                    //and it has attributes
                    
                    List attList = e.attributes();
                    
                    for(int cnt=0;cnt<attList.size();++cnt) {
                        //for each attribute
                        XAttribute newAtt = (XAttribute)attList.get(cnt); 
                        int fcnt = findInList(newAtt,attributeList);
                        if(fcnt > -1) {
                            //if the attribute matches the one we are looking for
                            //replace it with the new qname
                            String name = newAtt.getName();
                            String value = newAtt.getValue();
                            Namespace ns = newNs;
                            
                            attList.set(cnt, new XAttribute(new QName( name, ns), value));
                            
                            //now remove it from the attributeList to speed up the next searches
                            attributeList.remove(fcnt);
                            
                        }
                    }
                }
                treeWalk( (Element) node, attributeList, newNs );
            }                
        }
        
    }
    
    /**
     * Finds an attribute in a list and returns the index
     * 
     * @param att the attribute to search for
     * @param attributeList the list of attributes to search in
     * @return the index of the attribute in list, or -1 if not found.
     */
    public int findInList(XAttribute att,List attributeList) throws Exception{
        
        int cnt=0;
        int size = attributeList.size();
        boolean found=false;
        while((!found)&&(cnt<size)) {
            
            if(att.equals((XAttribute)attributeList.get(cnt))) {
                found=true;
            }
            else {
                cnt++;
            }
        }
        if(cnt==size)
            return(-1);
        else
            return(cnt);
    }
    
    
}
