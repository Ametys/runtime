/*
 * Copyright (c) 2007 Anyware Technologies and others. All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.opensource.org/licenses/eclipse-1.0.php Contributors: Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;
import org.ametys.runtime.util.LoggerFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.cocoon.Constants;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.servlet.multipart.RequestFactory;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Main entry point for applications.<br>
 * Overrides the CocoonServlet to add some initialization.<br>
 */
public class RuntimeServlet extends CocoonServlet
{
    /** The config file relative path */
    public static final String CONFIG_RELATIVE_PATH = "WEB-INF/config/config.xml";

    /** The run modes */
    public enum RunMode
    {
        /** Normal execution mode */
        NORMAL,
        /** Maintenance mode required by administrator */
        // MAINTENANCE,
    }

    private static RunMode _mode = RunMode.NORMAL;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        req.setCharacterEncoding("UTF-8");
        
        // Error mode
        if (exception != null)
        {
            try
            {
                _runErrorMode(req, res);
            }
            catch (ServletException e)
            {
                // La, on peut vraiment plus rien faire
                throw e;
            }
            catch (Exception e)
            {
                // La, on peut vraiment plus rien faire non plus
                throw new ServletException(e);
            }

            return;
        }

        // if (getRunMode() == RunMode.MAINTENANCE && !_accept(req))
        // {
        // _runMaintenanceMode(req, res);
        // }
        // else
        // {
        _fireRequestStarted(req);

        super.service(req, res);

        _fireRequestEnded(req);

