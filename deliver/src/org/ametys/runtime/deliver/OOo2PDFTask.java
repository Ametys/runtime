/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.deliver;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Ant Task used to perform file exportation.
 * Thanks to this task it is possible to convert SXW files to PDF files.
 */
public class OOo2PDFTask extends Task
{
    // UNO Port for OpenOffice
    private static final int UNO_PORT = 8100;
    
    // OpenOffice host
    private static final String HOST = "localhost";

    // Path of the directory which contains doc file to export
    private String _docDir;
    
    /** 
     * Setter for the _docDir attribute
     * @param docDir the root dir for encryption
     */
    public void setDocDir(String docDir)
    {
        _docDir = docDir;
    }
    
    /**
     * Starts to export doc files to PDF files.
     */
    @Override
    public void execute()
    {
        // Va parcourir le répertoire passé en argument et va convertir chaque fichier sxw qui s'y trouve en pdf.
        if (_docDir == null)
        {
            throw new BuildException("input directory must be specified");
        }

        File dir = new File(_docDir);
        if (dir.isDirectory())
        {
            FilenameFilter filter = new FilenameFilter()
            {
                public boolean accept(File directory, String name)
                {
                    return name.endsWith(".sxw");
                }
            };
            
            File[] children = dir.listFiles(filter);
            if (children != null)
            {
                _exportFiles(children);
            }
        }
    }
    
    private void _exportFiles(File[] files)
    {
        try
        {
            // First step: create local component context, get local servicemanager
            // and ask it to create a UnoUrlResolver object with an XUnoUrlResolver
            // interface
            XComponentContext xLocalContext = Bootstrap.createInitialComponentContext(null);
            XMultiComponentFactory xLocalComponentManager = xLocalContext.getServiceManager();

            // On ouvre la connection
            Object connector = xLocalComponentManager.createInstanceWithContext("com.sun.star.connection.Connector", xLocalContext);
            XConnector xConnector = (XConnector) UnoRuntime.queryInterface(XConnector.class, connector);
            XConnection connection = xConnector.connect("socket,host=" + HOST + ",port=" + UNO_PORT);
            
            // puis le bridge
            Object bridgeFactory = xLocalComponentManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", xLocalContext);
            XBridgeFactory xBridgeFactory = (XBridgeFactory) UnoRuntime.queryInterface(XBridgeFactory.class, bridgeFactory);
            XBridge bridge = xBridgeFactory.createBridge("", "urp", connection, null);
            
            Object serviceManager = bridge.getInstance("StarOffice.ServiceManager");
            XMultiComponentFactory xRemoteComponentManager = (XMultiComponentFactory) UnoRuntime.queryInterface(XMultiComponentFactory.class, serviceManager);
            
            XPropertySet xPropertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, serviceManager);
            
            Object context = xPropertySet.getPropertyValue("DefaultContext");
            XComponentContext xRemoteContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, context);
            
            Object desktop = xRemoteComponentManager.createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext);
            XComponentLoader xComponentLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktop);
            
            for (int i = 0; i < files.length; i++)                 
            {
                File file = files[i];
                if (file.exists() && file.length() != 0L)
                {
                    log("Export of " + file.getName());
                    _exportFile(file, xComponentLoader);
                }
            }

            // on dispose le bridge pour ne plus avoir de socket ouverte
            XComponent bridgeComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, bridge);
            bridgeComponent.dispose();
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
    
    /**
     * Calls the openoffice server API to export files
     * @param file the path of the file to process
     * @param xComponentLoader 
     * @throws BuildException if a problem occurs (OOo server not responding, ...)
     */
    private void _exportFile(File file, XComponentLoader xComponentLoader) throws BuildException
    {
        try
        {    
            PropertyValue[] loadProps = new PropertyValue[1];
            loadProps[0] = new PropertyValue();
            loadProps[0].Name = "Hidden";
            loadProps[0].Value = new Boolean(true);
    
            String filepath = file.getAbsolutePath();
            filepath = "file:///" + filepath.replace('\\', '/');
            
            XComponent document = xComponentLoader.loadComponentFromURL(filepath, "_default", 0, loadProps);
            
            XStorable xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, document);
    
            PropertyValue[] storeProps = new PropertyValue[2];
            storeProps[0] = new PropertyValue();
            storeProps[0].Name = "FilterName";
            storeProps[0].Value = "writer_pdf_Export";
            storeProps[1] = new PropertyValue();
            storeProps[1].Name = "CompressionMode";
            storeProps[1].Value = "1";    
    
            filepath = filepath.substring(0, filepath.lastIndexOf(".")) + ".pdf";
    
            xStorable.storeToURL(filepath, storeProps);
    
            // il faut disposer les ressources liées au document
            document.dispose();
    
            // on efface l'ancien fichier pour mettre le nouveau dessus
            file.delete();
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
}
