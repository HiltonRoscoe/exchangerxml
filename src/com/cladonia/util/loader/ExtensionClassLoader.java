/*
 * $Id: ExtensionClassLoader.java,v 1.1.1.1 2004/03/26 12:26:16 edankert Exp $
 *
 * Copyright (C) 2004, Cladonia Ltd. All rights reserved.
 *
 * This software is the proprietary information of Cladonia Ltd.  
 * Use is subject to license terms.
 */
package com.cladonia.util.loader;

import java.io.File;
import java.io.FilenameFilter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.cladonia.xml.XngrURLUtilities;

//import com.sun.org.apache.xpath.internal.operations.Plus;


/**
 * A class loader which can deal with endorsed libraries 
 * and extensions.
 *
 * @version	$Revision: 1.1.1.1 $, $Date: 2004/03/26 12:26:16 $
 * @author Dogsbay
 */
public class ExtensionClassLoader extends URLClassLoader {
    private static final boolean VERBOSE = true;
    
	private EndorsedClassLoader endorsedLoader = null;
	private boolean endorsing = false;

    public ExtensionClassLoader( ClassLoader parent) {
        super(new URL[0], parent);
		
		endorsedLoader = new EndorsedClassLoader( this);
    }
        
    public void addEndorsedLibrary( File file) {
		endorsedLoader.addEndorsed( file);
    }

    public void addLibrary( File library) {
        if (VERBOSE) System.out.println("Processing library: "+library);

        if ( library.exists()) {
	        try  {
	            URL url = XngrURLUtilities.getURLFromFile(library.getAbsoluteFile());

	            if (VERBOSE) System.out.println("Adding library: " + url.getPath());
	            super.addURL( url);
	        } catch (MalformedURLException e) {
	            throw new IllegalArgumentException(e.toString());
	        }
        }
    }

    public void addExtension( File extension) {
        if (VERBOSE) System.out.println("Processing extension: "+extension);

        if ( extension.exists()) {
            try  {
                URL url = XngrURLUtilities.getURLFromFile(extension.getAbsoluteFile());

                if (VERBOSE) System.out.println("Adding extension: " + url.getPath());
                super.addURL( url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.toString());
            }
        }
    }
    
    /*public void addPlugin( File plugin) {
        if (VERBOSE) System.out.println("Processing plugin: "+plugin);

        if ( plugin.exists()) {
            try  {
                URL url = plugin.getAbsoluteFile().toURL();

                if (VERBOSE) System.out.println("Adding plugin: " + url.getPath());
                super.addURL( url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e.toString());
            }
        }
    }*/
    
    public void addPluginsDirectory( File pluginsDir) {
        if (VERBOSE) System.out.println( "Set plugins directory: "+pluginsDir);

        if ( pluginsDir.exists() && pluginsDir.isDirectory()) {
            File[] pluginsDirFiles = pluginsDir.listFiles();

            for ( int i = 0; i < pluginsDirFiles.length; i++) {
            	if(pluginsDirFiles[i].isDirectory() == true) {
            		addPluginsDirectory(pluginsDirFiles[i]);
            	}
            	else if(pluginsDirFiles[i].isFile() == true){
	                if ( pluginsDirFiles[i].getAbsolutePath().toLowerCase().endsWith(".jar")) {
	                    try  {
	                        URL url = XngrURLUtilities.getURLFromFile(pluginsDirFiles[i].getAbsoluteFile());
	                    	if (VERBOSE) System.out.println("Adding plugin: " + url.getPath());
	                    	
	                    	//its a saxon jar
	                    	if(url.getPath().indexOf("saxon") > -1) {
	                    		if(VERBOSE) System.out.println("Loading saxon jar");
	                    		if(VERBOSE) System.out.println("Finding library: "+this.findLibrary(url.getFile()));
	                    	}
	                    	else {
	                    		super.addURL(url);
	                    	}
	                        
	                    } catch (MalformedURLException e) {
	                    	e.printStackTrace();
	                        throw new IllegalArgumentException( e.toString());
	                        
	                    }
	                }
            	}
            }
        }
    }
    
    
    protected Class findClass(String name) throws ClassNotFoundException {
    	
    	// TODO Auto-generated method stub
    	return super.findClass(name);
    }
    
    
    public void addPluginsLibDirectory( File pluginsLibDir) {
        if (VERBOSE) System.out.println( "Set plugins directory: "+pluginsLibDir);

        if ( pluginsLibDir.exists() && pluginsLibDir.isDirectory()) {
            File[] jars = pluginsLibDir.listFiles();

            for ( int i = 0; i < jars.length; i++) {
                if ( jars[i].getAbsolutePath().toLowerCase().endsWith(".jar")) {
                    try  {
                        URL url = XngrURLUtilities.getURLFromFile(jars[i].getAbsoluteFile());
                        if (VERBOSE) System.out.println("Adding plugin lib: " + url.getPath());
                        super.addURL(url);
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException( e.toString());
                    }
                }
            }
        }
    }

    public void addExtensionDirectory( File extensionDir) {
        if (VERBOSE) System.out.println( "Set extension directory: "+extensionDir);

        if ( extensionDir.exists() && extensionDir.isDirectory()) {
            File[] jars = extensionDir.listFiles();

            for ( int i = 0; i < jars.length; i++) {
                if ( jars[i].getAbsolutePath().toLowerCase().endsWith(".jar")) {
                    try  {
                        URL url = XngrURLUtilities.getURLFromFile(jars[i].getAbsoluteFile());
                        if (VERBOSE) System.out.println("Adding extension: " + url.getPath());
                        super.addURL(url);
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException( e.toString());
                    }
                }
            }
        }
    }