        if (req.getAttribute("org.ametys.runtime.reload") != null)
        {
            ConfigManager.getInstance().dispose();
            disposeCocoon();
            initLogger();
            createCocoon();
        }
        // }
    }

    @Override
    public final void init(ServletConfig conf) throws ServletException
    {
        try
        {
            super.init(conf);
        }
        catch (Throwable t)
        {
            if (getLogger() != null)
            {
                getLogger().error("Error while loading servlet. Entering in error mode.", t);
            }
            else
            {
                System.out.println("Error while loading servlet. Entering in error mode.");
                t.printStackTrace();

                if (t instanceof Exception)
                {
                    this.exception = (Exception) t;
                }
                else
                {
                    this.exception = new Exception(t);
                }
            }
        }
    }
    
    @Override
    protected synchronized void createCocoon() throws ServletException
    {
        super.createCocoon();
        
        if (ConfigManager.getInstance().isComplete())
        {
            // Reprise de la gestion de l'upload pour ne plus reposer sur le web.xml
            // Le code ici reprend en partie celui de la CocoonServlet (tout est privé)
            boolean enableUploads = getInitParameterAsBoolean("enable-uploads", false);
            
            if (enableUploads)
            {
                int maxUploadSize;
                Long maxUploadSizeParam = Config.getInstance().getValueAsLong("runtime.upload.max-size");
                if (maxUploadSizeParam == null)
                {
                    // la feature core/runtime.upload est désactivée, on repasse à la valeur du web.xml et sinon à 10Mo par défaut
                    maxUploadSize = getInitParameterAsInteger("upload-max-size", 10000000);
                }
                else
                {
                    maxUploadSize = (int) maxUploadSizeParam.longValue();
                }
                
                boolean autoSaveUploads = getInitParameterAsBoolean("autosave-uploads", true);
                String overwriteParam = getInitParameter("overwrite-uploads", "rename");
                String containerEncoding = getInitParameter("container-encoding", "ISO-8859-1");

                File uploadDir;
                String uploadDirParam = Config.getInstance().getValueAsString("runtime.upload.dir");
                if (uploadDirParam == null)
                {
                    // la feature core/runtime.upload est désactivée, on repasse à la valeur du web.xml et sinon à WEB-INF/data/uploads par défaut
                    uploadDirParam = getInitParameter("upload-directory", "WEB-INF/data/uploads");
                }
                
                // Context path exists : is upload-directory absolute ?
                File uploadDirParamFile = new File(uploadDirParam);
                if (uploadDirParamFile.isAbsolute())
                {
                    // Yes : keep it as is
                    uploadDir = uploadDirParamFile;
                }
                else
                {
                    // No : consider it relative to context path
                    uploadDir = new File(servletContextPath, uploadDirParam);
                }

                uploadDir.mkdirs();
                appContext.put(Constants.CONTEXT_UPLOAD_DIR, uploadDir);

                boolean allowOverwrite;
                boolean silentlyRename;

                // accepted values are deny|allow|rename - rename is default.
                if ("deny".equalsIgnoreCase(overwriteParam))
                {
                    allowOverwrite = false;
                    silentlyRename = false;
                }
                else if ("allow".equalsIgnoreCase(overwriteParam))
                {
                    allowOverwrite = true;
                    silentlyRename = false; // ignored in this case
                }
                else
                {
                    // either rename is specified or unsupported value - default to rename.
                    allowOverwrite = false;
                    silentlyRename = true;
                }

                requestFactory = new RequestFactory(autoSaveUploads, uploadDir, allowOverwrite, silentlyRename, maxUploadSize, containerEncoding);
            }
        }
    }

    @Override
    protected void updateEnvironment() throws ServletException
    {
        super.updateEnvironment();

        LoggerFactory.setup(getLoggerManager());

        // Chargement du contenu de WEB-INF/param/runtime.xml
        _loadRuntimeConfig();

        // Emplacement de la configuration
        Config.setFilename(servletContextPath + CONFIG_RELATIVE_PATH);
    }

    @SuppressWarnings("unchecked")
    private void _fireRequestStarted(HttpServletRequest req)
    {
        Collection< ? extends RequestListener> listeners = (Collection< ? extends RequestListener>) servletContext.getAttribute(RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS);

        if (listeners == null)
        {
            return;
        }

        for (RequestListener listener : listeners)
        {
            listener.requestStarted(req);
        }
    }

    @SuppressWarnings("unchecked")
    private void _fireRequestEnded(HttpServletRequest req)
    {
        Collection< ? extends RequestListener> listeners = (Collection< ? extends RequestListener>) servletContext.getAttribute(RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS);

        if (listeners == null)
        {
            return;
        }

        for (RequestListener listener : listeners)
        {
            listener.requestEnded(req);
        }
    }

    private void _loadRuntimeConfig() throws ServletException
    {
        InputStream is = null;
        InputStream xsd = null;

        try
        {
            // Validation du runtime.xml sur le schéma runtime.xsd
            xsd = getClass().getResourceAsStream("/org/ametys/runtime/servlet/runtime.xsd");
            Schema schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(new StreamSource(xsd));

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setSchema(schema);
            factory.setNamespaceAware(true);
            XMLReader reader = factory.newSAXParser().getXMLReader();

            DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder(reader);

            File configFile = new File(servletContextPath, "WEB-INF/param/runtime.xml");

            is = new FileInputStream(configFile);

            Configuration conf = confBuilder.build(is, configFile.getAbsolutePath());

            RuntimeConfig.configure(conf);
        }
        catch (Exception ex)
        {
            String errorMessage = "Unable to load config values at WEB-INF/param/runtime.xml";
            getLogger().error(errorMessage, ex);
            throw new ServletException(errorMessage, ex);
        }
        finally
        {
            if (xsd != null)
            {
                try
                {
                    xsd.close();
                }
                catch (IOException ex)
                {
                    String errorMessage = "Unable to close InputStream";
                    getLogger().error(errorMessage, ex);
                    throw new ServletException(errorMessage, ex);
                }
            }

            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException ex)
                {
                    String errorMessage = "Unable to close InputStream";
                    getLogger().error(errorMessage, ex);
                    throw new ServletException(errorMessage, ex);
                }
            }
        }
    }

    /**
     * Set the run mode
     * 
     * @param mode the running mode
     */
    public static void setRunMode(RunMode mode)
    {
        _mode = mode;
    }

    /**
     * Get the run mode
     * 
     * @return the current run mode
     */
    public static RunMode getRunMode()
    {
        return _mode;
    }

    private void _runErrorMode(HttpServletRequest req, HttpServletResponse res) throws Exception
    {
        ServletConfig config = getServletConfig();

        if (config == null)
        {
            throw new ServletException("Cannot access to ServletConfig");
        }

        String contextPath = req.getContextPath();

        ServletOutputStream os = res.getOutputStream();

        // Ressources statiques associées à la page d'erreur
        if (req.getRequestURI().startsWith(contextPath + "/kernel/resources/"))
        {
            InputStream is = getClass().getResourceAsStream("/org/ametys/runtime" + req.getRequestURI().substring(contextPath.length()));

            if (is == null)
            {
                res.setStatus(404);
            }
            else
            {
                res.setStatus(200);
                res.setContentType(config.getServletContext().getMimeType(req.getRequestURI()));

                byte[] buffer = new byte[8192];
                int length = -1;

                while ((length = is.read(buffer)) > -1)
                {
                    os.write(buffer, 0, length);
                }

                is.close();
            }

            return;
        }
        else if (req.getRequestURI().startsWith(contextPath + "/error/resources"))
        {
            File f = new File(config.getServletContext().getRealPath(req.getRequestURI().substring(contextPath.length())));
            if (f.exists())
            {
                res.setStatus(200);
                res.setContentType(config.getServletContext().getMimeType(req.getRequestURI()));

                InputStream is = new FileInputStream(f);

                byte[] buffer = new byte[8192];
                int length = -1;

                while ((length = is.read(buffer)) > -1)
                {
                    os.write(buffer, 0, length);
                }

                is.close();
            }
            else
            {
                res.setStatus(404);
            }

            return;
        }

        res.setStatus(500);
        res.setContentType("text/html; charset=UTF-8");

        SAXTransformerFactory saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

        InputStream is;

        File errorXSL = new File(config.getServletContext().getRealPath("error/error.xsl"));

        if (errorXSL.exists())
        {
            is = new FileInputStream(errorXSL);
        }
        else
        {
            is = getClass().getResourceAsStream("/org/ametys/runtime/kernel/pages/error/error.xsl");
        }

        StreamSource errorSource = new StreamSource(is);

        Templates templates = saxFactory.newTemplates(errorSource);

        TransformerHandler th = saxFactory.newTransformerHandler(templates);
        Properties format = new Properties();
        format.put(OutputKeys.METHOD, "html");
        format.put(OutputKeys.ENCODING, "UTF-8");
        th.getTransformer().setOutputProperties(format);

        th.getTransformer().setParameter("code", 500);
        th.getTransformer().setParameter("realpath", config.getServletContext().getRealPath("/"));
        th.getTransformer().setParameter("contextPath", req.getContextPath());

        is.close();

        StreamResult result = new StreamResult(os);
        th.setResult(result);

        th.startDocument();

        XMLUtils.startElement(th, "http://apache.org/cocoon/exception/1.0", "exception-report");
        XMLUtils.startElement(th, "http://apache.org/cocoon/exception/1.0", "message");
        saxErrorMessage(th);
        XMLUtils.endElement(th, "http://apache.org/cocoon/exception/1.0", "message");
        XMLUtils.endElement(th, "http://apache.org/cocoon/exception/1.0", "ex:exception-report");

        th.endDocument();
    }

    /**
     * In error mode, send error information as SAX events.<br>
     * 
     * @param ch the contentHandler receiving the message
     * @throws SAXException if an error occured while send SAX events
     */
    protected void saxErrorMessage(ContentHandler ch) throws SAXException
    {
        String errorMessage = "An error occurred. Please contact the administrator of the application.";
        XMLUtils.data(ch, errorMessage);
    }

    /*
     * private void _runMaintenanceMode(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException { ServletConfig config = getServletConfig(); if (config == null) { throw new ServletException("Cannot access to ServletConfig"); } ServletOutputStream os = res.getOutputStream(); if (req.getRequestURI().startsWith("/WEB-INF/error/")) { // Service des fichiers statiques de la page d'erreur File f = new File(config.getServletContext().getRealPath(req.getRequestURI())); if (f.exists()) { res.setStatus(200); InputStream is = new FileInputStream(f); SourceUtil.copy(is, os); is.close(); } else { res.setStatus(404); } return; } res.setStatus(500); SAXTransformerFactory saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance(); InputStream is; File errorXSL = new File(config.getServletContext().getRealPath("WEB-INF/error/error.xsl")); if (errorXSL.exists()) { is = new FileInputStream(errorXSL); } else { is = getClass().getResourceAsStream("/org/ametys/runtime/kernel/pages/error/error.xsl"); } try { StreamSource errorSource = new StreamSource(is); Templates templates = saxFactory.newTemplates(errorSource); TransformerHandler th = saxFactory.newTransformerHandler(templates); is.close(); StreamResult result = new StreamResult(os); th.setResult(result); th.startDocument(); th.startElement("", "error", "error", new AttributesImpl()); saxMaintenanceMessage(th); th.endElement("", "error", "error"); th.endDocument(); } catch (Exception ex) { throw new ServletException("Unable to send maintenance page", ex); } }
     */

    /**
     * In maintenance mode, send error information as SAX events.<br>
     * 
     * @param ch the contentHandler receiving the message
     * @throws SAXException if an error occured while send SAX events
     */
    /*
     * protected void saxMaintenanceMessage(ContentHandler ch) throws SAXException { String maintenanceMessage = "The application is under maintenance. Please retry later."; ch.characters(maintenanceMessage.toCharArray(), 0, maintenanceMessage.length()); } private boolean _accept(HttpServletRequest req) { // FIX ME à ne pas mettre en dur return req.getRequestURI().startsWith("_admin"); }
     */
}
