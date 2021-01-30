package com.cladonia.xml.schematron;

import java.io.OutputStream;

import javax.swing.SwingUtilities;
import javax.xml.transform.SourceLocator;

import com.cladonia.xngreditor.ExchangerEditor;

import net.sf.saxon.serialize.MessageEmitter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.tree.tiny.TinyTree;
import net.sf.saxon.trace.InstructionInfo;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;

public class SchematronTraceListener implements TraceListener {

	public MessageEmitter me = null;
    public int curPos;
    public OutputStream mos = null;
    private ExchangerEditor editor = null;
    
    private int currentLineNumber = -1;
    private int currentColumn = -1;
    
    private int errorCounter = 0;
    
    
    public SchematronTraceListener(ExchangerEditor editor) {
    	this.editor = editor;
    }
    
	@Override
	public void close() {
		//System.out.println("close");
		
		/*SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(errorCounter == 0) {
					editor.getOutputPanel().endCheck("SCHEMATRON", "Finished");
				}
				else if(errorCounter == 1){
					editor.getOutputPanel().endCheck("SCHEMATRON", "1 Error");
				}
				else {
					editor.getOutputPanel().endCheck("SCHEMATRON", errorCounter+ " Errors");
				}
			}
			
		});*/
		
	}

	@Override
	public void endCurrentItem(Item item) {
		
	}

	@Override
	public void enter(InstructionInfo arg0, XPathContext arg1) {
		
	}

	@Override
	public void leave(InstructionInfo arg0) {
		
		
		/*String str = messageOutputStream.toString("UTF-8");
		length = str.length();

        if (length != 0 && this.curPos !=  length  ) {
        	java.lang.System.err.println( str.substring(this.curPos) + " sysid:" + element.getSystemID() + " lineNum:" + element.getLineNumber());

        	message = str.substring(this.curPos);


		    jsError = new Packages.org.xml.sax.SAXParseException(message,"" , element.getSystemID(), element.getLineNumber(), 1);

           this.curPos = length;

        }*/
		
	}

	@Override
	public void open() {
		//System.out.println("open");
		
	}

	@Override
	public void startCurrentItem(Item item) {
		
		if(item instanceof TinyTextImpl) {
			//System.out.println("startCurrentItem: "+((TinyTextImpl)item).getLocalPart()+ " - line: "+((TinyTextImpl)item).getLineNumber());
			setCurrentLineNumber(((TinyTextImpl)item).getLineNumber());
			setCurrentColumn(((TinyTextImpl)item).getColumnNumber());
		}
		else if(item instanceof net.sf.saxon.tree.tiny.TinyNodeImpl) {
			//System.out.println("startCurrentItem: "+((TinyNodeImpl)item).getLocalPart()+ " - line: "+((TinyNodeImpl)item).getLineNumber());
			setCurrentLineNumber(((TinyNodeImpl)item).getLineNumber());
			setCurrentColumn(((TinyNodeImpl)item).getColumnNumber());
		}
		else {
			System.out.println("startCurrentItem: "+item.getClass());
			
		}
		
	}

	public void setMessageEmitter(MessageEmitter messageEmitter) { 
		me = messageEmitter;
	}

	public void setMessageOutputStream(OutputStream messageOutputStream) { 
		mos = messageOutputStream;
	}

	public void setCurrentLineNumber(int currentLineNumber) {
		this.currentLineNumber = currentLineNumber;
	}

	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	public void setCurrentColumn(int currentColumn) {
		this.currentColumn = currentColumn;
	}

	public int getCurrentColumn() {
		return currentColumn;
	}

	public void setErrorCounter(int errorCounter) {
		this.errorCounter = errorCounter;
	}

	public int getErrorCounter() {
		return errorCounter;
	}

	public void incrementErrorCounter() {
		this.errorCounter++;
	}
}
