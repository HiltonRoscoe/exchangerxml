
package com.cladonia.xslt.debugger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

/*
import com.icl.saxon.Controller;
import com.icl.saxon.trace.TraceListener;
import com.icl.saxon.output.MessageEmitter;
import com.icl.saxon.Context;
import com.icl.saxon.style.StyleElement;


import com.icl.saxon.Bindery;
import com.icl.saxon.Binding;
import com.icl.saxon.expr.StaticContext;
import com.icl.saxon.expr.Value;
*/

import javax.xml.transform.stream.StreamSource;

import java.util.*;


import java.io.PrintWriter;

import net.sf.saxon.serialize.MessageEmitter;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.style.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.Controller;
import net.sf.saxon.value.Value;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.expr.instruct.Bindery;
import net.sf.saxon.expr.instruct.Debugger;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.StackFrame;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.ValueRepresentation;

public class Saxon2Debugger extends XSLTDebugger implements Debugger
{

	protected TransformerFactoryImpl factory = null;
	private SlotManager sm = null;
	
	Saxon2TraceListener _saxon2TraceListener = null;
	MessageEmitter me = null;

	public SlotManager makeSlotManager()
	{
	  sm = new MySlotManager();
	  return sm;
	}

	public SlotManager getSlotManager()
	{
	  return sm;
	}

	
	public Saxon2Debugger( BreakpointList stylesheetBreakpoints, BreakpointList inputBreakpoints) 
	{
		super( stylesheetBreakpoints, inputBreakpoints);
	}

	public TransformerFactory getFactory()
	{
		factory = new net.sf.saxon.TransformerFactoryImpl();
        Configuration config = new Configuration();
        factory.setConfiguration(config);
        // In basic XSLT, all nodes are untyped by definition
       // config.setAllNodesUntyped(true);
        config.setDebugger(this);
        
            TraceListener traceListener = new net.sf.saxon.trace.XSLTTraceListener();
		_saxon2TraceListener  = new Saxon2TraceListener();

		factory.setAttribute(
                                FeatureKeys.LINE_NUMBERING,
                                Boolean.TRUE);

            factory.setAttribute(FeatureKeys.TRACE_LISTENER,_saxon2TraceListener);

		return (TransformerFactory)factory;
	}

