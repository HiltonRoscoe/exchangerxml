/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package com.cladonia.xml.svg;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.apps.svgbrowser.FindDialog;
import org.apache.batik.apps.svgbrowser.SVGInputHandler;
import org.apache.batik.apps.svgbrowser.SquiggleInputHandler;
import org.apache.batik.apps.svgbrowser.StatusBar;
import org.apache.batik.apps.svgbrowser.ThumbnailDialog;
import org.apache.batik.apps.svgbrowser.TransformHistory;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.dom.StyleSheetProcessingInstruction;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.ext.swing.JAffineTransformChooser;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.AbstractImageZoomInteractor;
import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.swing.gvt.AbstractRotateInteractor;
import org.apache.batik.swing.gvt.AbstractZoomInteractor;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.gvt.Interactor;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherListener;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.Service;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.apps.svgbrowser.DOMViewer;
import org.apache.batik.util.gui.URIChooser;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.JComponentModifier;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;
import org.bounce.event.PopupListener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.svg.SVGDocument;

import com.cladonia.xngreditor.ExchangerEditor;
import com.cladonia.xngreditor.MessageHandler;
import com.cladonia.xngreditor.XngrImageLoader;

/**
 * This class represents a SVG viewer swing frame.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: SVGViewerDialog.java,v 1.2 2004/10/23 15:11:48 edankert Exp $
 */
public class SVGViewerDialog extends JDialog implements ActionMap, SVGDocumentLoaderListener, GVTTreeBuilderListener, SVGLoadEventDispatcherListener, GVTTreeRendererListener, LinkActivationListener, UpdateManagerListener {

    /**
     * The gui resources file name
     */
    public final static String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.GUI";

    // The actions names.
    public final static String RELOAD_ACTION = "ReloadAction";
    public final static String FULL_SCREEN_ACTION = "FullScreenAction";
    public final static String ESCAPE_ACTION = "EscapeAction";
    public final static String PRINT_ACTION = "PrintAction";
    public final static String EXPORT_AS_JPG_ACTION = "ExportAsJPGAction";
    public final static String EXPORT_AS_PNG_ACTION = "ExportAsPNGAction";
    public final static String EXPORT_AS_TIFF_ACTION = "ExportAsTIFFAction";
    public final static String CLOSE_ACTION = "CloseAction";
    public final static String RESET_TRANSFORM_ACTION = "ResetTransformAction";
    public final static String PREVIOUS_TRANSFORM_ACTION = "PreviousTransformAction";
    public final static String NEXT_TRANSFORM_ACTION = "NextTransformAction";
    public final static String USE_STYLESHEET_ACTION = "UseStylesheetAction";
    public final static String PLAY_ACTION = "PlayAction";
    public final static String PAUSE_ACTION = "PauseAction";
    public final static String STOP_ACTION = "StopAction";
    public final static String SET_TRANSFORM_ACTION = "SetTransformAction";
    public final static String THUMBNAIL_DIALOG_ACTION = "ThumbnailDialogAction";
    public final static String FLUSH_ACTION = "FlushAction";

    // The cursor indicating that an operation is pending.
    public final static Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
    public final static Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

    // Name for the os-name property
//    public final static String PROPERTY_OS_NAME = Resources.getString( "JSVGViewerFrame.property.os.name");
//    public final static String PROPERTY_OS_NAME_DEFAULT = Resources.getString("JSVGViewerFrame.property.os.name.default");
//    public final static String PROPERTY_OS_WINDOWS_PREFIX = Resources.getString("JSVGViewerFrame.property.os.windows.prefix");

    protected static Vector handlers = null;

    protected static SVGInputHandler defaultHandler = new SVGInputHandler();

    protected static ResourceBundle bundle = null;

    protected static ResourceManager resources = null;

    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }

    private JSVGCanvas svgCanvas	= null;
    private JPopupMenu popup		= null;
	private JMenu stylesheetMenu	= null;
    private JPanel svgCanvasPanel	= null;
    private JWindow window			= null;

    private File currentPath 		= new File("");
    private File currentExportPath	= new File("");

    private PlayAction playAction		= new PlayAction();
    private PauseAction pauseAction 	= new PauseAction();
    private StopAction stopAction 		= new StopAction();

    /**
     * The previous transform action
     */
    private PreviousTransformAction previousTransformAction = new PreviousTransformAction();

    /**
     * The next transform action
     */
    protected NextTransformAction nextTransformAction = new NextTransformAction();

    /**
     * The use (author) stylesheet action
     */
    protected UseStylesheetAction useStylesheetAction = new UseStylesheetAction();

    /**
     * The debug flag.
     */
    protected boolean debug = false;

    /**
     * The auto adjust flag.
     */
    protected boolean autoAdjust = true;

    /**
     * Whether the update manager was stopped.
     */
    protected boolean managerStopped;

    /**
     * The SVG user agent.
     */
    protected SVGUserAgent userAgent = new UserAgent();

    /**
     * The current document.
     */
    protected SVGDocument svgDocument;

    /**
     * The URI chooser.
     */
    protected URIChooser uriChooser;

    /**
     * The DOM viewer.
     */
    protected DOMViewer domViewer;

    /**
     * The Find dialog.
     */
    protected FindDialog findDialog;

    /**
     * The Find dialog.
     */
    protected ThumbnailDialog thumbnailDialog;

    /**
     * The transform dialog
     */
    protected JAffineTransformChooser.Dialog transformDialog;

    /**
     * The status bar.
     */
    protected StatusBar statusBar;

    /**
     * The initial frame title.
     */
    protected String title;

    /**
     * The local history.
     */
