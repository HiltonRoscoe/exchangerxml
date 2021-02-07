package com.cladonia.xslt.debugger;

import java.io.PrintStream;
import java.util.Stack;
import java.util.HashMap;
import java.util.Vector;

import java.util.Properties;
/*
import com.icl.saxon.Bindery;
import com.icl.saxon.Binding;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.NodeHandler;
import com.icl.saxon.expr.StaticContext;
import com.icl.saxon.expr.Value;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.style.StyleElement;
import com.icl.saxon.output.Outputter;
*/

import javax.security.auth.login.Configuration;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStreamWriter;

import net.sf.saxon.expr.parser.Location;
import net.sf.saxon.lib.*;
import net.sf.saxon.serialize.Emitter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.Item;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.trace.InstructionInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.om.NodeInfo;
//tcurley 07-05-08 changed for saxon 9
//import net.sf.saxon.style.StandardNames;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.trace.LocationKind;
import net.sf.saxon.type.Type;
import net.sf.saxon.Controller;
import net.sf.saxon.type.SchemaType;


public class Saxon2TraceListener implements TraceListener {

  String indent = "";
  Saxon2Debugger _XSLTDebugger = null;
  int _currentStylesheetLineNumber = -1;
  int _currentStylesheetColumnNumber = -1;
  int _currentInputLineNumber = -1;
  int _currentInputColumnNumber = -1;
  int _previousStylesheetLineNumber = -1;
  int _previousInputLineNumber = -1;
  
  boolean _isInputBreakpoint = false;
  boolean _isStylesheetStartTag = true;
  boolean _isInputStartTag = true;
  
  String _inputDisplayName = null;
  String _inputLocalName = null;
  String _inputURI = null;
 
  String _stylesheetDisplayName = null;
  String _stylesheetLocalName = null;
  String _stylesheetURI = null;
 

  String _currentStylesheetFilename = null;
  String _previousStylesheetFilename = null;
  String _currentInputFilename = null;
  String _previousInputFilename = null;
  
  boolean _atStartOfStylesheet = true;
  boolean _atStartOfInput = true;
  
  Stack _styleStack = null;
  Saxon2StyleStackItem _ssiTemplateEnd = null;
  Stack _xslTemplateStack = null;
  Stack _xmlStack = null;
  Stack _outStack = null;
  Stack _mixedStack = null;
 
  Stack _xslTraceStack = null;
  Stack _xmlTraceStack = null;
  Stack _outTraceStack = null;
  Stack _mixedTraceStack = null;
  
  
  HashMap _xslTemplatesProfileMap = null;
  
  HashMap _xslFilesMap = null;

  boolean newdoc = false;

  boolean firstStyleElement = false;


    /**
     * Generate attributes to be included in the opening trace element
     */

    protected String getOpeningAttributes() {
        return "xmlns:xsl=\"" + NamespaceConstant.XSLT + '\"';
    }

    /**
     * Get the trace element tagname to be used for a particular construct. Return null for
     * trace events that are ignored by this trace listener.
     */

    protected String tag(int construct) {

//System.out.println(":in tag: " + StandardNames.getDisplayName(construct));


        if (construct < 1024) {
            return StandardNames.getDisplayName(construct);
        }


        switch (construct) {
            case LocationKind.LITERAL_RESULT_ELEMENT:
                return "LRE";
            case LocationKind.LITERAL_RESULT_ATTRIBUTE:
                return "ATTR";
            case LocationKind.LET_EXPRESSION:
                return "xsl:variable";
            case LocationKind.EXTENSION_INSTRUCTION:
                return "extension-instruction";
            case LocationKind.TRACE_CALL:
                return "user-trace";
            default:
                return null;

            }

            //return null;

    }


 public void open(Controller controller){
      open();
 }
  
  public void open()
  {

        //System.out.println("<trace " +
        //        "saxon-version=\"" + Version.getProductVersion()+ "\" " +
        //        getOpeningAttributes() + '>');


//System.out.println("open");

    _atStartOfStylesheet = true;
    _atStartOfInput = true;
    
    _xslFilesMap = new HashMap();
    
    this._XSLTDebugger.onStart();
 
//System.out.println("back from onStart");   
    
  }

 
  public void close()
  {
//System.out.println("close");
        //System.out.println("</trace>");

    this._XSLTDebugger.onEnd();
  }

 