    public final Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    	boolean LOCAL_VERBOSE = false;
    	
    	/*if(name.indexOf("saxon") > -1) {
    		LOCAL_VERBOSE = true;
    	}*/
    	
		Class clazz = null;
		
		try {
			if(LOCAL_VERBOSE) System.out.println("loadClass: trying to load: "+name);
	        clazz = endorsedLoader.loadEndorsedClass( name, resolve);
	        if(LOCAL_VERBOSE) System.out.println("loadClass: Success load class from endorsed: "+name);
		} catch ( Exception e) {
			clazz = null;
			if(LOCAL_VERBOSE) System.out.println("loadClass: Failed to load class from endorsed: "+name);
			//e.printStackTrace();
		}

		if ( clazz == null) {
			try {
				clazz = super.loadClass( name, resolve);
				if(LOCAL_VERBOSE) System.out.println("loadClass: Loaded normally: "+name);
			} catch (ClassNotFoundException e) {
				throw(e);
			} catch (Exception e) {
				if(LOCAL_VERBOSE) System.err.println("ExtensionClassLoader::loadClass - trying to load class name: "+name);
				e.printStackTrace();
			}
			
		}
		
		return clazz;
    }
    
    public final URL getResource(final String name) {
	    URL url = null;

	    try {
	        url = endorsedLoader.getEndorsedResource( name);
	    } catch ( Exception e) {
	    	url = null;
	    }

		if ( url == null) {
	    	url = super.getResource( name);
	    }
	    
	    return url;
    }
    
    class JarFileNameFilter implements FilenameFilter {

    	private String JAR_FILE_SUFFIX = "jar";
		
    	public boolean accept(File dir, String name) {
			if(dir.isDirectory() == true) {
				return(true);
			}
			else {
				if((name != null) && (name.toLowerCase().endsWith(JAR_FILE_SUFFIX))) {
					return(true);
				}
				else {
					return false;
				}
			}
		}
    	
    }

    class EndorsedClassLoader extends URLClassLoader {
		ClassLoader parent = null;

        public EndorsedClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
			
			this.parent = parent;
        }
            
        public final Class loadEndorsedClass(String name, boolean resolve) throws ClassNotFoundException {
            // First check if it's already loaded
            Class clazz = findLoadedClass(name);
            
            if (clazz == null) {
                clazz = findClass(name);
            }
            
            if (resolve) {
                resolveClass(clazz);
            }
            
            return clazz;
        }
        
        public final URL getEndorsedResource(final String name) {
            URL resource = findResource(name);
    
            return resource;
        }

        public void addEndorsed( File file) {
        	boolean LOCAL_VERBOSE = false;
        	
            if (LOCAL_VERBOSE) System.out.println("Processing endorsed: " +file);

            if ( file.exists()) {
				/*try {
		            URL url = file.getAbsoluteFile().toURL();
		            if (VERBOSE) System.out.println("Adding Endorsed: " + url.getPath());

		            super.addURL( url);                
		        } catch (MalformedURLException e) {
		            throw new IllegalArgumentException(e.toString());
		        }*/
            	List list = getAllJarFilesRecursive(file);
            	if(list != null) {
            		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						URL url = (URL) iterator.next();
						if(url != null) {
							if (LOCAL_VERBOSE) System.out.println("Adding Endorsed: " + url.getPath());
							super.addURL(url);
						}
					}
            	}
            }
        }
        
        public List getAllJarFilesRecursive(File parentFile) {
        	ArrayList returnList = new ArrayList();
        	
        	if(parentFile.exists() == true) {
        		if(parentFile.isDirectory() == true) {
        			//get all children, if any of them are jar files, add them,
        			//if any are folders, recursively call this method
        			File[] childFiles = parentFile.listFiles(new JarFileNameFilter());
        			if(childFiles != null) {
        				for (int cnt = 0; cnt < childFiles.length; cnt++) {
							File childFile = childFiles[cnt];
							if(childFile != null) {
								if(childFile.isDirectory()) {
									List childList = getAllJarFilesRecursive(childFile);
									returnList.addAll(childList);
								}
								else {
									try {
										returnList.add(childFile.toURI().toURL());
									}catch(MalformedURLException e) {
										e.printStackTrace();
									}
								}
							}
						}
        			}
        		}
        		else {
        			if((parentFile.getName() != null) && (parentFile.getName().toLowerCase().endsWith("jar"))) {
        				
	        			try {
							returnList.add(parentFile.toURI().toURL());
						}catch(MalformedURLException e) {
							e.printStackTrace();
						}
        			}
        		}
        		
        	}
        	return(returnList);
        }
        
        
        
	    public final Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    	    // First check if it's already loaded
        	Class clazz = findLoadedClass(name);
        
	        if (clazz == null) {
            
    	        try {
            	    clazz = findClass(name);
        	        //System.err.println("Paranoid load : " + name);
	            } catch (ClassNotFoundException cnfe) {
    	            if (this.parent != null) {
        	             // Ask to parent ClassLoader (can also throw a CNFE).
            	        clazz = this.parent.loadClass(name);
                	} else {
                    	// Propagate exception
	                    throw cnfe;
    	            }
        	    }
	        }
        
    	    if (resolve) {
        	    resolveClass(clazz);
	        }
        
    	    return clazz;
	    }
    
    	public final URL getResource(final String name) {

	        URL resource = findResource(name);

    	    if (resource == null && this.parent != null) {
        	    resource = this.parent.getResource(name);
	        }

    	    return resource;
	    }
        
    }
}
