/*
 *  Copyright 2009 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.cocoon.Constants;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.servlet.multipart.RequestFactory;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;
import org.ametys.runtime.util.LoggerFactory;

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
    /**
     * Force the encoding to UTF-8 and delegates the actual processing to _doService
     */
    public final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        req.setCharacterEncoding("UTF-8");
        
        _doService(req, res);
    }

    /**
     * Process the HTTP request.
     * @param req the request
     * @param res the response
     * @throws ServletException if the HTTP request cannot be handled
     * @throws IOException if an I/O error occurs
     */
    protected void _doService(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        // Error mode
        if (exception != null)
        {
            try
            {
                _runErrorMode(req, res);
            }
            catch (ServletException e)
            {
                // Nothing to do anymore
                throw e;
            }
            catch (Exception e)
            {
                // Nothing to do as well
                throw new ServletException(e);
            }

            return;
        }
        
        MDC.put("requestURI", req.getRequestURI());

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
        // Set this property in order to avoid a System.err.println (CatalogManager.java)
        if (System.getProperty("xml.catalog.ignoreMissing") == null)
        {
            System.setProperty("xml.catalog.ignoreMissing", "true");
        }
        
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
                    // feature core/runtime.upload is deactivated, back to the web.xml value or 10 Mb by default
                    maxUploadSize = getInitParameterAsInteger("upload-max-size", 1073741824);
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
                    
                    // feature core/runtime.upload is deactivated, back to the web.xml value or "WEB-INF/data/uploads" by default
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
        
        // Create temp dir if it does not exist
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if (!tmpDir.exists())
        {
            try
            {
                FileUtils.forceMkdir(tmpDir);
            }
            catch (IOException e)
            {
                LoggerFactory.getLoggerFor(getClass()).warn("Unable to create temp directory", e);
            }
        }

        // WEB-INF/param/runtime.xml loading
        _loadRuntimeConfig();

        // Configuration file
        Config.setFilename(servletContext.getRealPath(CONFIG_RELATIVE_PATH));
    }

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

    private void _fireRequestEnded(HttpServletRequest req)
    {
        Collection<? extends RequestListener> listeners = (Collection< ? extends RequestListener>) servletContext.getAttribute(RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS);

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
        
        XMLUtils.startElement(th, "http://apache.org/cocoon/exception/1.0", "stacktrace");
        XMLUtils.data(th, ExceptionUtils.getStackTrace(exception));
        XMLUtils.endElement(th, "http://apache.org/cocoon/exception/1.0", "stacktrace");
        XMLUtils.endElement(th, "http://apache.org/cocoon/exception/1.0", "ex:exception-report");

        th.endDocument();
    }

    /**
     * In error mode, send error information as SAX events.<br>
     * 
     * @param ch the contentHandler receiving the message
     * @throws SAXException if an error occurred while send SAX events
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