  public void enter(InstructionInfo instruction, XPathContext context)
  {
	//System.out.println("enter");
    //System.out.println("The class of instruction is"  + instruction.getClass().getName());


    int infotype = instruction.getConstructType();
    //tcurley 07-05-08 changed for saxon 9
    //int objectNameCode = instruction.getObjectNameCode();
    String tag = tag(infotype);
    if (tag==null) {
        // this TraceListener ignores some events to reduce the volume of output
        return;
    }
    NamePool pool = context.getController().getConfiguration().getNamePool();
    String msg = /*AbstractTraceListener.spaces(indent) + '<' +*/ tag;

    //String n = (String)instruction.getProperty("name");
    //if (n!=null) {
    //    msg += " name=\"" + n + '"';
    //} else if (objectNameCode != -1) {
    //     msg += " name=\"" + pool.getDisplayName(objectNameCode) + '"';
    //}

    //System.out.println(msg);
    
    //System.out.println("The class of instruction is"  + instruction.getClass().getName());

    
    if (!(instruction instanceof StyleElement))
      return;
   
    _isStylesheetStartTag = true;

    StyleElement styleElement = (StyleElement) instruction ;
      
    //System.out.println();
    NamePool np = styleElement.getNamePool();
    int code = styleElement.getFingerprint();
    _stylesheetLocalName = np.getLocalName(code);
    _stylesheetURI = np.getURI(code);
    _stylesheetDisplayName = np.getDisplayName(code);

    //_stylesheetLocalName = styleElement.getLocalName();
    //_stylesheetURI = styleElement.getURI();
    //_stylesheetDisplayName = styleElement.getDisplayName();


    //System.out.println(_stylesheetLocalName + "; " + _stylesheetDisplayName + "; " );

    this._currentStylesheetFilename = Breakpoint.normalizeFilename(styleElement
        .getSystemId());
    this._currentStylesheetLineNumber = styleElement.getLineNumber();
    this._currentStylesheetColumnNumber = -1;
    System.out.println("Trace: "+_stylesheetDisplayName+" Line: "+_currentInputLineNumber+" Col: "+styleElement.getColumnNumber());
    
    

     _xslFilesMap.put(this._currentStylesheetFilename, this._currentStylesheetFilename);
 

    if (styleElement instanceof XSLTemplate)
    {
      String match = null;
      match = styleElement.getAttributeValue("match");
      String name = null;
      name = styleElement.getAttributeValue("name");
      String priority = null;
      priority = styleElement.getAttributeValue("priority");
      String mode = null;
      mode = styleElement.getAttributeValue("mode");
      
      //System.out.println(name + "; " + match + "; " + priority + "; " + mode );
      
      XSLTemplateStackItem xtsi = new XSLTemplateStackItem(name, match,
          priority, mode, this._currentStylesheetFilename,
          this._currentStylesheetLineNumber,
          this._currentStylesheetColumnNumber);
      _xslTemplateStack.push(xtsi);
      
      String styleElementStr = _stylesheetDisplayName;
      if (name != null) styleElementStr += " name=\"" + name + "\"";
      if (match != null) styleElementStr += " match=\"" + match + "\"";
      if (priority != null)
          styleElementStr += " priority=\"" + priority + "\"";
      if (mode != null) styleElementStr += " mode=\"" + mode + "\"";
      
      DefaultStackItem dsi = new DefaultStackItem(styleElementStr,
          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_XSL_START);
      _mixedStack.push(dsi);
      
      if (this._XSLTDebugger.isTracingEnabled())
      {
        _xslTraceStack.push(dsi);
        _mixedTraceStack.push(dsi);
      }
      
      Integer count = (Integer)_xslTemplatesProfileMap.get(styleElementStr);
      
      // update profile for this template
      if (count == null)
      {
        _xslTemplatesProfileMap.put(styleElementStr, new Integer(1));
      }
      else
      {
        _xslTemplatesProfileMap.put(styleElementStr, new Integer(count.intValue() + 1));
      }
      
    } 
    else if (_stylesheetURI.equals(XSLTDebugger.XSL_URI) && _stylesheetLocalName.equals("element"))
    {
      String name = "";
      name = styleElement.getAttributeValue("name");
      //System.out.println("Element: " + name);
      DefaultStackItem dsi = new DefaultStackItem(name,
          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_OUT_START);
      _outStack.push(dsi);
      _mixedStack.push(dsi);
      
      if (this._XSLTDebugger.isTracingEnabled())
      {
        _outTraceStack.push(dsi);
      	_mixedTraceStack.push(dsi);
      }
    } 
    else if (_stylesheetURI.equals(XSLTDebugger.XSL_URI))
    {
      DefaultStackItem dsi = new DefaultStackItem(_stylesheetDisplayName,
          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_XSL_START);
      //System.out.println("push on mixedstack");
      _mixedStack.push(dsi);
      
      if (this._XSLTDebugger.isTracingEnabled())
      {
        _xslTraceStack.push(dsi);
        _mixedTraceStack.push(dsi);
      } 
      
    } 
    else if (!(_stylesheetURI.equals(XSLTDebugger.XSL_URI)))
    {
      //System.out.println("Element: " + localName);
      DefaultStackItem dsi = new DefaultStackItem(_stylesheetDisplayName,
          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_OUT_START);
      _outStack.push(dsi);
      _mixedStack.push(dsi);
      
      if (this._XSLTDebugger.isTracingEnabled())
      {
        _outTraceStack.push(dsi);
        _mixedTraceStack.push(dsi);
      }
    }
    



    Saxon2StyleStackItem ssi = new Saxon2StyleStackItem(context,styleElement );
    this._styleStack.push(ssi);
    
    if (this._XSLTDebugger.isOutputRedirected())
    {
	    if (_stylesheetURI.equals(XSLTDebugger.XSL_URI) && (_stylesheetLocalName.equals("document") || _stylesheetLocalName.equals("result-document") ))
	    {
	      handleOutputDocumentOpen(styleElement , context);
	    } 
	    else if (newdoc == true)
	    {
	      // must wait till first instruction after xsl:document to actually change
	      changeOutputDestination(styleElement , context);
	    }
    }
    
    if (handleImmediateStop() == true)
    {
      this._XSLTDebugger.command(XSLTDebugger.COMMAND_STEP);
    }
    
    
    if (this._XSLTDebugger.getCommand() == XSLTDebugger.COMMAND_RUN_TO_END)
    {
      //DO NOTHING
    }
    else if ( (_isInputBreakpoint == true) || this._XSLTDebugger.isStylesheetBreakpoint(_currentStylesheetFilename,
        _currentStylesheetLineNumber))
    {
      
      XSLTStatus status = new XSLTStatus(
          this._currentStylesheetFilename,
          this._currentStylesheetLineNumber,
          -1,
          _stylesheetDisplayName,
          _stylesheetLocalName,
          _stylesheetURI,
          _isStylesheetStartTag,
          this._currentInputFilename,
          this._currentInputLineNumber,
          -1,
          _inputDisplayName,
          _inputLocalName,
          _inputURI,
          _isInputStartTag         
          );      
      
      stoppedOn(XSLTDebugger.STOPPED_ON_STYLESHEET_BREAKPOINT, status);
      
      _isInputBreakpoint = false;

    } 
    else if (this._XSLTDebugger.getCommand() == XSLTDebugger.COMMAND_STEP)
    {
      XSLTStatus status = new XSLTStatus(
          this._currentStylesheetFilename,
          this._currentStylesheetLineNumber,
          -1,
          _stylesheetDisplayName,
          _stylesheetLocalName,
          _stylesheetURI,
          _isStylesheetStartTag,
          this._currentInputFilename,
          this._currentInputLineNumber,
          -1,
          _inputDisplayName,
          _inputLocalName,
          _inputURI,
          _isInputStartTag         
          );
      
      stoppedOn(XSLTDebugger.STOPPED_ON_STYLESHEET_LINECHANGE, status);
      
    }
    
    
    //else if ((this._XSLTDebugger.getCommand() == XSLTDebugger.COMMAND_STEP)
    //    && (this._previousStylesheetLineNumber != this._currentStylesheetLineNumber
    //        || this._previousStylesheetFilename == null || !(this._previousStylesheetFilename
    //        .equals(this._currentStylesheetFilename))))
    //{
    //  stoppedOn(XSLTDebugger.STOPPED_ON_STYLESHEET_LINECHANGE,
    //      this._currentStylesheetFilename, this._currentStylesheetLineNumber,
    //      element, false);
    //  
    //}
    
    
    this._previousStylesheetLineNumber = this._currentStylesheetLineNumber;
    this._previousStylesheetFilename = this._currentStylesheetFilename;


  
  }



