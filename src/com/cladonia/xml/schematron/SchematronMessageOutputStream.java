package com.cladonia.xml.schematron;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.SwingUtilities;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2Impl;

import com.cladonia.xml.XMLError;
import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.FileUtilities;

public class SchematronMessageOutputStream extends OutputStream {

	private ExchangerEditor editor = null;
	private SchematronTraceListener schematronTraceListener = null;
	private String systemId = null;
	
	
	
	public SchematronMessageOutputStream(ExchangerEditor _editor, SchematronTraceListener schematronTraceListener, String systemId) {
		super();
		this.editor = _editor;
		this.schematronTraceListener = schematronTraceListener;
		this.systemId = systemId;
		/*
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				editor.getOutputPanel().startCheck( "SCHEMATRON", "Schematron Version ["+FileUtilities.getSchematronVersion()+"]...");
			}						
		});
		*/
	}

	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		super.write(b);
		if(true) {
			System.out.println("write(byte[] b): "+b);
		}
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		//super.write(b, off, len);
		System.out.println("###MESSAGE:"+new String(b)+"\n\tLine: "+schematronTraceListener.getCurrentLineNumber()+"\tColumn: "+schematronTraceListener.getCurrentColumn());
		
		if(editor.getOutputPanel() != null) {
			Locator2Impl locatorImpl = new Locator2Impl();
			locatorImpl.setColumnNumber(schematronTraceListener.getCurrentColumn());
			locatorImpl.setLineNumber(schematronTraceListener.getCurrentLineNumber());
			locatorImpl.setSystemId(systemId);
			
			
			SAXParseException exception = new SAXParseException(new String(b), locatorImpl);
			
			final XMLError error = new XMLError( exception, XMLError.ERROR);
			
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					editor.getOutputPanel().addErrorSortedByLineNumber( "SCHEMATRON", error);
				}						
			});
			
		}
		
		schematronTraceListener.incrementErrorCounter();

	}

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		if(true) {
			System.out.println("write(int b): "+b);
		}
	}
	
	
}