//    protected History history = null;

    /**
     * The transform history.
     */
    protected TransformHistory transformHistory = new TransformHistory();

    /**
     * The alternate style-sheet title.
     */
    protected String alternateStyleSheet;
	
	private ExchangerEditor parent = null;

	private Interactor defaultInteractor = null;

	private Interactor imageZoomInteractor = new AbstractImageZoomInteractor() {
	    public boolean startInteraction(InputEvent ie) {
	        int mods = ie.getModifiers();
	        return ie.getID() == MouseEvent.MOUSE_PRESSED && (mods & InputEvent.BUTTON1_MASK) != 0;
	    }
	};

    private Interactor panInteractor = new AbstractPanInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return ie.getID() == MouseEvent.MOUSE_PRESSED && (mods & InputEvent.BUTTON1_MASK) != 0;
        }
    };

    private Interactor rotateInteractor = new AbstractRotateInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return ie.getID() == MouseEvent.MOUSE_PRESSED && (mods & InputEvent.BUTTON1_MASK) != 0;
        }
    };

    private Interactor zoomInteractor = new AbstractZoomInteractor() {
        public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiers();
            return ie.getID() == MouseEvent.MOUSE_PRESSED && (mods & InputEvent.BUTTON1_MASK) != 0;
        }
    };

    /**
     * Creates a new SVG viewer frame.
     */
    public SVGViewerDialog( ExchangerEditor parent) {
		super( parent, false);

        this.parent = parent;
		
//		history = new History();
		
		setTitle( "SVG Viewer");

        addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
				// reset the current document...
				svgCanvas.stopProcessing();
				hide();
				//setVisible(false);
            }
        });
		
		setDefaultCloseOperation( HIDE_ON_CLOSE);

        //
        // Set the frame's maximum size so that content
        // bigger than the screen does not cause the creation
        // of unnecessary large images.
        //
        svgCanvas = new JSVGCanvas( userAgent, true, true) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                
                {
                    setMaximumSize(screenSize);
                }
                
                public Dimension getPreferredSize(){
                    Dimension s = super.getPreferredSize();
                    if (s.width > screenSize.width) s.width =screenSize.width;
                    if (s.height > screenSize.height) s.height = screenSize.height;
                    return s;
                }
                
            };
        
        javax.swing.ActionMap map = svgCanvas.getActionMap();
        listeners.put( FULL_SCREEN_ACTION, new FullScreenAction());
        map.put( FULL_SCREEN_ACTION, getAction( FULL_SCREEN_ACTION));
        javax.swing.InputMap imap = svgCanvas.getInputMap( JComponent.WHEN_FOCUSED);
        KeyStroke key = KeyStroke.getKeyStroke( KeyEvent.VK_F11, 0);
        imap.put( key, FULL_SCREEN_ACTION);

        svgCanvas.getActionMap().put( ESCAPE_ACTION, new EscapeAction());
        svgCanvas.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0, false), ESCAPE_ACTION);

        svgCanvas.setDoubleBufferedRendering(true);

        listeners.put( RELOAD_ACTION, new ReloadAction());
        listeners.put( PRINT_ACTION, new PrintAction());
        listeners.put( EXPORT_AS_JPG_ACTION, new ExportAsJPGAction());
        listeners.put( EXPORT_AS_PNG_ACTION, new ExportAsPNGAction());
        listeners.put( EXPORT_AS_TIFF_ACTION, new ExportAsTIFFAction());

        javax.swing.ActionMap cMap = svgCanvas.getActionMap();

        listeners.put( RESET_TRANSFORM_ACTION, cMap.get(JSVGCanvas.RESET_TRANSFORM_ACTION));
        listeners.put( PREVIOUS_TRANSFORM_ACTION, previousTransformAction);

        key = KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK);
        imap.put(key, previousTransformAction);

        listeners.put( NEXT_TRANSFORM_ACTION, nextTransformAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK);
        imap.put(key, nextTransformAction);

        listeners.put(USE_STYLESHEET_ACTION, useStylesheetAction);
        listeners.put(PLAY_ACTION, playAction);
        listeners.put(PAUSE_ACTION, pauseAction);
        listeners.put(STOP_ACTION, stopAction);

        listeners.put(SET_TRANSFORM_ACTION, new SetTransformAction());

        key = KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK);
        svgCanvas.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put( key, getAction( USE_STYLESHEET_ACTION));

        listeners.put(THUMBNAIL_DIALOG_ACTION, new ThumbnailDialogAction());
        listeners.put(FLUSH_ACTION, new FlushAction());

        JPanel p = new JPanel(new BorderLayout());

        ToggleButtonToolBar tb = new ToggleButtonToolBar();
        tb.setFloatable(false);
        getContentPane().add( p, BorderLayout.NORTH);
        p.add(tb, BorderLayout.NORTH);
		
		tb.add( getAction( PRINT_ACTION));
		tb.addSeparator();
		tb.add( getAction( RELOAD_ACTION));

		tb.addSeparator();
		tb.add( getAction( PLAY_ACTION));
		tb.add( getAction( PAUSE_ACTION));
		tb.add( getAction( STOP_ACTION));
		tb.addSeparator();
		ButtonGroup group = new ButtonGroup();
		SelectAction selectAction = new SelectAction();
		ZoomAction zoomAction = new ZoomAction();
		PanAction panAction = new PanAction();
		RotateAction rotateAction = new RotateAction();
		ZoomSelectionAction zoomSelectionAction = new ZoomSelectionAction();
		
		JToggleButton button = tb.addToggleButton( selectAction);
		selectAction.addToggle( button);
		group.add( button);

		button = tb.addToggleButton( zoomSelectionAction);
		zoomSelectionAction.addToggle( button);
		group.add( button);
		
		button = tb.addToggleButton( panAction);
		panAction.addToggle( button);
		group.add( button);

		button = tb.addToggleButton( rotateAction);
		rotateAction.addToggle( button);
		group.add( button);

		button = tb.addToggleButton( zoomAction);
		zoomAction.addToggle( button);
		group.add( button);

		tb.addSeparator();
		tb.add( getAction( FULL_SCREEN_ACTION));

		popup = new JPopupMenu();
		group = new ButtonGroup();
		
		JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem( selectAction);
		selectAction.addToggle( menuItem);
		group.add( menuItem);
		popup.add( menuItem);

		menuItem = new JRadioButtonMenuItem( zoomSelectionAction);
		zoomSelectionAction.addToggle( menuItem);
		group.add( menuItem);
		popup.add( menuItem);

		menuItem = new JRadioButtonMenuItem( panAction);
		panAction.addToggle( menuItem);
		group.add( menuItem);
		popup.add( menuItem);

		menuItem = new JRadioButtonMenuItem( rotateAction);
		rotateAction.addToggle( menuItem);
		group.add( menuItem);
		popup.add( menuItem);

		menuItem = new JRadioButtonMenuItem( zoomAction);
		zoomAction.addToggle( menuItem);
		group.add( menuItem);
		popup.add( menuItem);

		popup.addSeparator();

		popup.add( getAction( PLAY_ACTION));
		popup.add( getAction( PAUSE_ACTION));
		popup.add( getAction( STOP_ACTION));
		
		popup.addSeparator();
		stylesheetMenu = new JMenu( "Stylesheets");
		popup.add( stylesheetMenu);
		popup.addSeparator();
		popup.add( getAction( RELOAD_ACTION));
		
		svgCanvas.addMouseListener( new PopupListener() {
			public void popupTriggered( MouseEvent e) {
				popup.show( svgCanvas, e.getX(), e.getY());
			}
		});

		selectAction.setSelected( true);

        svgCanvasPanel = new JPanel(new BorderLayout());
        svgCanvasPanel.setBorder(BorderFactory.createEtchedBorder());

        svgCanvasPanel.add(svgCanvas, BorderLayout.CENTER);
        p = new JPanel(new BorderLayout());
        p.add( svgCanvasPanel, BorderLayout.CENTER);
        p.add(statusBar = new StatusBar(), BorderLayout.SOUTH);

        getContentPane().add(p, BorderLayout.CENTER);

        svgCanvas.addSVGDocumentLoaderListener( this);
        svgCanvas.addGVTTreeBuilderListener( this);
        svgCanvas.addSVGLoadEventDispatcherListener( this);
        svgCanvas.addGVTTreeRendererListener( this);
        svgCanvas.addLinkActivationListener( this);
        svgCanvas.addUpdateManagerListener( this);

        svgCanvas.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    if (svgDocument == null) {
                        statusBar.setXPosition(e.getX());
                        statusBar.setYPosition(e.getY());
                    } else {
                        try {
                            AffineTransform at = svgCanvas.getRenderingTransform();
                            if (at != null) {
                                at = at.createInverse();
                                Point2D p2d = at.transform( new Point2D.Float(e.getX(), e.getY()), null);
                                statusBar.setXPosition((float)p2d.getX());
                                statusBar.setYPosition((float)p2d.getY());
                                return;
                            }
                        } catch (NoninvertibleTransformException ex) {
                        }
                        statusBar.setXPosition(e.getX());
                        statusBar.setYPosition(e.getY());
                    }
                }
            });
        svgCanvas.addMouseListener(new MouseAdapter() {
                public void mouseExited(MouseEvent e) {
                    Dimension dim = svgCanvas.getSize();
                    if (svgDocument == null) {
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    } else {
                        try {
                            AffineTransform at = svgCanvas.getRenderingTransform();
                            if (at != null) {
                                at = at.createInverse();
                                Point2D o = at.transform( new Point2D.Float(0, 0), null);
                                Point2D p2d = at.transform( new Point2D.Float(dim.width, dim.height), null);
                                statusBar.setWidth((float)(p2d.getX() - o.getX()));
                                statusBar.setHeight((float)(p2d.getY() - o.getY()));
                                return;
                            }
                        } catch (NoninvertibleTransformException ex) {
                        }
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    }
                }
            });

        svgCanvas.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    Dimension dim = svgCanvas.getSize();
                    if (svgDocument == null) {
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    } else {
                        try {
                            AffineTransform at = svgCanvas.getRenderingTransform();
                            if (at != null) {
                                at = at.createInverse();
                                Point2D o = at.transform(new Point2D.Float(0, 0), null);
                                Point2D p2d = at.transform(new Point2D.Float(dim.width, dim.height), null);
                                statusBar.setWidth((float)(p2d.getX() - o.getX()));
                                statusBar.setHeight((float)(p2d.getY() - o.getY()));
                                return;
                            }
                        } catch (NoninvertibleTransformException ex) {
                        }
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    }
                }
            });
    }

	private void setDefaultInteractor( Interactor interactor) {
		if ( defaultInteractor != null) {
			List intl = svgCanvas.getInteractors();
			intl.remove( defaultInteractor);
		}
		
		defaultInteractor = interactor;
		
		if ( defaultInteractor != null) {
			List intl = svgCanvas.getInteractors();
			intl.add( defaultInteractor);
		}
	}
	
    /**
     * Whether to show the debug traces.
     */
    public void setDebug(boolean b) {
        debug = b;
    }

    /**
     * Whether to auto adjust the canvas to the size of the document.
     */
    public void setAutoAdjust(boolean b) {
        autoAdjust = b;
    }

    /**
     * Returns the main JSVGCanvas of this frame.
     */
    public JSVGCanvas getJSVGCanvas() {
        return svgCanvas;
    }

    /**
     * Needed to work-around JFileChooser bug with abstract Files
     */
    private static File makeAbsolute(File f){
        if(!f.isAbsolute()){
            return f.getAbsoluteFile();
        }
        return f;
    }

    /**
     * To open a new file.
     */
//    public class OpenAction extends AbstractAction {