  public void leave(InstructionInfo instruction)
  {
//System.out.println("leave(InstructionInfo instruction)");

    if (!(instruction instanceof StyleElement)) { 
      return; 
    }
    
    _isStylesheetStartTag = false;
    
    StyleElement styleElement = (StyleElement) instruction;
    
    String previousStylesheetLocalName = _stylesheetLocalName;
    String previousStylesheetURI = _stylesheetURI;

    NamePool np = styleElement.getNamePool();
    int code = styleElement.getFingerprint();
    _stylesheetLocalName = np.getLocalName(code);
    _stylesheetURI = np.getURI(code);
    _stylesheetDisplayName = np.getDisplayName(code);
    
    //_stylesheetLocalName = styleElement.getLocalName();
    //_stylesheetURI = styleElement.getURI();
    //_stylesheetDisplayName = styleElement.getDisplayName();

    this._currentStylesheetFilename = Breakpoint.normalizeFilename(styleElement
        .getSystemId());
    this._currentStylesheetLineNumber = styleElement.getLineNumber();
    //THOMAS
    this._currentStylesheetColumnNumber = -1;
    System.out.println("Trace: "+_stylesheetDisplayName+" Line: "+_currentStylesheetLineNumber+" Col: "+styleElement.getColumnNumber());
    
    
    if (styleElement instanceof XSLTemplate)
    {
 
      _xslTemplateStack.pop();
      _mixedStack.pop();

      if (this._XSLTDebugger.isTracingEnabled())
      {
       
	      String match = null;
	      match = styleElement.getAttributeValue("match");
	      String name = null;
	      name = styleElement.getAttributeValue("name");
	      String priority = null;
	      priority = styleElement.getAttributeValue("priority");
	      String mode = null;
	      mode = styleElement.getAttributeValue("mode");
	            
	      String styleElementStr = _stylesheetDisplayName;
	      if (name != null) styleElementStr += " name=\"" + name + "\"";
	      if (match != null) styleElementStr += " match=\"" + match + "\"";
	      if (priority != null)
	          styleElementStr += " priority=\"" + priority + "\"";
	      if (mode != null) styleElementStr += " mode=\"" + mode + "\"";
	      
            //System.out.println(name + "; " + match + "; " + priority + "; " + mode );	      
	      
	      DefaultStackItem dsi = new DefaultStackItem(styleElementStr,
	          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
	          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_XSL_END);
	       
	      _xslTraceStack.push(dsi);
	      _mixedTraceStack.push(dsi);
      }
    }
   else if (_stylesheetURI.equals(XSLTDebugger.XSL_URI) && _stylesheetLocalName.equals("element"))
    {
      _outStack.pop();
      _mixedStack.pop();

      if (this._XSLTDebugger.isTracingEnabled())
      {
	
	      DefaultStackItem dsi = new DefaultStackItem(_stylesheetDisplayName,
	          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
	          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_XML_END);
	      
	      _outTraceStack.push(dsi);
	      _mixedTraceStack.push(dsi);
      }      
      
    } 
    else if (_stylesheetURI.equals(XSLTDebugger.XSL_URI))
    {
      _mixedStack.pop();
      
      if (this._XSLTDebugger.isTracingEnabled())
      {
	
	      DefaultStackItem dsi = new DefaultStackItem(_stylesheetDisplayName,
	          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
	          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_XSL_END);
	      
	      _xslTraceStack.push(dsi);
	      _mixedTraceStack.push(dsi);
      }
      
    } 
    else if (!(_stylesheetURI.equals(XSLTDebugger.XSL_URI)))
    {
      _outStack.pop();
      _mixedStack.pop();
      
      if (this._XSLTDebugger.isTracingEnabled())
      {

          DefaultStackItem dsi = new DefaultStackItem(_stylesheetDisplayName,
          this._currentStylesheetFilename, this._currentStylesheetLineNumber,
          this._currentStylesheetColumnNumber, XSLTDebugger.STACK_ITEM_OUT_END);
	      
	      _outTraceStack.push(dsi);
	      _mixedTraceStack.push(dsi);
      }            
    }
    
    




    Saxon2StyleStackItem ssi = (Saxon2StyleStackItem) this._styleStack.peek();

    if (handleImmediateStop() == true)
    {
      //System.out.println("***set command to STEP");
      this._XSLTDebugger.command(XSLTDebugger.COMMAND_STEP);
    }
    if (this._XSLTDebugger.isOutputRedirected())
    {
	    if (_stylesheetURI.equals(XSLTDebugger.XSL_URI) && (_stylesheetLocalName.equals("document") || _stylesheetLocalName.equals("result-document")) )
	    {
	      //String href = element.getAttributeValue("", "href");
	      handleOutputDocumentClose(styleElement);
	
	    }
    }
    switch (this._XSLTDebugger.getCommand())
    {
      case XSLTDebugger.COMMAND_STEP_OVER:
         if (this._ssiTemplateEnd != null && this._ssiTemplateEnd == ssi)
        {
          this._XSLTDebugger.command(XSLTDebugger.COMMAND_STEP);
        }
        break;
      case XSLTDebugger.COMMAND_STEP_OUT_OF_TEMPLATE:
        if (this._ssiTemplateEnd != null && this._ssiTemplateEnd == ssi)
        {
         this._XSLTDebugger.command(XSLTDebugger.COMMAND_STEP);
         }
        break;
        
        
      case XSLTDebugger.COMMAND_STEP:  
        
    
        if (previousStylesheetLocalName.equals(_stylesheetLocalName) 
               && previousStylesheetURI.equals(_stylesheetURI) )
		{ 
          // do nothing - empty element so don't report to gui twice
		}
       else
       {
		  XSLTStatus status = new XSLTStatus(
		      this._currentStylesheetFilename,
		      this._currentStylesheetLineNumber,
          -1,
          _stylesheetDisplayName,
          _stylesheetLocalName,
          _stylesheetURI,
          _isStylesheetStartTag,
          this._currentInputFilename,
          this._currentInputLineNumber,
          -1,
          _inputDisplayName,
          _inputLocalName,
          _inputURI,
          _isInputStartTag         
          );      
   
          stoppedOn(XSLTDebugger.STOPPED_ON_STYLESHEET_LINECHANGE, status);
		}
		break;        
        
    }

    this._styleStack.pop();




  }