	public Transformer initializeTransformer(Transformer t) {
		_saxon2TraceListener.setXSLTDebugger(this);
		_saxon2TraceListener.setStyleStack(_styleStack);
		_saxon2TraceListener.setXSLTemplateStack(_xslTemplateStack);
		_saxon2TraceListener.setXMLStack(_xmlStack);
		_saxon2TraceListener.setOutStack(_outStack);
		_saxon2TraceListener.setMixedStack(_mixedStack);
		_saxon2TraceListener.setXSLTraceStack(_xslTraceStack);
		_saxon2TraceListener.setXMLTraceStack(_xmlTraceStack);
		_saxon2TraceListener.setOutTraceStack(_outTraceStack);
		_saxon2TraceListener.setMixedTraceStack(_mixedTraceStack);

		_saxon2TraceListener.setXSLTemplateProfileMap(_xslTemplatesProfileMap);


                PreparedStylesheet sheet = null;

		Transformer instance = null;

		try
		{
                sheet = (PreparedStylesheet)factory.newTemplates(new StreamSource(
					_stylesheetFilename));

                instance = sheet.newTransformer();

		}
		catch (Exception Ex) {}
	
		if (instance != null)
		     t =  instance;


		//((Controller)t).addTraceListener((TraceListener)_saxon2TraceListener);
		//factory.addTraceListener(traceListener );

		//((Controller)t).addTraceListener(traceListener );
		
		if (_messageOutputStream != null)
		{
		  me = new MessageEmitter();
		  //tcurley 07-05-08 changed for saxon9
		  try {
			me.setWriter(new PrintWriter(_messageOutputStream));
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	        try
              {

		    me.setOutputStream(_messageOutputStream);
              }
              catch (Exception ex) {}

		  ((Controller)t).setMessageEmitter(me);
		}
		
		//((Controller)t).setLineNumbering(true);

		

		return t;
	}
	
public Vector getLocalVariables()
{


	if (this._styleStack == null) 
	{
		//System.out.println("null stack ");
		return null;
	}

	if (this._styleStack.size() == 0) 
	{
		//System.out.println("empty stack ");
		return null;
	}

	


	Vector locvars = new Vector();

	Saxon2StyleStackItem ssi = (Saxon2StyleStackItem)this._styleStack.peek();

	if (ssi == null)
		return null;

	XPathContext context = ssi._context;
	//StaticContext sc = context.getStaticContext();
	
	StackFrame sf = context.getStackFrame();
	
	if(sf.getStackFrameMap() instanceof MySlotManager) {
		MySlotManager slotManager = (MySlotManager)sf.getStackFrameMap();
		
		HashMap hm = null;
		
		if (slotManager != null)
		{
		  //System.out.println("got slot manager");
		  hm = slotManager.getMap();
		}
		else
		{
		  //System.out.println("**null slot manager!***");
		  return null;
		}
		
		ValueRepresentation [] vrs = sf.getStackFrameValues();
		
		int len = vrs.length;
		
		for (int i=0; i<len; i++)
		{
		  
		  //ValueRepresentation  vr = vrs[i];
		  
		  
		}
		
		Set keys = hm.entrySet();
	/*
		for (Iterator i=m.entrySet().iterator(); i.hasNext(); ) {
		    Map.Entry e = (Map.Entry) i.next();
		    System.out.println(e.getKey() + ": " + e.getValue());
		}
	*/
	
		
		
		Iterator iter = keys.iterator();
		
		while (iter.hasNext())
		{
		  Map.Entry entry = ((Map.Entry)iter.next());
		  
		  Object key = entry.getKey();
		  Object value = entry.getValue();
		  
		  StructuredQName structQNameValue = null;
		  if(value instanceof StructuredQName) {
			  structQNameValue = (StructuredQName)value;
		  }
		  else {
			  
		  }
		  
		  //System.out.println("Key: " + key + "  Value: " + value);
		  
		  
		  ValueRepresentation vr = context.evaluateLocalVariable( ((Integer)key).intValue() )  ;
	
		String val = "";
		
		int type = XSLTVariable.XSLT_TYPE_UNKNOWN;
	
		  
		  	if (vr != null)
		  	{
	
		  	  
		  	  //System.out.println("The class of vr is"  + vr.getClass().getName());
		  	  try
		  	  {
	
		  		//tcurley 07-05-08 changed for saxon9
	   		  //XSLVariableDeclaration vd = ssi._styleElement.bindVariable(((Integer)value).intValue());
		  		XSLVariableDeclaration vd = ssi._styleElement.bindVariable(structQNameValue);
		  	    //String name = vd.getVariableName();
		  		String name = vd.getVariableDisplayName();
	
		  		XSLTVariable var = new XSLTVariable(name);
	
		  	    val = ((Value)vr).getStringValue();
			  	//System.out.println("Value: " + val);
	
		    	if (val != null)
		    	{
		    	  var.setValue(val);
		    	}
			  	
			  	//TODO SET TYPE
		    	
		    	locvars.add(var);
			  	
		  	  }
		  	  catch (Exception ex) {}
		  	  
		  	}
		}

	  
	}
	else {
		SlotManager slotManager = sf.getStackFrameMap();
		
		List hm = null;
		
		if (slotManager != null)
		{
		  //System.out.println("got slot manager");
		  hm = slotManager.getVariableMap();
		}
		else
		{
		  //System.out.println("**null slot manager!***");
		  return null;
		}
		
		ValueRepresentation [] vrs = sf.getStackFrameValues();
		
		int len = vrs.length;
		
		for (int i=0; i<len; i++) {
			
		}
		
		//Set keys = hm.entrySet();
		
		for(int cnt=0;cnt<hm.size();++cnt) {
			
			Object value = hm.get(cnt);
			
			
		  //Map.Entry entry = ((Map.Entry)iter.next());
		  
		  //Object key = entry.getKey();
		  //Object value = entry.getValue();
		  
		  StructuredQName structQNameValue = null;
		  if(value instanceof StructuredQName) {
			  structQNameValue = (StructuredQName)value;
		  }
		  else {
			  
		  }
		  
		  //System.out.println("Key: " + key + "  Value: " + value);
		  
		  
		  ValueRepresentation vr = context.evaluateLocalVariable(cnt)  ;
	
		String val = "";
		
		int type = XSLTVariable.XSLT_TYPE_UNKNOWN;
	
		  
		  	if (vr != null)
		  	{
	
		  	  
		  	  //System.out.println("The class of vr is"  + vr.getClass().getName());
		  	  try
		  	  {
	
		  		//tcurley 07-05-08 changed for saxon9
	   		  //XSLVariableDeclaration vd = ssi._styleElement.bindVariable(((Integer)value).intValue());
		  		XSLVariableDeclaration vd = ssi._styleElement.bindVariable(structQNameValue);
		  	    //String name = vd.getVariableName();
		  		String name = vd.getVariableDisplayName();
	
		  		XSLTVariable var = new XSLTVariable(name);
	
		  	    val = ((Value)vr).getStringValue();
			  	//System.out.println("Value: " + val);
	
		    	if (val != null)
		    	{
		    	  var.setValue(val);
		    	}
			  	
			  	//TODO SET TYPE
		    	
		    	locvars.add(var);
			  	
		  	  }
		  	  catch (Exception ex) {}
		  	  
		  	}
		}
	}

	
	/*
    ParameterSet tunnelParams = context.getTunnelParameters();

    for (int i=0; i<tunnelParams.length; i++) {

      
    }
    */
    
	return locvars;

	
	/*
	Enumeration[] en = ssi._styleElement.getVariableNames();


	//System.out.println("Local Variables ");
	while (en[1].hasMoreElements())
	{
		String elname = (String)en[1].nextElement();

		elname = elname.substring(1, elname.length());

		locvars.add(getVariable(ssi, elname));		
	}

	return locvars;
*/

}

public Vector getGlobalVariables()
{

	if (this._styleStack == null) 
	{
		//System.out.println("null stack ");
		return null;
	}

	if (this._styleStack.size() == 0) 
	{
		//System.out.println("empty stack ");
		return null;
	}

	
	Vector glvars = new Vector();


	Saxon2StyleStackItem ssi = (Saxon2StyleStackItem)this._styleStack.peek();

	if (ssi == null)
		return null;

	XSLStylesheet sheet = ssi._styleElement.getContainingStylesheet();
	
	StylesheetModule tempSheet = null;
	StylesheetModule module = sheet.getPrincipalStylesheetModule();
	while ( ( tempSheet = module.getImporter()) != null)
	  module = tempSheet;
	
	//XSLStylesheet sheet = ssi._styleElement.getPrincipalStylesheet();

	List topLevelElements = new ArrayList();// module.getTopLevel();
	
	Iterator it = topLevelElements.iterator();


	while (it.hasNext())
	{
	  Object obj = it.next();
	  
	  if (obj instanceof XSLVariable || obj instanceof XSLParam)
	  {

		int type = XSLTVariable.XSLT_TYPE_UNKNOWN;
		String value = "";
		
	    
	    XSLVariableDeclaration vd = (XSLVariableDeclaration)obj;

	    //tcurley 07-05-08 changed for saxon9
		//XSLTVariable var = new XSLTVariable(vd.getVariableName());
	    XSLTVariable var = new XSLTVariable(vd.getVariableDisplayName());

	    
		Bindery bry = ssi._context.getController().getBindery();

		try
		{
		  if (bry != null)
		  {
			Value v = (Value)bry.getGlobalVariable(vd.getSlotNumber());
	
		    	if (v != null)
		    	{
		    	  var.setValue(v.getStringValue());
		    	}
		    	
		    	//var.setType(convertSaxon2Type(v.getDataType()));
		    	
		    	glvars.add(var);
		  }

		}
		catch (Exception ex) {}
		
		
		
	  }
	  
	}
	

	return glvars;
	
	
	/*
	Enumeration[] en = ssi._styleElement.getVariableNames();


	//System.out.println("Global  Variables ");
	while (en[0].hasMoreElements())
	{
		String elname = (String)en[0].nextElement();

		elname = elname.substring(1, elname.length());


		glvars.add(getVariable(ssi, elname));
		

	}

	return glvars;
*/

		
	
}


public XSLTVariable getVariable(Saxon2StyleStackItem ssi, String variableName)
{
	return null;
/*

	XPathContext ctx = ssi._context;
	
	int type = XSLTVariable.XSLT_TYPE_UNKNOWN;
	String value = "";
	
	XSLTVariable var = new XSLTVariable(variableName);
	
	
	if (ctx == null)
		return var;

	StyleElement se = ssi._styleElement;

	Controller c = ctx.getController();
	StaticContext sc = ctx.getStaticContext();

	if (sc == null)
		return var;

	try
	{
	  Bindery bry = ctx.getBindery();

	  Binding bng = se.getVariableBinding(sc.makeNameCode(variableName, false));

	  if (bng != null)
	  {
	    	Value v = bry.getValue(bng, bry.getFrameId());

	    	if (v != null)
	    	{
	    	  var.setValue(v.asString());
	    	}
	    	
	    	var.setType(convertSaxon2Type(v.getDataType()));
	  }

	}
	catch (Exception ex) {}

	return var;
*/
	}


public String getVariableValue(String variableName) {
	return null;

	//return this._saxon2TraceListener.getVariableValue(variableName);
}

public static int convertSaxon2Type(int saxonType)
{
  return 1;
/*
  switch (saxonType)
  {
    case Value.BOOLEAN:
      return XSLTVariable.XSLT_TYPE_BOOLEAN;
    
    case Value.NUMBER:
      return XSLTVariable.XSLT_TYPE_NUMBER;
    
    case Value.STRING:
      return XSLTVariable.XSLT_TYPE_STRING;
    
    case Value.NODESET:
      return XSLTVariable.XSLT_TYPE_NODESET;
    
    case Value.OBJECT:
      return XSLTVariable.XSLT_TYPE_OBJECT;
    
    default:
      return XSLTVariable.XSLT_TYPE_UNKNOWN;
    
  }
*/



}

public void cleanUp() {
	super.cleanUp();

	//	T Curley 04.12.06
	//fixed a null pointer exception bug with the debugger
	if(t != null) {
		((Controller)t).removeTraceListener( _saxon2TraceListener);
		//((Controller)t).setMessageEmitter( null);
		((Controller)t).clearDocumentPool();
	}
	
	if(me != null) {
		
		try
		{
			me.setWriter( null);

		
			me.setOutputStream( null);
		}		
		catch (XPathException ex) {
			
		}
		catch (Exception ex) {
			
		}
	}
	
	me = null;
	t = null;
}
}

class MySlotManager extends SlotManager
{
  private HashMap hm = new HashMap();
  
  MySlotManager()
  {
    super();  
  }
  /*public int allocateSlotNumber(int fingerprint)  
  { 
  
    int slotNumber = super.allocateSlotNumber(fingerprint);
    //System.out.println("slot: " + slotNumber + "    fingerprint: " + fingerprint);
  
    hm.put(new Integer(slotNumber) , new Integer(fingerprint) );
    
    return slotNumber;
    
  }*/
  
  public int allocateSlotNumber(StructuredQName fingerprint)  
  { 
  
    int slotNumber = super.allocateSlotNumber(fingerprint);
    //System.out.println("slot: " + slotNumber + "    fingerprint: " + fingerprint);
  
    hm.put(new Integer(slotNumber) , fingerprint );
    
    return slotNumber;
    
  }

  public HashMap getMap()
  {
   return hm; 
  }
  
}