//      public OpenAction() {
//        }
//        public void actionPerformed(ActionEvent e) {
//            JFileChooser fileChooser = null;
//
//            // Apply work around Windows problem when security is enabled, 
//            // and when prior to JDK 1.4.
//            String os = System.getProperty(PROPERTY_OS_NAME, PROPERTY_OS_NAME_DEFAULT);
//            SecurityManager sm = System.getSecurityManager();
//            
//            if ( priorJDK1_4 && sm != null && os.indexOf(PROPERTY_OS_WINDOWS_PREFIX) != -1 ){
//                fileChooser = new JFileChooser(makeAbsolute(currentPath),
//                                               new WindowsAltFileSystemView());
//            } else {
//                fileChooser = new JFileChooser(makeAbsolute(currentPath));
//            }
//
//            fileChooser.setFileHidingEnabled(false);
//            fileChooser.setFileSelectionMode
//                (JFileChooser.FILES_ONLY);
//
//            //
//            // Add file filters from the handlers map
//            //
//            Iterator iter = getHandlers().iterator();
//            while (iter.hasNext()) {
//                SquiggleInputHandler handler 
//                    = (SquiggleInputHandler)iter.next();
//                fileChooser.addChoosableFileFilter
//                    (new SquiggleInputHandlerFilter(handler));
//            }
//            
//            int choice = fileChooser.showOpenDialog(JSVGViewerFrame.this);
//            if (choice == JFileChooser.APPROVE_OPTION) {
//                File f = fileChooser.getSelectedFile();
//                
//                currentPath = f;
//                try { 
//                    String furl = f.toURL().toString();
//                    showSVGDocument(furl);
//                } catch (MalformedURLException ex) {
//                    if (userAgent != null) {
//                        userAgent.displayError(ex);
//                    }
//                }
//            }
//        }
//    }

    /**
     * Shows the given document into the viewer frame
     */
    public void showSVGDocument( String uri){
        try {
            ParsedURL purl = new ParsedURL( uri);
            
            getJSVGCanvas().loadSVGDocument( purl.toString());
        } catch (Exception e) {
			e.printStackTrace();
        }

    }

    public void showSVGDocument( SVGDocument document){
        try {
//			history.reset();
            getJSVGCanvas().setSVGDocument( document);
	
	        setSVGDocument( document, document.getURL(), document.getTitle());
        } catch (Exception e) {
    		e.printStackTrace();
        }

    }

    /**
     * Returns the input handler for the given URI
     */
    public SquiggleInputHandler getInputHandler(ParsedURL purl) throws IOException {
        Iterator iter = getHandlers().iterator();
        SquiggleInputHandler handler = null;

        while (iter.hasNext()) {
            SquiggleInputHandler curHandler = (SquiggleInputHandler)iter.next();

            if ( curHandler.accept(purl)) {
                handler = curHandler;
                break;
            }
        }

        // No handler found, use the default one.
        if (handler == null) {
            handler = defaultHandler;
        }

        return handler;
    }


    /**
     * Returns the list of input file handler. 
     */
    protected static Vector getHandlers() {
        if (handlers != null) {
            return handlers;
        }

        handlers = new Vector();
        registerHandler(new SVGInputHandler());
        
        Iterator iter = Service.providers(SquiggleInputHandler.class);
        while (iter.hasNext()) {
            SquiggleInputHandler handler 
                = (SquiggleInputHandler)iter.next();

            registerHandler(handler);
        }

        return handlers;
    }

    /**
     * Registers an input file handler by adding it to the handlers map.
     * @param handler the new input handler to register.
     */
    public static synchronized void registerHandler(SquiggleInputHandler handler) {
        Vector handlers = getHandlers();
        handlers.addElement(handler);
    }

    /**
     * To open a new document.
     */
//    public class OpenLocationAction extends AbstractAction {
//        public OpenLocationAction() {}
//        public void actionPerformed(ActionEvent e) {
//            if (uriChooser == null) {
//                uriChooser = new URIChooser(JSVGViewerFrame.this);
//                uriChooser.setFileFilter(new SVGFileFilter());
//                uriChooser.pack();
//                Rectangle fr = getBounds();
//                Dimension sd = uriChooser.getSize();
//                uriChooser.setLocation(fr.x + (fr.width  - sd.width) / 2,
//                                       fr.y + (fr.height - sd.height) / 2);
//            }
//            if (uriChooser.showDialog() == URIChooser.OK_OPTION) {
//                String s = uriChooser.getText();
//                int i = s.indexOf("#");
//                String t = "";
//                if (i != -1) {
//                    t = s.substring(i + 1);
//                    s = s.substring(0, i);
//                }
//                if (!s.equals("")) {
//                    File f = new File(s);
//                    if (f.exists()) {
//                        if (f.isDirectory()) {
//                            s = null;
//                        } else {
//                            try {
//                                s = f.getCanonicalPath();
//                                if (s.startsWith("/")) {
//                                    s = "file:" + s;
//                                } else {
//                                    s = "file:/" + s;
//                                }
//                            } catch (IOException ex) {
//                            }
//                        }
//                    }
//                    if (s != null) {
//                        if (svgDocument != null) {
//                            ParsedURL docPURL 
//                                = new ParsedURL(svgDocument.getURL());
//                            ParsedURL purl = new ParsedURL(docPURL, s);
//                            String fi = svgCanvas.getFragmentIdentifier();
//                            if (docPURL.equals(purl) && t.equals(fi)) {
//                                return;
//                            }
//                        }
//                        if (t.length() != 0) {
//                            s += "#" + t;
//                        }
//
//                        showSVGDocument(s);
//                    }
//                }
//            }
//        }
//    }

    /**
     * To open a new window.
     */
//    public class NewWindowAction extends AbstractAction {
//        public NewWindowAction() {}
//        public void actionPerformed(ActionEvent e) {
//            JSVGViewerFrame vf = application.createAndShowJSVGViewerFrame();
//
//            // Copy the current settings to the new window.
//            vf.autoAdjust = autoAdjust;
//            vf.debug = debug;
//            vf.svgCanvas.setProgressivePaint(svgCanvas.getProgressivePaint());
//            vf.svgCanvas.setDoubleBufferedRendering
//                (svgCanvas.getDoubleBufferedRendering());
//        }
//    }

    /**
     * To show the preferences.
     */
//    public class PreferencesAction extends AbstractAction {
//        public PreferencesAction() {}
//        public void actionPerformed(ActionEvent e) {
//            application.showPreferenceDialog(JSVGViewerFrame.this);
//        }
//    }

    /**
     * To close the last document.
     */
//    public class CloseAction extends AbstractAction {
//        public CloseAction() {}
//        public void actionPerformed(ActionEvent e) {
//            application.closeJSVGViewerFrame(JSVGViewerFrame.this);
//        }
//    }