  public void startCurrentItem(Item currentItem)
  {
//System.out.println("startCurrentItem(Item currentItem)");
    if (!(currentItem  instanceof NodeInfo))
      return;

    NodeInfo element = (NodeInfo)currentItem;

    //System.out.println("el: " + element.getDisplayName() + "  linenum: " + element.getLineNumber());

    if (element.getDisplayName() !=null &&  element.getDisplayName() !="" && element.getLineNumber() != 0)
    {
    NamePool np = element.getConfiguration().getNamePool();
    // may not be equivelent
    int code = element.getSchemaType().getFingerprint();

    _isInputStartTag = true;
    _inputLocalName = np.getLocalName(code);
    _inputURI = np.getURI(code);
    _inputDisplayName = np.getDisplayName(code);
    this._currentInputLineNumber = element.getLineNumber();

    if (_xslFilesMap.get(Breakpoint.normalizeFilename(element
        .getSystemId())) != null)
      return;

    }
    else
    {
    _isInputStartTag = true;
    _inputLocalName = "#document";
    _inputURI = "#document";
    _inputDisplayName = "#document";

    this._currentInputLineNumber = 1;

    }



    this._currentInputFilename = Breakpoint.normalizeFilename(element
        .getSystemId());


    if (element.getNodeKind() == Type.ELEMENT)
    {
      DefaultStackItem dsi = new DefaultStackItem(_inputDisplayName,
          this._currentInputFilename, this._currentInputLineNumber,
          this._currentInputColumnNumber, XSLTDebugger.STACK_ITEM_XML_START);
      
      _xmlStack.push(dsi);
      _mixedStack.push(dsi);
      
      
      
      if (this._XSLTDebugger.isTracingEnabled())
      {
        _xmlTraceStack.push(dsi);
        _mixedTraceStack.push(dsi);     
        
      }
      
    }
 
    if (this._XSLTDebugger.getCommand() == XSLTDebugger.COMMAND_RUN_TO_END)
    {
      // do nothing!!!
    }
    else if (this._XSLTDebugger.isInputBreakpoint(this._currentInputFilename,
        this._currentInputLineNumber))
    {
      // dont stop yet (only stop in stylesheet)
      _isInputBreakpoint = true;
      //stoppedOn(XSLTDebugger.STOPPED_ON_INPUT_BREAKPOINT, this._currentInputFilename,
      //    this._currentInputLineNumber, element, false);
    } 
 
    this._previousInputLineNumber = this._currentInputLineNumber;
    this._previousInputFilename = this._currentInputFilename;
   

  }

  public void endCurrentItem(Item currentItem)
  {
//System.out.println("endCurrentItem(Item currentItem)");
    if (!(currentItem  instanceof NodeInfo))
      return;

    NodeInfo element = (NodeInfo)currentItem;

    //System.out.println("el: " + element.getDisplayName() + "  linenum: " + element.getLineNumber());


    this._currentInputFilename = Breakpoint.normalizeFilename(element
        .getSystemId());
    this._currentInputLineNumber = element.getLineNumber();


    if (_xslFilesMap.get(this._currentInputFilename) != null)
      return;


    if (element.getDisplayName() !=null &&  element.getDisplayName() !="" && element.getLineNumber() != 0)
    {
    NamePool np = element.getConfiguration().getNamePool();
    // may not be equivelent
    int code = element.getSchemaType().getFingerprint();

    _isInputStartTag = true;
    _inputLocalName = np.getLocalName(code);
    _inputURI = np.getURI(code);
    _inputDisplayName = np.getDisplayName(code);
     
    }
    else
    {
    _isInputStartTag = true;
    _inputLocalName = "#document";
    _inputURI = "#document";
    _inputDisplayName = "#document";

    }


    if (element.getNodeKind() == Type.ELEMENT)
    {
      _xmlStack.pop();
      _mixedStack.pop();
      
      
      if (this._XSLTDebugger.isTracingEnabled())
      {

      DefaultStackItem dsi = new DefaultStackItem(_inputDisplayName,
          this._currentInputFilename, this._currentInputLineNumber,
          this._currentInputColumnNumber, XSLTDebugger.STACK_ITEM_XML_END);
      
      _xmlTraceStack.push(dsi);
      _mixedTraceStack.push(dsi);

      }
    }
    
    _isInputBreakpoint = false;


  }