//    public class ZoomInAction extends AbstractAction {
//		private Action parent = null;
//		
//        public ZoomInAction( Action parent) {
//            super( "Zoom In");
//			
//			this.parent = parent;
//       
//            putValue( MNEMONIC_KEY, new Integer( 'I'));
//            putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/ZoomIn16.gif"));
//            putValue( SHORT_DESCRIPTION, "Zoom In");
//    	
//    	}
//
//        public void actionPerformed( ActionEvent e) {
//			parent.actionPerformed( e);
//        }
//    }
//
//    public class ZoomOutAction extends AbstractAction {
//    	private Action parent = null;
//    	
//        public ZoomOutAction( Action parent) {
//            super( "Zoom Out");
//    		
//    		this.parent = parent;
//       
//            putValue( MNEMONIC_KEY, new Integer( 'O'));
//            putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/ZoomOut16.gif"));
//            putValue( SHORT_DESCRIPTION, "Zoom Out");
//    	}
//
//        public void actionPerformed( ActionEvent e) {
//    		parent.actionPerformed( e);
//        }
//    }

	public class DefaultMouseAction extends AbstractAction {
		private Vector toggles = null;
		
		public DefaultMouseAction( String string) {
		     super( string);
			
			toggles = new Vector();
		}
		
		public void setSelected( boolean enabled) {
			for ( int i = 0; i < toggles.size(); i++) {
				((AbstractButton)toggles.elementAt( i)).setSelected( enabled);
			}
		}

		public void addToggle( AbstractButton e) {
			toggles.addElement( e);
		}

		public void actionPerformed( ActionEvent e) {
			if ( e.getSource() instanceof AbstractButton) {
				setSelected( ((AbstractButton)e.getSource()).isSelected());
			}
		}
	}
	
    public class ZoomAction extends DefaultMouseAction {
       public ZoomAction() {
            super( "Zoom");
    		
            putValue( MNEMONIC_KEY, new Integer( 'Z'));
            putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Zoom16.gif"));
            putValue( SHORT_DESCRIPTION, "Zoom In and Out");
    	}

        public void actionPerformed( ActionEvent e) {
			setDefaultInteractor( imageZoomInteractor);
			super.actionPerformed( e);
        }
    }

    public class ZoomSelectionAction extends DefaultMouseAction {
       public ZoomSelectionAction() {
            super( "Zoom Selection");
    		
            putValue( MNEMONIC_KEY, new Integer( 'o'));
            putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/ZoomSelection16.gif"));
            putValue( SHORT_DESCRIPTION, "Zoom Selection");
    	}

        public void actionPerformed( ActionEvent e) {
    		setDefaultInteractor( zoomInteractor);
	        super.actionPerformed( e);
        }
    }

    public class SelectAction extends DefaultMouseAction {
       public SelectAction() {
            super( "Select");
    		
            putValue( MNEMONIC_KEY, new Integer( 'e'));
            putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Select16.gif"));
            putValue( SHORT_DESCRIPTION, "Select");
    	}

        public void actionPerformed( ActionEvent e) {
    		setDefaultInteractor( null);
	        super.actionPerformed( e);
        }
    }

    public class RotateAction extends DefaultMouseAction {
       public RotateAction() {
            super( "Rotate");
    		
            putValue( MNEMONIC_KEY, new Integer( 'R'));
            putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Rotate16.gif"));
            putValue( SHORT_DESCRIPTION, "Rotate");
    	}

        public void actionPerformed( ActionEvent e) {
    		setDefaultInteractor( rotateInteractor);
	        super.actionPerformed( e);
        }
    }

    public class PanAction extends DefaultMouseAction {
       public PanAction() {
            super( "Pan");
    		
            putValue( MNEMONIC_KEY, new Integer( 'P'));
            putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Pan16.gif"));
            putValue( SHORT_DESCRIPTION, "Pan");
    	}

        public void actionPerformed( ActionEvent e) {
    		setDefaultInteractor( panInteractor);
	        super.actionPerformed( e);
        }
    }

    /**
     * To reload the current document.
     */
    public class ReloadAction extends DefaultMouseAction {
        public ReloadAction() {
	        super( "Reload");
	   
	        putValue( MNEMONIC_KEY, new Integer( 'l'));
	        putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Refresh16.gif"));
	        putValue( SHORT_DESCRIPTION, "Reload Image");
		
		}
        public void actionPerformed(ActionEvent e) {
            if ( (e.getModifiers() & ActionEvent.SHIFT_MASK) == 1) {
                svgCanvas.flushImageCache();
            }

            if (svgDocument != null) {
                showSVGDocument( (String)svgCanvas.getURI());
            }
        }
    }

    /**
     * To go back to the previous document
     */
    public class BackAction extends AbstractAction {
        List components = new LinkedList();

        public BackAction() {
			super( "Back");
		
			putValue( MNEMONIC_KEY, new Integer( 'B'));
			putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Back16.gif"));
			putValue( SHORT_DESCRIPTION, "Back");
		}

        public void actionPerformed(ActionEvent e) {
//            if ( history.canGoBack()) {
//                history.back();
//            }
        }

        protected void update() {
//            setEnabled( history.canGoBack());
        }
    }

    /**
     * To go forward to the previous document
     */
    public class ForwardAction extends AbstractAction {
        List components = new LinkedList();
        public ForwardAction() {
			super( "Forward");
		
			putValue( MNEMONIC_KEY, new Integer( 'F'));
			putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Forward16.gif"));
			putValue( SHORT_DESCRIPTION, "Forward");
		}

        public void actionPerformed(ActionEvent e) {
//            if ( history.canGoForward()) {
//                history.forward();
//            }
        }

        protected void update() {
//            setEnabled( history.canGoForward());
        }
    }

    /**
     * To print the current document.
     */
    public class PrintAction extends AbstractAction {
        public PrintAction() {
			super( "Print");
		
			putValue( MNEMONIC_KEY, new Integer( 'i'));
			putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Print16.gif"));
			putValue( SHORT_DESCRIPTION, "Print Image");
		}
        public void actionPerformed(ActionEvent e) {
            if (svgDocument != null) {
                final SVGDocument doc = svgDocument;

                new Thread() {
                    public void run(){
                        String uri = doc.getURL();
                        String fragment = svgCanvas.getFragmentIdentifier();
                        if ( fragment != null) {
                            uri += "#"+fragment;
                        }

                        //
                        // Build a PrintTranscoder to handle printing
                        // of the svgDocument object
                        //
                        PrintTranscoder pt = new PrintTranscoder();

                        //
                        // Set transcoding hints
                        //
                        pt.addTranscodingHint( XMLAbstractTranscoder.KEY_XML_PARSER_CLASSNAME, XMLResourceDescriptor.getXMLParserClassName());
                        pt.addTranscodingHint( PrintTranscoder.KEY_SHOW_PAGE_DIALOG, Boolean.TRUE);
                        pt.addTranscodingHint( PrintTranscoder.KEY_SHOW_PRINTER_DIALOG, Boolean.TRUE);

                        //
                        // Do transcoding now
                        //
                        pt.transcode(new TranscoderInput(uri), null);

                        //
                        // Print
                        //
                        try {
                            pt.print();
                        } catch (PrinterException ex) {
							ex.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * To save the current document as JPG.
     */
    public class ExportAsJPGAction extends AbstractAction {
        public ExportAsJPGAction() {}
        public void actionPerformed(ActionEvent e) {
//            JFileChooser fileChooser =
//                new JFileChooser(makeAbsolute(currentExportPath));
//            fileChooser.setDialogTitle(resources.getString("ExportAsJPG.title"));
//            fileChooser.setFileHidingEnabled(false);
//            fileChooser.setFileSelectionMode
//                (JFileChooser.FILES_ONLY);
//            fileChooser.addChoosableFileFilter(new ImageFileFilter(".jpg"));
//
//            int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
//            if (choice == JFileChooser.APPROVE_OPTION) {
//                float quality =
//                    JPEGOptionPanel.showDialog(JSVGViewerFrame.this);
//
//                final File f = fileChooser.getSelectedFile();
//                BufferedImage buffer = svgCanvas.getOffScreen();
//                if (buffer != null) {
//                    statusBar.setMessage
//                        (resources.getString("Message.exportAsJPG"));
//
//                    // create a BufferedImage of the appropriate type
//                    int w = buffer.getWidth();
//                    int h = buffer.getHeight();
//                    final ImageTranscoder trans = new JPEGTranscoder();
//                    trans.addTranscodingHint
//                        (JPEGTranscoder.KEY_XML_PARSER_CLASSNAME,
//                         application.getXMLParserClassName());
//                    trans.addTranscodingHint
//                        (JPEGTranscoder.KEY_QUALITY, new Float(quality));
//
//                    final BufferedImage img = trans.createImage(w, h);
//
//                    // paint the buffer to the image
//                    Graphics2D g2d = img.createGraphics();
//                    g2d.setColor(Color.white);
//                    g2d.fillRect(0, 0, w, h);
//                    g2d.drawImage(buffer, null, 0, 0);
//                    new Thread() {
//                        public void run() {
//                            try {
//                                currentExportPath = f;
//                                OutputStream ostream =
//                                    new BufferedOutputStream(new FileOutputStream(f));
//                                trans.writeImage(img, new TranscoderOutput(ostream));
//                                ostream.flush();
//                                ostream.close();
//                            } catch (Exception ex) { }
//                            statusBar.setMessage
//                                (resources.getString("Message.done"));
//                        }
//                    }.start();
//                }
//            }
        }
    }

    /**
     * To save the current document as PNG.
     */
    public class ExportAsPNGAction extends AbstractAction {
        public ExportAsPNGAction() {}
        public void actionPerformed(ActionEvent e) {
//            JFileChooser fileChooser =
//                new JFileChooser(makeAbsolute(currentExportPath));
//            fileChooser.setDialogTitle(resources.getString("ExportAsPNG.title"));
//            fileChooser.setFileHidingEnabled(false);
//            fileChooser.setFileSelectionMode
//                (JFileChooser.FILES_ONLY);
//            fileChooser.addChoosableFileFilter(new ImageFileFilter(".png"));
//
//            int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
//            if (choice == JFileChooser.APPROVE_OPTION) {
//
//		// Start: By Jun Inamori (jun@oop-reserch.com)
//                boolean isIndexed =
//                    PNGOptionPanel.showDialog(JSVGViewerFrame.this);
//		// End: By Jun Inamori (jun@oop-reserch.com)
//
//                final File f = fileChooser.getSelectedFile();
//                BufferedImage buffer = svgCanvas.getOffScreen();
//                if (buffer != null) {
//                    statusBar.setMessage
//                        (resources.getString("Message.exportAsPNG"));
//
//                    // create a BufferedImage of the appropriate type
//                    int w = buffer.getWidth();
//                    int h = buffer.getHeight();
//                    final ImageTranscoder trans = new PNGTranscoder();
//                    trans.addTranscodingHint(PNGTranscoder.KEY_XML_PARSER_CLASSNAME,
//                                             application.getXMLParserClassName());
//                    trans.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE,
//                                             new Boolean(true));
//
//		    // Start: By Jun Inamori
//		    if(isIndexed){
//			trans.addTranscodingHint
//                            (PNGTranscoder.KEY_INDEXED,new Integer(256));
//		    }
//		    // End: By Jun Inamori
//
//                    final BufferedImage img = trans.createImage(w, h);
//
//                    // paint the buffer to the image
//                    Graphics2D g2d = img.createGraphics();
//                    g2d.drawImage(buffer, null, 0, 0);
//                    new Thread() {
//                        public void run() {
//                            try {
//                                currentExportPath = f;
//                                OutputStream ostream =
//                                    new BufferedOutputStream(new FileOutputStream(f));
//                                trans.writeImage(img,
//                                                 new TranscoderOutput(ostream));
//                                ostream.flush();
//                            } catch (Exception ex) {}
//                            statusBar.setMessage
//                                (resources.getString("Message.done"));
//                        }
//                    }.start();
//                }
//            }
        }
    }

    /**
     * To save the current document as TIFF.
     */
    public class ExportAsTIFFAction extends AbstractAction {
        public ExportAsTIFFAction() {}
        public void actionPerformed(ActionEvent e) {
//            JFileChooser fileChooser =
//                new JFileChooser(makeAbsolute(currentExportPath));
//            fileChooser.setDialogTitle(resources.getString("ExportAsTIFF.title"));
//            fileChooser.setFileHidingEnabled(false);
//            fileChooser.setFileSelectionMode
//                (JFileChooser.FILES_ONLY);
//            fileChooser.addChoosableFileFilter(new ImageFileFilter(".tiff"));
//
//            int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
//            if (choice == JFileChooser.APPROVE_OPTION) {
//                final File f = fileChooser.getSelectedFile();
//                BufferedImage buffer = svgCanvas.getOffScreen();
//                if (buffer != null) {
//                    statusBar.setMessage
//                        (resources.getString("Message.exportAsTIFF"));
//
//                    // create a BufferedImage of the appropriate type
//                    int w = buffer.getWidth();
//                    int h = buffer.getHeight();
//                    final ImageTranscoder trans = new TIFFTranscoder();
//                    trans.addTranscodingHint
//                        (TIFFTranscoder.KEY_XML_PARSER_CLASSNAME,
//                         application.getXMLParserClassName());
//                    final BufferedImage img = trans.createImage(w, h);
//
//                    // paint the buffer to the image
//                    Graphics2D g2d = img.createGraphics();
//                    g2d.drawImage(buffer, null, 0, 0);
//                    new Thread() {
//                        public void run() {
//                            try {
//                                currentExportPath = f;
//                                OutputStream ostream = new BufferedOutputStream
//                                    (new FileOutputStream(f));
//                                trans.writeImage
//                                    (img, new TranscoderOutput(ostream));
//                                ostream.flush();
//                            } catch (Exception ex) {}
//                            statusBar.setMessage
//                                (resources.getString("Message.done"));
//                        }
//                    }.start();
//                }
//            }
        }
    }

    /**
     * To view the source of the current document.
     */
//    public class ViewSourceAction extends AbstractAction {
//        public ViewSourceAction() {}
//        public void actionPerformed(ActionEvent e) {
//            if (svgDocument == null) {
//                return;
//            }
//
//            final ParsedURL u = new ParsedURL(svgDocument.getURL());
//            
//            final JFrame fr = new JFrame(u.toString());
//            fr.setSize(resources.getInteger("ViewSource.width"),
//                       resources.getInteger("ViewSource.height"));
//            final JTextArea ta  = new JTextArea();
//            ta.setLineWrap(true);
//            ta.setFont(new Font("monospaced", Font.PLAIN, 12));
//
//            JScrollPane scroll = new JScrollPane();
//            scroll.getViewport().add(ta);
//            scroll.setVerticalScrollBarPolicy
//                (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//            fr.getContentPane().add(scroll, BorderLayout.CENTER);
//
//            new Thread() {
//                public void run() {
//                    char [] buffer = new char[4096];
//
//                    try {
//                        Document  doc = new PlainDocument();
//
//                        ParsedURL purl = new ParsedURL(svgDocument.getURL());
//                        InputStream is
//                            = u.openStream(getInputHandler(purl).
//                                           getHandledMimeTypes());
//                        // u.openStream(MimeTypeConstants.MIME_TYPES_SVG);
//
//                        Reader in = XMLUtilities.createExchangerDocumentReader(is);
//                        int len;
//                        while ((len=in.read(buffer, 0, buffer.length)) != -1) {
//                            doc.insertString(doc.getLength(),
//                                             new String(buffer, 0, len), null);
//                        }
//
//                        ta.setDocument(doc);
//                        ta.setEditable(false);
//                        ta.setBackground(Color.white);
//                        fr.show();
//                    } catch (Exception ex) {
//                        userAgent.displayError(ex);
//                    }
//                }
//            }.start();
//        }
//    }

    /**
     * To flush image cache (purely for debugging purposes)
     */
    public class FlushAction extends AbstractAction {
        public FlushAction() {}
        public void actionPerformed(ActionEvent e) {
            svgCanvas.flush();
            // Force redraw...
            svgCanvas.setRenderingTransform(svgCanvas.getRenderingTransform());
        }
    }

    /**
     * To go back to the previous transform
     */
    public class PreviousTransformAction extends AbstractAction implements JComponentModifier {
        List components = new LinkedList();
        public PreviousTransformAction() {}
        public void actionPerformed(ActionEvent e) {
//            if (transformHistory.canGoBack()) {
//                transformHistory.back();
//                update();
//                nextTransformAction.update();
//                svgCanvas.setRenderingTransform(transformHistory.currentTransform());
//            }
        }

        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }

        protected void update() {
            boolean b = transformHistory.canGoBack();
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(b);
            }
        }
    }

    /**
     * To go forward to the next transform
     */
    public class NextTransformAction extends    AbstractAction
                                         implements JComponentModifier {
        List components = new LinkedList();
        public NextTransformAction() {}
        public void actionPerformed(ActionEvent e) {
            if (transformHistory.canGoForward()) {
                transformHistory.forward();
                update();
                previousTransformAction.update();
                svgCanvas.setRenderingTransform(transformHistory.currentTransform());
            }
        }

        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }

        protected void update() {
            boolean b = transformHistory.canGoForward();
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(b);
            }
        }
    }

    /**
     * To apply the selected author stylesheet
     */
    public class UseStylesheetAction extends AbstractAction implements JComponentModifier {

        List components = new LinkedList();

        public UseStylesheetAction() {}

        public void actionPerformed(ActionEvent e) {
        }

        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }

        protected void update() {
            alternateStyleSheet = null;
            SVGDocument doc = svgCanvas.getSVGDocument();
			if ( stylesheetMenu != null) {
				stylesheetMenu.removeAll();
				stylesheetMenu.setEnabled(false);
			}

            ButtonGroup buttonGroup = new ButtonGroup();

            for ( Node n = doc.getFirstChild(); n != null && n.getNodeType() != Node.ELEMENT_NODE; n = n.getNextSibling()) {
                if ( n instanceof StyleSheetProcessingInstruction) {
                    StyleSheetProcessingInstruction sspi = (StyleSheetProcessingInstruction)n;

                    HashMap<String,String> attrs = sspi.getPseudoAttributes();
                    final String title = (String)attrs.get("title");
                    String alt = (String)attrs.get("alternate");

                    if ( title != null && "yes".equals(alt)) {
                        JRadioButtonMenuItem button = new JRadioButtonMenuItem( title);

                        button.addActionListener
                            (new java.awt.event.ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    SVGOMDocument doc = (SVGOMDocument)svgCanvas.getSVGDocument();
                                    doc.clearViewCSS();

                                    alternateStyleSheet = title;
                                    svgCanvas.setSVGDocument( doc);
                                }
                            });

                        buttonGroup.add( button);
                        stylesheetMenu.add( button);
                        stylesheetMenu.setEnabled( true);
                    }
                }
            }
        }
    }

    /**
     * To restart after a pause.
     */
    public class PlayAction extends AbstractAction {

        public PlayAction() {
			super( "Play");
		
			putValue( MNEMONIC_KEY, new Integer( 'a'));
			putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Play16.gif"));
			putValue( SHORT_DESCRIPTION, "Play");
			
			setEnabled( false);
		}
		
        public void actionPerformed(ActionEvent e) {
            svgCanvas.resumeProcessing();
        }
    }

    /**
     * To pause a document.
     */
    public class PauseAction extends AbstractAction {

        public PauseAction() {
			super( "Pause");
		
			putValue( MNEMONIC_KEY, new Integer( 'u'));
			putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Pause16.gif"));
			putValue( SHORT_DESCRIPTION, "Pause");

			setEnabled( false);
		}

        public void actionPerformed(ActionEvent e) {
            svgCanvas.suspendProcessing();
        }
    }

    /**
     * To stop the current processing.
     */
    public class StopAction extends AbstractAction {

        public StopAction() {
			super( "Stop");
		
			putValue( MNEMONIC_KEY, new Integer( 'S'));
			putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/Stop16.gif"));
			putValue( SHORT_DESCRIPTION, "Stop");

			setEnabled( false);
		}

        public void actionPerformed(ActionEvent e) {
            svgCanvas.stopProcessing();
        }
    }

    /**
     * To show the set transform dialog
     */
    public class SetTransformAction extends AbstractAction {
        public SetTransformAction(){}
        public void actionPerformed(ActionEvent e){
//			System.out.println( "Set Transform");
            if (transformDialog == null){
                transformDialog = JAffineTransformChooser.createDialog( parent, resources.getString("SetTransform.title"));
            }

            AffineTransform txf = transformDialog.showDialog();
            if(txf != null){
                AffineTransform at = svgCanvas.getRenderingTransform();
                if(at == null){
                    at = new AffineTransform();
                }

                txf.concatenate(at);
                svgCanvas.setRenderingTransform(txf);
            }
        }
    }

    /**
     * To display the Find dialog
     */
    public class FindDialogAction extends AbstractAction {
        public FindDialogAction() {}
        public void actionPerformed(ActionEvent e) {
//            if (findDialog == null) {
//                findDialog = new FindDialog(JSVGViewerFrame.this, svgCanvas);
//                findDialog.setGraphicsNode(svgCanvas.getGraphicsNode());
//                findDialog.pack();
//                Rectangle fr = getBounds();
//                Dimension td = findDialog.getSize();
//                findDialog.setLocation(fr.x + (fr.width  - td.width) / 2,
//                                       fr.y + (fr.height - td.height) / 2);
//            }
//            findDialog.show();
        }
    }

    /**
     * To display the Thumbnail dialog
     */
    public class ThumbnailDialogAction extends AbstractAction {
        public ThumbnailDialogAction() {}
        public void actionPerformed(ActionEvent e) {
//            if (thumbnailDialog == null) {
//                thumbnailDialog
//                    = new ThumbnailDialog(JSVGViewerFrame.this, svgCanvas);
//                thumbnailDialog.pack();
//                Rectangle fr = getBounds();
//                Dimension td = thumbnailDialog.getSize();
//                thumbnailDialog.setLocation(fr.x + (fr.width  - td.width) / 2,
//                                            fr.y + (fr.height - td.height) / 2);
//            }
//            thumbnailDialog.show();
        }
    }

    /**
     * To display the document full screen
     */
    public class FullScreenAction extends AbstractAction {
    	private Point location = null;
    	private Dimension size = null;
    	
        public FullScreenAction() {
			super( "Full Screen");
			putValue( MNEMONIC_KEY, new Integer( 'F'));
			putValue( SMALL_ICON, XngrImageLoader.get().getImage( "com/cladonia/xml/svg/icons/FullScreen16.gif"));
			putValue( SHORT_DESCRIPTION, "Full Screen");
		}

        public void actionPerformed(ActionEvent e) {
        	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        	Dimension current = SVGViewerDialog.this.getSize();
        	
        	if ( !current.equals( screen)) {
        		size = current;
        		location = SVGViewerDialog.this.getLocation();

        		SVGViewerDialog.this.setLocation( new Point(0,0));
        		SVGViewerDialog.this.setSize( screen);
        		SVGViewerDialog.this.doLayout();
        	} else if ( size != null) {
        		SVGViewerDialog.this.setLocation( location);
        		SVGViewerDialog.this.setSize( size);
        		SVGViewerDialog.this.doLayout();
        	}
        }
    }
    
    /**
     * What happens when the escape button is pressed
     */
    public class EscapeAction extends AbstractAction {
        public EscapeAction() {}

        public void actionPerformed( ActionEvent e) {
//			System.out.println( "Escaped");
            if ( window != null && window.isVisible()) {
				svgCanvas.getParent().remove( svgCanvas);
				svgCanvasPanel.add( svgCanvas, BorderLayout.CENTER);
				//window.setVisible( false);
				window.hide();
            } else {
	            svgCanvas.stopProcessing();
	          hide();
//				setVisible(false);
            } 
        }
    }

    // ActionMap /////////////////////////////////////////////////////

    /**
     * The map that contains the action listeners
     */
    protected Map listeners = new HashMap();

    /**
     * Returns the action associated with the given string
     * or null on error
     * @param key the key mapped with the action to get
     * @throws MissingListenerException if the action is not found
     */
    public Action getAction(String key) throws MissingListenerException {
        Action result = (Action)listeners.get(key);
        //if (result == null) {
        //result = canvas.getAction(key);
        //}
        if (result == null) {
            throw new MissingListenerException("Can't find action.", RESOURCES, key);
        }
        return result;
    }

    // SVGDocumentLoaderListener ///////////////////////////////////////////

    long time; // For debug.

    /**
     * Called when the loading of a document was started.
     */
    public void documentLoadingStarted( SVGDocumentLoaderEvent e) {
        if (debug) {
            System.out.println("Document load started...");
            time = System.currentTimeMillis();
        }
        statusBar.setMainMessage(resources.getString("Message.documentLoad"));
        stopAction.setEnabled( true);
        svgCanvas.setCursor(WAIT_CURSOR);
    }


    /**
     * Called when the loading of a document was completed.
     */
    public void documentLoadingCompleted( SVGDocumentLoaderEvent e) {
        if (debug) {
            System.out.print("Document load completed in ");
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }

        setSVGDocument( e.getSVGDocument(), e.getSVGDocument().getURL(), e.getSVGDocument().getTitle());
    }

    /**
     * Forces the viewer frame to show the input SVGDocument
     */
    public void setSVGDocument( SVGDocument svgDocument, String svgDocumentURL, String svgDocumentTitle) {
        this.svgDocument = svgDocument;

        if (domViewer != null) {
            if(domViewer.isVisible() && svgDocument != null) {
                domViewer.setDocument( svgDocument, (ViewCSS)svgDocument.getDocumentElement());
            } else {
                domViewer.dispose();
                domViewer = null;
            }
        }

        stopAction.setEnabled(false);
        svgCanvas.setCursor( DEFAULT_CURSOR);
        String s = svgDocumentURL;
        String t = svgCanvas.getFragmentIdentifier();
		
        if ( t != null) {
            s += "#" + t;
        }

//        locationBar.setText(s);
        if ( title == null) {
            title = getTitle();
        }

        String dt = svgDocumentTitle;
        if ( dt.length() != 0) {
            setTitle( title + ":" + dt);
        } else {
            int i = s.lastIndexOf("/");
            if (i == -1) {
                i = s.lastIndexOf("\\");
                if (i == -1) {
                    setTitle(title + ":" + s);
                } else {
                    setTitle(title + ":" + s.substring(i + 1));
                }
            } else {
                setTitle(title + ":" + s.substring(i + 1));
            }
        }

//		if ( svgDocumentURL.indexOf( "#") != -1) {
//			history.update( svgDocumentURL);
//		}
//        history.update( svgDocument);
//        backAction.update();
//      forwardAction.update();

        transformHistory = new TransformHistory();
        previousTransformAction.update();
        nextTransformAction.update();

        useStylesheetAction.update();
    }

    /**
     * Called when the loading of a document was cancelled.
     */
    public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
        if (debug) {
            System.out.println("Document load cancelled");
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.documentCancelled"));
        stopAction.setEnabled(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }

    /**
     * Called when the loading of a document has failed.
     */
    public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
        if (debug) {
            System.out.println("Document load failed");
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.documentFailed"));
        stopAction.setEnabled(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }

    // GVTTreeBuilderListener //////////////////////////////////////////////

    /**
     * Called when a build started.
     * The data of the event is initialized to the old document.
     */
    public void gvtBuildStarted(GVTTreeBuilderEvent e) {
        if (debug) {
            System.out.println("GVT build started...");
            time = System.currentTimeMillis();
        }
        statusBar.setMainMessage(resources.getString("Message.treeBuild"));
        stopAction.setEnabled(true);
        svgCanvas.setCursor(WAIT_CURSOR);
    }

    /**
     * Called when a build was completed.
     */
    public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
        if (debug) {
            System.out.print("GVT build completed in ");
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }
        if (findDialog != null) {
            if(findDialog.isVisible()) {
                findDialog.setGraphicsNode(svgCanvas.getGraphicsNode());
            } else {
                findDialog.dispose();
                findDialog = null;
            }
        }
        stopAction.setEnabled(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
//        svgCanvas.setSelectionOverlayXORMode( application.isSelectionOverlayXORMode());
        svgCanvas.setSelectionOverlayXORMode( true);
        svgCanvas.requestFocusInWindow();  // request focus when load completes.
        if (autoAdjust) {
            pack();
        }
    }

    /**
     * Called when a build was cancelled.
     */
    public void gvtBuildCancelled(GVTTreeBuilderEvent e) {
        if (debug) {
            System.out.println("GVT build cancelled");
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.treeCancelled"));
        stopAction.setEnabled(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
//        svgCanvas.setSelectionOverlayXORMode( application.isSelectionOverlayXORMode());
	    svgCanvas.setSelectionOverlayXORMode( true);
    }

    /**
     * Called when a build failed.
     */
    public void gvtBuildFailed(GVTTreeBuilderEvent e) {
        if (debug) {
            System.out.println("GVT build failed");
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.treeFailed"));
        stopAction.setEnabled(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
//        svgCanvas.setSelectionOverlayXORMode(application.isSelectionOverlayXORMode());
        svgCanvas.setSelectionOverlayXORMode( true);
        if (autoAdjust) {
            pack();
        }
    }

    // SVGLoadEventDispatcherListener //////////////////////////////////////

    /**
     * Called when a onload event dispatch started.
     */
    public void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e) {
        if (debug) {
            System.out.println("Onload dispatch started...");
            time = System.currentTimeMillis();
        }
        stopAction.setEnabled(true);
        statusBar.setMainMessage(resources.getString("Message.onload"));
    }

    /**
     * Called when a onload event dispatch was completed.
     */
    public void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e) {
        if (debug) {
            System.out.print("Onload dispatch completed in ");
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }
        stopAction.setEnabled(false);
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.done"));
    }

    /**
     * Called when a onload event dispatch was cancelled.
     */
    public void svgLoadEventDispatchCancelled(SVGLoadEventDispatcherEvent e) {
        if (debug) {
            System.out.println("Onload dispatch cancelled.");
        }
        stopAction.setEnabled(false);
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.onloadCancelled"));
    }

    /**
     * Called when a onload event dispatch failed.
     */
    public void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e) {
        if (debug) {
            System.out.println("Onload dispatch failed.");
        }
        stopAction.setEnabled(false);
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.onloadFailed"));
    }

    // GVTTreeRendererListener /////////////////////////////////////////////

    /**
     * Called when a rendering is in its preparing phase.
     */
    public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
        if (debug) {
            System.out.println("GVT rendering preparation...");
            time = System.currentTimeMillis();
        }
        stopAction.setEnabled(true);
        svgCanvas.setCursor(WAIT_CURSOR);
        statusBar.setMainMessage(resources.getString("Message.treeRendering"));
    }

    /**
     * Called when a rendering started.
     */
    public void gvtRenderingStarted(GVTTreeRendererEvent e) {
        if (debug) {
            System.out.print("GVT rendering prepared in ");
            System.out.println((System.currentTimeMillis() - time) + " ms");
            time = System.currentTimeMillis();
            System.out.println("GVT rendering started...");
        }
        // Do nothing
    }

    /**
     * Called when a rendering was completed.
     */
    public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
        if (debug) {
            System.out.print("GVT rendering completed in ");
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.done"));
        if (!svgCanvas.isDynamic() || managerStopped) {
            stopAction.setEnabled(false);
        }
        svgCanvas.setCursor(DEFAULT_CURSOR);

        transformHistory.update(svgCanvas.getRenderingTransform());
        previousTransformAction.update();
        nextTransformAction.update();
    }

    /**
     * Called when a rendering was cancelled.
     */
    public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
        if (debug) {
            System.out.println("GVT rendering cancelled");
        }
        statusBar.setMainMessage("");
        if (!svgCanvas.isDynamic()) {
            stopAction.setEnabled(false);
        }
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }

    /**
     * Called when a rendering failed.
     */
    public void gvtRenderingFailed(GVTTreeRendererEvent e) {
        if (debug) {
            System.out.println("GVT rendering failed");
        }
        statusBar.setMainMessage("");
        if (!svgCanvas.isDynamic()) {
            stopAction.setEnabled(false);
        }
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }

    // LinkActivationListener /////////////////////////////////////////

    /**
     * Called when a link was activated.
     */
    public void linkActivated(LinkActivationEvent e) {
        String s = e.getReferencedURI();
        if (svgDocument != null) {
            ParsedURL docURL = new ParsedURL(svgDocument.getURL());
            ParsedURL url    = new ParsedURL(docURL, s);
            if (!url.sameFile(docURL)) {
                return;
            }

            if (s.indexOf("#") != -1) {
//                history.update( s);
//                application.addVisitedURI(s);
//                backAction.update();
//                forwardAction.update();

                transformHistory = new TransformHistory();
                previousTransformAction.update();
                nextTransformAction.update();
            }
        }
    }

    // UpdateManagerListener ////////////////////////////////////////////////

    /**
     * Called when the manager was started.
     */
    public void managerStarted(UpdateManagerEvent e) {
        if (debug) {
            System.out.println( "Update manager started...");
        }
        managerStopped = false;
        playAction.setEnabled(false);
        pauseAction.setEnabled(true);
        stopAction.setEnabled(true);
    }

    /**
     * Called when the manager was suspended.
     */
    public void managerSuspended(UpdateManagerEvent e) {
        if (debug) {
            System.out.println("Update manager suspended");
        }
        playAction.setEnabled(true);
        pauseAction.setEnabled(false);
    }

    /**
     * Called when the manager was resumed.
     */
    public void managerResumed(UpdateManagerEvent e) {
        if (debug) {
            System.out.println("Update manager resumed");
        }
        playAction.setEnabled(false);
        pauseAction.setEnabled(true);
    }

    /**
     * Called when the manager was stopped.
     */
    public void managerStopped(UpdateManagerEvent e) {
        if (debug) {
            System.out.println("Update Manager Stopped");
        }
        managerStopped = true;
        playAction.setEnabled(false);
        pauseAction.setEnabled(false);
        stopAction.setEnabled(false);
    }

    /**
     * Called when an update started.
     */
    public void updateStarted(final UpdateManagerEvent e) {
	    if (debug) {
//    	    System.out.println("Update started");
	    }
    }

    /**
     * Called when an update was completed.
     */
    public void updateCompleted(final UpdateManagerEvent e) {
	    if (debug) {
//	        System.out.println("Update completed");
	    }
    }

    /**
     * Called when an update failed.
     */
    public void updateFailed(UpdateManagerEvent e) {
	    if (debug) {
//	        System.out.println("Update failed");
	    }
    }

    /**
     * This class implements a SVG user agent.
     */
    protected class UserAgent implements SVGUserAgent {

        /**
         * Creates a new SVGUserAgent.
         */
        protected UserAgent() {
        }

        /**
         * Displays an error message.
         */
        public void displayError(String message) {
            if (debug) {
                System.out.println( "displayError( "+message+")");
            }

            MessageHandler.showError( message, "SVG Error");
        }

        /**
         * Displays an error resulting from the specified Exception.
         */
        public void displayError( Exception ex) {
            if (debug) {
                System.out.println( "displayError( "+ex+")");
				ex.printStackTrace();
            }
			
			if ( !(ex instanceof NoninvertibleTransformException)) {
				MessageHandler.showError( ex, "SVG Error");
			}
        }

        /**
         * Displays a message in the User Agent interface.
         * The given message is typically displayed in a status bar.
         */
        public void displayMessage(String message) {
            statusBar.setMessage(message);
        }

        /**
         * Shows an alert dialog box.
         */
        public void showAlert(String message) {
            svgCanvas.showAlert(message);
        }

        /**
         * Shows a prompt dialog box.
         */
        public String showPrompt(String message) {
            return svgCanvas.showPrompt(message);
        }

        /**
         * Shows a prompt dialog box.
         */
        public String showPrompt(String message, String defaultValue) {
            return svgCanvas.showPrompt(message, defaultValue);
        }

        /**
         * Shows a confirm dialog box.
         */
        public boolean showConfirm(String message) {
            return svgCanvas.showConfirm(message);
        }

        /**
         * Returns the size of a px CSS unit in millimeters.
         */
        public float getPixelUnitToMillimeter() {
            return 0.26458333333333333333333333333333f; // 96dpi
        }
        
        /**
         * Returns the size of a px CSS unit in millimeters.
         * This will be removed after next release.
         * @see #getPixelUnitToMillimeter()
         */
        public float getPixelToMM() {
            return getPixelUnitToMillimeter();
            
        }

        /**
         * Returns the default font family.
         */
        public String getDefaultFontFamily() {
            return "Arial, Helvetica, sans-serif"; 
        }

        /** 
         * Returns the  medium font size. 
         */
        public float getMediumFontSize() {
            // 9pt (72pt == 1in)
            return 9f * 25.4f / (72f * getPixelUnitToMillimeter());
        }

        /**
         * Returns a lighter font-weight.
         */
        public float getLighterFontWeight(float f) {
            // Round f to nearest 100...
            int weight = ((int)((f+50)/100))*100;
            switch (weight) {
            case 100: return 100;
            case 200: return 100;
            case 300: return 200;
            case 400: return 300;
            case 500: return 400;
            case 600: return 400;
            case 700: return 400;
            case 800: return 400;
            case 900: return 400;
            default:
                throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
        }

        /**
         * Returns a bolder font-weight.
         */
        public float getBolderFontWeight(float f) {
            // Round f to nearest 100...
            int weight = ((int)((f+50)/100))*100;
            switch (weight) {
            case 100: return 600;
            case 200: return 600;
            case 300: return 600;
            case 400: return 600;
            case 500: return 600;
            case 600: return 700;
            case 700: return 800;
            case 800: return 900;
            case 900: return 900;
            default:
                throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
        }


        /**
         * Returns the language settings.
         */
        public String getLanguages() {
            return Locale.getDefault().getLanguage();
        }

        /**
         * Returns the user stylesheet uri.
         * @return null if no user style sheet was specified.
         */
        public String getUserStyleSheetURI() {
            return null;
        }

        /**
         * Returns the class name of the XML parser.
         */
        public String getXMLParserClassName() {
            return XMLResourceDescriptor.getXMLParserClassName();
        }

        /**
         * Returns true if the XML parser must be in validation mode, false
         * otherwise.
         */
        public boolean isXMLParserValidating() {
            return false;
        }

        /**
         * Returns this user agent's CSS media.
         */
        public String getMedia() {
            return "screen";
        }

        /**
         * Returns this user agent's alternate style-sheet title.
         */
        public String getAlternateStyleSheet() {
            return alternateStyleSheet;
        }

        /**
         * Opens a link.
         * @param uri The document URI.
         * @param newc Whether the link should be activated in a new component.
         */
        public void openLink(String uri, boolean newc) {
			showSVGDocument(uri);
        }

        /**
         * Tells whether the given extension is supported by this
         * user agent.
         */
        public boolean supportExtension(String s) {
            return false;
        }

        public void handleElement(Element elt, Object data){
        }

        /**
         * Returns the security settings for the given script
         * type, script url and document url
         * 
         * @param scriptType type of script, as found in the 
         *        type attribute of the &lt;script&gt; element.
         * @param scriptURL url for the script, as defined in
         *        the script's xlink:href attribute. If that
         *        attribute was empty, then this parameter should
         *        be null
         * @param docURL url for the document into which the 
         *        script was found.
         */
        public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL){
            return new DefaultScriptSecurity( scriptType, scriptURL, docURL);
        }

        /**
         * This method throws a SecurityException if the script
         * of given type, found at url and referenced from docURL
         * should not be loaded.
         * 
         * This is a convenience method to call checkLoadScript
         * on the ScriptSecurity strategy returned by 
         * getScriptSecurity.
         *
         * @param scriptType type of script, as found in the 
         *        type attribute of the &lt;script&gt; element.
         * @param scriptURL url for the script, as defined in
         *        the script's xlink:href attribute. If that
         *        attribute was empty, then this parameter should
         *        be null
         * @param docURL url for the document into which the 
         *        script was found.
         */
        public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) throws SecurityException {
            ScriptSecurity s = getScriptSecurity(scriptType, scriptURL, docURL);

            if (s != null) {
                s.checkLoadScript();
            } 
        }

        /**
         * Returns the security settings for the given 
         * resource url and document url
         * 
         * @param resourceURL url for the resource, as defined in
         *        the resource's xlink:href attribute. If that
         *        attribute was empty, then this parameter should
         *        be null
         * @param docURL url for the document into which the 
         *        resource was found.
         */
        public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL){
			return new RelaxedExternalResourceSecurity(resourceURL, docURL);
        }

        /**
         * This method throws a SecurityException if the resource
         * found at url and referenced from docURL
         * should not be loaded.
         * 
         * This is a convenience method to call checkLoadExternalResource
         * on the ExternalResourceSecurity strategy returned by 
         * getExternalResourceSecurity.
         *
         * @param scriptURL url for the script, as defined in
         *        the script's xlink:href attribute. If that
         *        attribute was empty, then this parameter should
         *        be null
         * @param docURL url for the document into which the 
         *        script was found.
         */
        public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) throws SecurityException {
            ExternalResourceSecurity s =  getExternalResourceSecurity(resourceURL, docURL);
            
            if (s != null) {
                s.checkLoadExternalResource();
            }
        }
    }

    protected class ToggleButtonToolBar extends JToolBar {
	    public ToggleButtonToolBar() {
	        super();
	    }

	    JToggleButton addToggleButton(Action a) {
	        JToggleButton tb = new JToggleButton( (String)a.getValue(Action.NAME), (Icon)a.getValue( Action.SMALL_ICON));
	        tb.setAction(a);
//	        tb.setMargin( new Insets(1,1,1,1));
	        tb.setText( null);
	        tb.setEnabled( a.isEnabled());
	        tb.setToolTipText((String)a.getValue(Action.SHORT_DESCRIPTION));
	        add(tb);
	        return tb;
	    }
    }

    /**
     * A FileFilter used when exporting the SVG document as an image.
     */
    protected static class ImageFileFilter extends FileFilter {

        /** The extension of the image filename. */
        protected String extension;

        public ImageFileFilter(String extension) {
            this.extension = extension;
        }

        /**
         * Returns true if <tt>f</tt> is a file with the correct extension,
         * false otherwise.
         */
        public boolean accept(File f) {
            boolean accept = false;
            String fileName = null;
            if (f != null) {
                if (f.isDirectory()) {
                    accept = true;
                } else {
                    fileName = f.getPath().toLowerCase();
                    if (fileName.endsWith(extension)) {
                        accept = true;
                    }
                }
            }
            return accept;
        }

        /**
         * Returns the file description
         */
        public String getDescription() {
            return extension;
        }
    }
	
	private class History {
		private final static int STABLE_STATE = 0;
		private final static int BACK_PENDING_STATE = 1;
		private final static int FORWARD_PENDING_STATE = 2;
		private final static int RELOAD_PENDING_STATE = 3;

		private int index = 0;
		private Vector references = null;
	    private int state = STABLE_STATE;

	    /**
	     * Creates a new history.
	     */
	    public History() {
			references = new Vector();
	    }

	    /**
	     * Goes back of one position in the history.
	     * Assumes that <tt>canGoBack()</tt> is true.
	     */
	    public void back() {
//	        System.out.println( "History.back() ["+index+"]");
	        update();

	        state = BACK_PENDING_STATE;
	        index -= 2;

	        showSVGDocument( (String)references.get( index + 1));
	    }

	    /**
	     * Whether it is possible to go back.
	     */
	    public boolean canGoBack() {
//	        System.out.println( "History.canGoBack() ["+index+"]");
	        return index > 0;
	    }

	    /**
	     * Goes forward of one position in the history.
	     * Assumes that <tt>canGoForward()</tt> is true.
	     */
	    public void forward() {
//	        System.out.println( "History.forward() ["+index+"]");
	        update();

	        state = FORWARD_PENDING_STATE;

	        showSVGDocument( (String)references.get( index + 1));
	    }

	    /**
	     * Whether it is possible to go forward.
	     */
	    public boolean canGoForward() {
//	        System.out.println( "History.canGoForward() ["+index+"]");
	        return index < references.size() - 1;
	    }

	    /**
	     * Reloads the current document.
	     */
	    public void reload() {
//	        System.out.println( "History.reload() ["+index+"]");
	        update();
	        
			state = RELOAD_PENDING_STATE;
	        index--;
	        
			showSVGDocument( (String)references.get( index + 1));
	    }

	    /**
	     * Updates the history.
	     * @param uri The URI of the document just loaded.
	     */
	    public void update( String ref) {
//	        System.out.println( "History.update( "+ref+") ["+index+"]");
	        if ( index < -1) {
				System.err.println( "Index is smaller than -1");
	        }

	        state = STABLE_STATE;

	        if ( ++index < references.size()) {
	            if ( !references.get( index).equals( ref)) {
	                references = new Vector( references.subList( 0, index + 1));
	            }

	            references.set( index, ref);
	        } else {
	            if ( references.size() >= 10) {
	                references.remove(0);
	                index--;
	            }
				
	            references.add( ref);
	        }
	    }

	    /**
	     * Updates the state of this history.
	     */
	    public void reset() {
//			System.out.println( "History.reset() ["+index+"]");

			state = STABLE_STATE;
			references.removeAllElements();
			index = 0;
	    }

	    /**
	     * Updates the state of this history.
	     */
	    private void update() {
	        switch (state) {
	        case BACK_PENDING_STATE:
	            index += 2;
	            break;
	        case RELOAD_PENDING_STATE:
	            index++;
	        case FORWARD_PENDING_STATE:
	        case STABLE_STATE:
	        }
	    }
	}	
}