  /**
   * Called after a node of the source tree got processed
   */

/*
  public void leaveSource(NodeHandler handler, Context context)
  {
    NodeInfo element = context.getContextNodeInfo();
    
    this._currentInputFilename = Breakpoint.normalizeFilename(element
        .getSystemId());
    this._currentInputLineNumber = element.getLineNumber();
    
    if (_xslFilesMap.get(this._currentInputFilename) != null)
      return;

    _isInputStartTag = false;
    _inputLocalName = element.getLocalName();
    _inputURI = element.getURI();
    _inputDisplayName = element.getDisplayName();
    
    if (element.getNodeType() == NodeInfo.ELEMENT)
    {
      _xmlStack.pop();
      _mixedStack.pop();
      
      
      if (this._XSLTDebugger.isTracingEnabled())
      {

      DefaultStackItem dsi = new DefaultStackItem(_inputDisplayName,
          this._currentInputFilename, this._currentInputLineNumber,
          this._currentInputColumnNumber, XSLTDebugger.STACK_ITEM_XML_END);
      
      _xmlTraceStack.push(dsi);
      _mixedTraceStack.push(dsi);

      }
    }
    
    _isInputBreakpoint = false;

     //
     // indent = indent.substring(0, indent.length() - 1);
     // System.err.println(indent + " </Source> <!-- " +
     // Navigator.getPath(context.getContextNodeInfo()) + " -->");
     //
  }
*/

  
 /* 
  String getModeName(Context context)
  {
    int nameCode = context.getMode().getNameCode();
    if (nameCode == -1)
    {
      return "";
    } else
    {
      return context.getController().getNamePool().getDisplayName(nameCode);
    }
  }
*/

  public void setXSLTDebugger(Saxon2Debugger debugger)
  {
    this._XSLTDebugger = debugger;
  }

  public void setStyleStack(Stack stack)
  {
    this._styleStack = stack;
  }

  public void setXMLStack(Stack stack)
  {
    this._xmlStack = stack;
  }

  public void setOutStack(Stack stack)
  {
    this._outStack = stack;
  }

  public void setMixedStack(Stack stack)
  {
    this._mixedStack = stack;
  }

  public void setXSLTemplateStack(Stack stack)
  {
    this._xslTemplateStack = stack;
  }

  
  public void setXMLTraceStack(Stack stack)
  {
    this._xmlTraceStack = stack;
  }

  public void setOutTraceStack(Stack stack)
  {
    this._outTraceStack = stack;
  }

  public void setMixedTraceStack(Stack stack)
  {
    this._mixedTraceStack = stack;
  }

  public void setXSLTraceStack(Stack stack)
  {
    this._xslTraceStack = stack;
  }
  
  public void setXSLTemplateProfileMap(HashMap xslTemplatesProfileMap)
  {
    this._xslTemplatesProfileMap = xslTemplatesProfileMap;
  }

  public String getVariableValue(String variableName)
  {

/*
    Context ctx = ((Saxon2StyleStackItem) this._styleStack.peek())._context;
    StyleElement se = ((Saxon2StyleStackItem) this._styleStack.peek())._styleElement;
    Controller c = ctx.getController();
    StaticContext sc = ctx.getStaticContext();
    if (sc == null) return null;
    try
    {
      Bindery bry = ctx.getBindery();
      Binding bng = se.getVariableBinding(sc.makeNameCode(variableName, false));
      if (bng != null)
      {
        Value v = bry.getValue(bng, bry.getFrameId());
        if (v != null) return v.asString();
      }
    } catch (Exception ex)
    {
    }
*/

    return null;
  }



  public void changeOutputDestination(StyleElement styleElement, XPathContext context)
  {

    //Outputter op = null;
    Controller con = context.getController();

    StreamResult sr = new StreamResult(new OutputStreamWriter(
        this._XSLTDebugger._outputStream));

    XMLEmitter emitter = new XMLEmitter();

    try
    {
        emitter.setStreamResult(this._XSLTDebugger._sresult);
    	ParseOptions options = new ParseOptions();
        options.setSchemaValidationMode(Validation.STRICT);

        //TODO: Find replacement
    	//context.changeOutputDestination(emitter, options);
    } 
	catch (Exception ex)
    {
      System.out.println("****exception" + ex.getMessage());
    }
    newdoc = false;

  }

  public void setOutputDestination(Logger l){
    return;
  }

  public void handleOutputDocumentClose(StyleElement styleElement)
  {
    this._XSLTDebugger.onCloseOutputDocument("");
  }




  public void handleOutputDocumentOpen(StyleElement styleElement, XPathContext context)
  {
    

  
    newdoc = true;
    //changeOutputDestination(element, context);
    //System.err.println(element.getDisplayName() + " " +
    // element.getAttributeValue("", "href") + " " + element.getSystemId()
    // + " " + element.getLineNumber());


    String href = styleElement.getAttributeValue("{}href");
    String href2 = styleElement.getAttributeValue("href");
    String temp = href;
	//System.out.println("href: " + href);
	//System.out.println("href2: " + href2);

 
    int n1 = 0;
    int n2 = 0;
    String fn = "";
    //System.err.println(temp + " n1 = " + temp.indexOf("{") + " n2 = " + temp.indexOf("}") );
    try
    {
      //Controller c = context.getController();
      //StaticContext sc = context.getStaticContext();

      while ((n1 = temp.indexOf("{")) >= 0 && (n2 = temp.indexOf("}")) > n1)
      {
        String variableName = temp.substring(n1 + 2, n2);
        String val = "";
        //System.err.println("variableName = ***" + variableName + "****");
        fn += temp.substring(0, n1);
        //System.err.println("initial fn = " + fn);


	if (this._XSLTDebugger == null)
		//System.out.println("***********null xsltdebugger ***********");
try
{
	Vector locvars = this._XSLTDebugger.getLocalVariables();

	if (locvars != null)
	{
	 //System.out.println("locvars  length: " + locvars.size());

	 for (int k = 0; k<locvars.size();k++)
	 {
		XSLTVariable xslvar = (XSLTVariable)(locvars.get(k));

		//System.out.println("var name: " + xslvar.getName() + "   var value: " +xslvar.getValue());

		if (xslvar.getName().equals(variableName))
		{
			val = xslvar.getValue();
			break;
		}
	 }
	}
}
catch (Exception ex) { System.out.println(ex.getMessage());}



	if (val.equals(""))
	{
 	 Vector globvars = this._XSLTDebugger.getGlobalVariables();

	 if (globvars != null)
	 {
	  //System.out.println("globvars length: " + globvars.size());
	  for (int l = 0; l<globvars.size();l++)
	  {
		XSLTVariable xslvar = (XSLTVariable)globvars.get(l);

		//System.out.println("global var name: " + xslvar.getName() + "   global var value: " +xslvar.getValue());

		if (xslvar.getName().equals(variableName))
		{
			val = xslvar.getValue();
			break;
		}
	  }
	 }	 
	}

/*
        Bindery bry = context.getBindery();
        Binding bng = ((StyleElement) element).getVariableBinding(sc
            .makeNameCode(variableName, false));
        if (bng != null)
        {
          Value v = bry.getValue(bng, bry.getFrameId());
          if (v != null) val = v.asString();
        }
*/
        //System.err.println("variableName = " + variableName + " val
        // = " + val);

        fn += val;
        //System.err.println("fn = " + fn);
        temp = temp.substring(n2 + 1);
      }
    } catch (Exception ex)
    {
    }
    fn += temp;
    //System.err.println("output filename = " + fn);
    this._XSLTDebugger.onOpenOutputDocument(fn);


  }



  public synchronized boolean handleImmediateStop()
  {
     boolean isStop = this._XSLTDebugger.handleImmediateStop();
  
     return isStop;
  }

  public synchronized void stoppedOn(int stopReason, XSLTStatus status)
  {
    
    //this._XSLTDebugger.stoppedOn(stopReason, status._stylesheetFilename, status._stylesheetLineNumber, status._stylesheetDisplayName, status._isStylesheetStartTag);
    this._XSLTDebugger.stoppedOn(stopReason,status);

    //this._XSLTDebugger.stoppedOn(stopReason, status);
    
    switch (this._XSLTDebugger.getCommand())
    {
      case XSLTDebugger.COMMAND_STEP_OVER:
        if ((status._stylesheetURI.equals(XSLTDebugger.XSL_URI) && status._stylesheetLocalName.equals("apply-templates"))
            || (status._stylesheetURI.equals(XSLTDebugger.XSL_URI) && status._stylesheetLocalName.equals("call-template")))
        {
          this._ssiTemplateEnd = (Saxon2StyleStackItem) this._styleStack.peek();
        } 
        else
        {
          this._XSLTDebugger.command(XSLTDebugger.COMMAND_STEP);
        }
        break;
        
      case XSLTDebugger.COMMAND_STEP_OUT_OF_TEMPLATE:
        for (int i = this._styleStack.size() - 1; i >= 0; i--)
        {
          Saxon2StyleStackItem ssi = (Saxon2StyleStackItem) this._styleStack.get(i);
          if (ssi._isTemplateElement == true)
          {
            //if (i == this._styleStack.size() - 1)
           // {
            //  continue;
           // } 
            //else
            //{
              this._ssiTemplateEnd = ssi;
              break;
            //}
          }
        }
        break;
        
      default:
        break;
    }
  }
  
  public void cleanUp() {
    _XSLTDebugger = null;
    
    _inputDisplayName = null;
    _inputLocalName = null;
    _inputURI = null;
   
    _stylesheetDisplayName = null;
    _stylesheetLocalName = null;
    _stylesheetURI = null;
   
    _currentStylesheetFilename = null;
    _previousStylesheetFilename = null;
    _currentInputFilename = null;
    _previousInputFilename = null;
    
    _styleStack = null;
    _ssiTemplateEnd = null;
    _xslTemplateStack = null;
    _xmlStack = null;
    _outStack = null;
    _mixedStack = null;
   
    _xslTraceStack = null;
    _xmlTraceStack = null;
    _outTraceStack = null;
    _mixedTraceStack = null;
    
    _xslTemplatesProfileMap = null;
    
    _xslFilesMap = null;
  }
}