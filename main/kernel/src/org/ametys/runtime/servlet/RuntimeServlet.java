/*
 *  Copyright 2012 Anyware Services
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
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.servlet.multipart.MultipartHttpServletRequest;
import org.apache.cocoon.servlet.multipart.RequestFactory;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.apache.cocoon.util.log.SLF4JLoggerManager;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.xml.sax.XMLReader;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.data.AmetysHomeLock;
import org.ametys.runtime.data.AmetysHomeLockException;
import org.ametys.runtime.log.MemoryAppender;
import org.ametys.runtime.plugin.Init;
import org.ametys.runtime.plugin.InitExtensionPoint;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.Status;
import org.ametys.runtime.plugin.component.PluginsComponentManager;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;
import org.ametys.runtime.util.AmetysHomeHelper;

/**
 * Main entry point for applications.<br>
 * Overrides the CocoonServlet to add some initialization.<br>
 */
public class RuntimeServlet extends HttpServlet
{
    /** Constant for storing the {@link ServletConfig} in the Avalon context  */
    public static final String CONTEXT_SERVLET_CONFIG = "servlet-config";
    
    /** Constant for storing the servlet context URL in the Avalon context  */
    public static final String CONTEXT_CONTEXT_ROOT = "context-root";
    
    /** The cocoon.xconf URL */
    public static final String COCOON_CONF_URL = "/org/ametys/runtime/cocoon/cocoon.xconf";
    
    /** Default max upload size (10 Mb) */
    public static final int DEFAULT_MAX_UPLOAD_SIZE = 10 * 1024 * 1024;

    /** The config file name */
    public static final String CONFIG_FILE_NAME = "config.xml";
    
    /** Name of the servlet initialization parameter for the ametys home property */
    public static final String AMETYS_HOME_PROPERTY = "ametys.home.property";
    
    /** The default ametys home path (relative to the servlet context)  */
    public static final String AMETYS_HOME_DEFAULT = "/WEB-INF/data";

    /** The run modes */
    public enum RunMode
    {
        /** Normal execution mode */
        NORMAL,
        /** Maintenance mode required by administrator */
        // MAINTENANCE,
    }
    
    private static RunMode _mode = RunMode.NORMAL;
    
    private ServletContext _servletContext;
    private String _servletContextPath;
    private URL _servletContextURL;
    
    private DefaultContext _avalonContext;
    private HttpContext _context;
    private Cocoon _cocoon;
    private RequestFactory _requestFactory;
    private File _workDir;
    
    private int _maxUploadSize = DEFAULT_MAX_UPLOAD_SIZE;
    private File _uploadDir;
    private File _cacheDir;
    
    private File _ametysHome;
    private AmetysHomeLock _ametysHomeLock;
    
    private Logger _logger;
    private LoggerManager _loggerManager;
    
    private Exception _exception;
    
    private Collection<Pattern> _allowedURLPattern = Arrays.asList(Pattern.compile("_admin($|/.*)"), Pattern.compile("plugins/[^/]+/resources/.*"));
    
    @Override
    public void init() throws ServletException 
    {
        try
        {
            // Set this property in order to avoid a System.err.println (CatalogManager.java)
            if (System.getProperty("xml.catalog.ignoreMissing") == null)
            {
                System.setProperty("xml.catalog.ignoreMissing", "true");
            }
            
            _servletContext = getServletContext();
            _servletContextPath = _servletContext.getRealPath("/");
            
            _avalonContext = new DefaultContext();
            _context = new HttpContext(_servletContext);
            _avalonContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, _context);
            _avalonContext.put(Constants.CONTEXT_DEFAULT_ENCODING, "UTF-8");
            _avalonContext.put(CONTEXT_SERVLET_CONFIG, getServletConfig());
            
            _servletContextURL = new File(_servletContextPath).toURI().toURL();
            _avalonContext.put(CONTEXT_CONTEXT_ROOT, _servletContextURL);
        
            URL configFile = getClass().getResource(COCOON_CONF_URL);
            _avalonContext.put(Constants.CONTEXT_CONFIG_URL, configFile);
            
            _workDir = new File((File) _servletContext.getAttribute("javax.servlet.context.tempdir"), "cocoon-files");
            _workDir.mkdirs();
            _avalonContext.put(Constants.CONTEXT_WORK_DIR, _workDir);
            
            _cacheDir = new File(_workDir, "cache-dir");
            _cacheDir.mkdirs();
            _avalonContext.put(Constants.CONTEXT_CACHE_DIR, _cacheDir);
    
            // Create temp dir if it does not exist
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            if (!tmpDir.exists())
            {
                FileUtils.forceMkdir(tmpDir);
            }
            
            // Init the Ametys home directory and lock before setting the logger.
            // The log folder is located in the Ametys home directory.
            _initAmetysHome();
            
            // Configuration file
            File config = FileUtils.getFile(_ametysHome, AmetysHomeHelper.AMETYS_HOME_CONFIG_DIR, CONFIG_FILE_NAME);
            Config.setFilename(config.getCanonicalPath());
            
            // Init logger
            _initLogger();
            
            _initAmetys();
        }
        catch (Throwable t)
        {
            if (_logger != null)
            {
                _logger.error("Error while loading Ametys. Entering in error mode.", t);
            }
            else
            {
                System.out.println("Error while loading Ametys. Entering in error mode.");
                t.printStackTrace();
            }

            if (t instanceof Exception)
            {
                _exception = (Exception) t;
            }
            else
            {
                _exception = new Exception(t);
            }
            
            _disposeCocoon();
        }
    }
    
    private void _initAmetysHome() throws AmetysHomeLockException
    {
        String ametysHomePath = null;
        
        boolean hasAmetysHomeProp = EnumerationUtils.toList(getInitParameterNames()).contains(AMETYS_HOME_PROPERTY);
        if (!hasAmetysHomeProp)
        {
            System.out.println(String.format("[INFO] The '%s' servlet initialization parameter was not found. The Ametys home directory default value will be used '%s'",
                    AMETYS_HOME_PROPERTY, AMETYS_HOME_DEFAULT));
        }
        else
        {
            String ametysHomeEnv = getInitParameter(AMETYS_HOME_PROPERTY);
            if (StringUtils.isEmpty(ametysHomeEnv))
            {
                System.out.println(String.format("[WARN] The '%s' servlet initialization parameter appears to be empty. Ametys home directory default value will be used '%s'",
                        AMETYS_HOME_PROPERTY, AMETYS_HOME_DEFAULT));
            }
            else
            {
                ametysHomePath = System.getenv(ametysHomeEnv);
                if (StringUtils.isEmpty(ametysHomePath))
                {
                    System.out.println(String.format(
                            "[WARN] The '%s' environment variable was not found or was empty. Ametys home directory default value will be used '%s'",
                            ametysHomeEnv, AMETYS_HOME_DEFAULT));
                }
            }
        }
        
        if (StringUtils.isEmpty(ametysHomePath))
        {
            ametysHomePath = _servletContext.getRealPath(AMETYS_HOME_DEFAULT);
        }
        
        System.out.println("Acquiring lock on " + ametysHomePath);
        
        // Acquire the lock on Ametys home
        _ametysHome = new File(ametysHomePath);
        _ametysHome.mkdirs();
        
        _ametysHomeLock = new AmetysHomeLock(_ametysHome);
        _ametysHomeLock.acquire();
    }

    private void _initAmetys() throws Exception
    {
        // WEB-INF/param/runtime.xml loading
        _loadRuntimeConfig();

        _createCocoon();
        
        // Upload initialization
        _maxUploadSize = DEFAULT_MAX_UPLOAD_SIZE;
        _uploadDir = new File(RuntimeConfig.getInstance().getAmetysHome(), "uploads");
        
        if (ConfigManager.getInstance().isComplete())
        {
            Long maxUploadSizeParam = Config.getInstance().getValueAsLong("runtime.upload.max-size");
            if (maxUploadSizeParam != null)
            {
                // if the feature core/runtime.upload is deactivated, use the default value (10 Mb)
                _maxUploadSize = maxUploadSizeParam.intValue();
            }
        }

        _uploadDir.mkdirs();
        _avalonContext.put(Constants.CONTEXT_UPLOAD_DIR, _uploadDir);
        
        _requestFactory = new RequestFactory(true, _uploadDir, false, true, _maxUploadSize, "UTF-8");

        // Init classes
        _initPlugins();
    }
    
    private void _initLogger() 
    {
        // Configure Log4j
        String logj4fFile = _servletContext.getRealPath("/WEB-INF/log4j.xml");
        
        // Hack to have context-relative log files, because of lack in configuration capabilities in log4j.
        // If there are more than one Ametys in the same JVM, the property will be successively set for each instance, 
        // so we heavily rely on DOMConfigurator beeing synchronous.
        System.setProperty("ametys.home.dir", _ametysHome.getAbsolutePath());
        DOMConfigurator.configure(logj4fFile);
        System.clearProperty("ametys.home.dir");
        
        Appender appender = new MemoryAppender(); 
        appender.setName("memory-appender");
        LogManager.getRootLogger().addAppender(appender); 
        Enumeration<org.apache.log4j.Logger> categories = LogManager.getCurrentLoggers(); 
        while (categories.hasMoreElements()) 
        { 
            org.apache.log4j.Logger logger = categories.nextElement(); 
            logger.addAppender(appender); 
        } 
        
        _loggerManager = new SLF4JLoggerManager();
        _logger = LoggerFactory.getLogger(getClass());
    }
    
    private void _createCocoon() throws Exception
    {
        _exception = null;
        
        _avalonContext.put(Constants.CONTEXT_CLASS_LOADER, getClass().getClassLoader());
        _avalonContext.put(Constants.CONTEXT_CLASSPATH, "");
        
        URL configFile = (URL) _avalonContext.get(Constants.CONTEXT_CONFIG_URL);
        
        _logger.info("Reloading from: {}", configFile.toExternalForm());
        
        Cocoon c = (Cocoon) ClassUtils.newInstance("org.apache.cocoon.Cocoon");
        ContainerUtil.enableLogging(c, new SLF4JLoggerAdapter(_logger));
        c.setLoggerManager(_loggerManager);
        ContainerUtil.contextualize(c, _avalonContext);
        ContainerUtil.initialize(c);

        _cocoon = c;
    }
    
    private void _initPlugins() throws Exception
    {
        PluginsComponentManager pluginCM = (PluginsComponentManager) _servletContext.getAttribute("PluginsComponentManager");
        
        // If we're in safe mode 
//        if (!PluginsManager.getInstance().isSafeMode())
//        {
        // FIXME the conditional was commented temporary for creating admin users SQL table even in safe mode
        // Plugins Init class execution
        InitExtensionPoint initExtensionPoint = (InitExtensionPoint) pluginCM.lookup(InitExtensionPoint.ROLE);
        for (String id : initExtensionPoint.getExtensionsIds())
        {
            Init init = initExtensionPoint.getExtension(id);
            init.init();
        }
        
        // Application Init class execution if available
        if (pluginCM.hasComponent(Init.ROLE))
        {
            Init init = (Init) pluginCM.lookup(Init.ROLE);
            init.init();
        }
//        }
    }
    
    private void _loadRuntimeConfig() throws ServletException
    {
        Configuration runtimeConf = null;
        try
        {
            // XML Schema validation
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            
            URL schemaURL = getClass().getResource("runtime-4.0.xsd");
            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaURL);
            factory.setSchema(schema);
            
            XMLReader reader = factory.newSAXParser().getXMLReader();
            DefaultConfigurationBuilder runtimeConfBuilder = new DefaultConfigurationBuilder(reader);
            
            File runtimeConfigFile = new File(_servletContextPath, "WEB-INF/param/runtime.xml");
            try (InputStream runtime = new FileInputStream(runtimeConfigFile))
            {
                runtimeConf = runtimeConfBuilder.build(runtime, runtimeConfigFile.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            _logger.error("Unable to load runtime file at 'WEB-INF/param/runtime.xml'. PluginsManager will enter in safe mode.", e);
        }
        
        Configuration externalConf = null;
        try
        {
            DefaultConfigurationBuilder externalConfBuilder = new DefaultConfigurationBuilder();

            File externalConfigFile = new File(_servletContextPath, "WEB-INF/param/external-locations.xml");
            if (externalConfigFile.exists())
            {
                try (InputStream external = new FileInputStream(externalConfigFile))
                {
                    externalConf = externalConfBuilder.build(external, externalConfigFile.getAbsolutePath());
                }
            }
        }
        catch (Exception e)
        {
            _logger.error("Unable to load external locations values at WEB-INF/param/external-locations.xml", e);
            throw new ServletException("Unable to load external locations values at WEB-INF/param/external-locations.xml", e);
        }
        
        RuntimeConfig.configure(runtimeConf, externalConf, _ametysHome, _servletContextPath);
    }

    @Override
    public void destroy() 
    {
        if (_cocoon != null) 
        {
            _logger.debug("Servlet destroyed - disposing Cocoon");
            _disposeCocoon();
        }
        
        if (_ametysHomeLock != null)
        {
            _ametysHomeLock.release();
            _ametysHomeLock = null;
        }
        
        _avalonContext = null;
        _logger = null;
        _loggerManager = null;
    }

    
    private final void _disposeCocoon() 
    {
        if (_cocoon != null) 
        {
            ContainerUtil.dispose(_cocoon);
            _cocoon = null;
        }
    }

    @Override
    public final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        req.setCharacterEncoding("UTF-8");

        // add the cocoon header timestamp
        res.addHeader("X-Cocoon-Version", Constants.VERSION);
        
        // Error mode
        if (_exception != null)
        {
            _renderError(req, res, _exception, "An error occured during Ametys initialization.");
            return;
        }
        
        String uri = _retrieveUri(req);
        
        if (PluginsManager.getInstance().isSafeMode())
        {
            // safe mode
            String finalUri = uri;
            boolean allowed = _allowedURLPattern.stream().anyMatch(p -> p.matcher(finalUri).matches());
            if (!allowed) 
            {
                res.addHeader("X-Ametys-SafeMode", "true");

                Status status = PluginsManager.getInstance().getStatus();
                
                if (status == Status.NO_CONFIG)
                {
                    res.sendRedirect(req.getContextPath() + "/_admin/public/first-start.html");
                    return;
                }
                else if (status == Status.CONFIG_INCOMPLETE)
                {
                    res.sendRedirect(req.getContextPath() + "/_admin/public/load-config.html");
                    return;
                }
                else
                {
                    res.sendRedirect(req.getContextPath() + "/_admin/public/safe-mode.html");
                    return;
                }
            }
        }
        
        HttpSession session = req.getSession(false); 
        if (session != null) 
        { 
            String authenticatedUser = (String) session.getAttribute("Runtime:UserLogin"); 
            
            if (authenticatedUser != null)
            {
                MDC.put("user", authenticatedUser);
            }
        }
        
        MDC.put("requestURI", req.getRequestURI());

        // if (getRunMode() == RunMode.MAINTENANCE && !_accept(req))
        // {
        // _runMaintenanceMode(req, res);
        // }
        // else
        // {
        
        StopWatch stopWatch = new StopWatch();
        HttpServletRequest request = null;
        try 
        {
            // used for timing the processing
            stopWatch.start();

            _fireRequestStarted(req);
            
            // get the request (wrapped if contains multipart-form data)
            request = _requestFactory.getServletRequest(req);
            
            // Process the request
            HttpEnvironment env = new HttpEnvironment(uri, _servletContextURL.toExternalForm(), request, res, _servletContext, _context, "UTF-8", "UTF-8");
            env.enableLogging(new SLF4JLoggerAdapter(_logger));
            
            if (!_cocoon.process(env)) 
            {
                // We reach this when there is nothing in the processing change that matches
                // the request. For example, no matcher matches.
                _logger.error("The Cocoon engine failed to process the request.");
                _renderError(request, res, null, "Cocoon engine failed to process the request");
            }
        } 
        catch (ResourceNotFoundException | ConnectionResetException | IOException e) 
        {
            _logger.warn(e.toString());
            _renderError(request, res, e, e.getMessage());
        } 
        catch (Exception e) 
        {
            _logger.error("Internal Cocoon Problem", e);
            _renderError(request, res, e, "Internal Cocoon Problem");
        }
        finally 
        {
            stopWatch.stop();
            _logger.info("'{}' processed in {} ms.", uri, stopWatch.getTime());
            
            try
            {
                if (request instanceof MultipartHttpServletRequest) 
                {
                    _logger.debug("Deleting uploaded file(s).");
                    ((MultipartHttpServletRequest) request).cleanup();
                }
            } 
            catch (IOException e)
            {
                _logger.error("Cocoon got an Exception while trying to cleanup the uploaded files.", e);
            }
        }

        _fireRequestEnded(req);

        if (Boolean.TRUE.equals(req.getAttribute("org.ametys.runtime.reload")))
        {
            restartCocoon(req);
        }
        // }
    }
    
    /**
     * Restart cocoon
     * @param req The http servlet request, used to read safeMode and normalMode
     *            request attribute if possible. If null, safe mode will be
     *            forced only if it was alread forced.
     */
    public void restartCocoon(HttpServletRequest req)
    {
        try
        {
            ConfigManager.getInstance().dispose();
            _disposeCocoon(); 
            _servletContext.removeAttribute("PluginsComponentManager");
            
            // By default force safe mode if it was already forced
            boolean wasForcedSafeMode = PluginsManager.Status.SAFE_MODE_FORCED.equals(PluginsManager.getInstance().getStatus());
            boolean forceSafeMode = wasForcedSafeMode;
            if (req != null)
            {
                // Also, checks if there is some specific request attributes
                // Force safe mode if is explicitly requested
                // Or force safe mode it was already forced unless normal mode is explicitly requested 
                forceSafeMode = Boolean.TRUE.equals(req.getAttribute("org.ametys.runtime.reload.safeMode"));
                if (!forceSafeMode)
                {
                    // 
                    forceSafeMode = wasForcedSafeMode && !Boolean.TRUE.equals(req.getAttribute("org.ametys.runtime.reload.normalMode"));
                }
            }
            
            _servletContext.setAttribute("org.ametys.runtime.forceSafeMode", forceSafeMode);
            
            _initAmetys();
        }
        catch (Exception e)
        {
            _logger.error("Error while reloading Ametys. Entering in error mode.", e);
            _exception = e;
        }
    }
    
    private String _retrieveUri(HttpServletRequest req)
    {
        String uri = req.getServletPath();
        
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) 
        {
            uri += pathInfo;
        }
        
        if (uri.length() > 0 && uri.charAt(0) == '/') 
        {
            uri = uri.substring(1);
        }
        return uri;
    }
    
    @SuppressWarnings("unchecked")
    private void _fireRequestStarted(HttpServletRequest req)
    {
        Collection< ? extends RequestListener> listeners = (Collection< ? extends RequestListener>) _servletContext.getAttribute(RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS);

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
        Collection<? extends RequestListener> listeners = (Collection< ? extends RequestListener>) _servletContext.getAttribute(RequestListenerManager.CONTEXT_ATTRIBUTE_REQUEST_LISTENERS);

        if (listeners == null)
        {
            return;
        }

        for (RequestListener listener : listeners)
        {
            listener.requestEnded(req);
        }
    }

    /**
     * Set the run mode
     * @param mode the running mode
     */
    public static void setRunMode(RunMode mode)
    {
        _mode = mode;
    }

    /**
     * Get the run mode
     * @return the current run mode
     */
    public static RunMode getRunMode()
    {
        return _mode;
    }

    private void _renderError(HttpServletRequest req, HttpServletResponse res, Exception exception, String message) throws ServletException
    {
        ServletConfig config = getServletConfig();

        if (config == null)
        {
            throw new ServletException("Cannot access to ServletConfig");
        }

        try
        {
            ServletOutputStream os = res.getOutputStream();
            String path = req.getRequestURI().substring(req.getContextPath().length());
    
            // Favicon associated with the error page.
            if (path.equals("/favicon.ico"))
            {
                try (InputStream is = getClass().getResourceAsStream("favicon.ico"))
                {
                    res.setStatus(200);
                    res.setContentType(config.getServletContext().getMimeType("favicon.ico"));
                    
                    IOUtils.copy(is, os);
                    
                    return;
                }
            }
    
            res.setStatus(500);
            res.setContentType("text/html; charset=UTF-8");
    
            SAXTransformerFactory saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            TransformerHandler th;
            
            try (InputStream is = getClass().getResourceAsStream("fatal.xsl"))
            {
                StreamSource errorSource = new StreamSource(is);
                th = saxFactory.newTransformerHandler(errorSource);
            }
            
            Properties format = new Properties();
            format.put(OutputKeys.METHOD, "xml");
            format.put(OutputKeys.ENCODING, "UTF-8");
            format.put(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");
    
            th.getTransformer().setOutputProperties(format);
    
            th.getTransformer().setParameter("code", 500);
            th.getTransformer().setParameter("realPath", config.getServletContext().getRealPath("/"));
            th.getTransformer().setParameter("contextPath", req.getContextPath());
            
            StreamResult result = new StreamResult(os);
            th.setResult(result);
    
            th.startDocument();
    
            XMLUtils.startElement(th, "http://apache.org/cocoon/exception/1.0", "exception-report");
            XMLUtils.startElement(th, "http://apache.org/cocoon/exception/1.0", "message");
            XMLUtils.data(th, message);
            XMLUtils.endElement(th, "http://apache.org/cocoon/exception/1.0", "message");
            
            XMLUtils.startElement(th, "http://apache.org/cocoon/exception/1.0", "stacktrace");
            XMLUtils.data(th, ExceptionUtils.getStackTrace(exception));
            XMLUtils.endElement(th, "http://apache.org/cocoon/exception/1.0", "stacktrace");
            XMLUtils.endElement(th, "http://apache.org/cocoon/exception/1.0", "ex:exception-report");
    
            th.endDocument();
        }
        catch (Exception e)
        {
            // Nothing we can do anymore ...
            throw new ServletException(e);
        }
    }

    /*
     * private void _runMaintenanceMode(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException { ServletConfig config = getServletConfig(); if (config == null) { throw new ServletException("Cannot access to ServletConfig"); } ServletOutputStream os = res.getOutputStream(); if (req.getRequestURI().startsWith("/WEB-INF/error/")) { // Service des fichiers statiques de la page d'erreur File f = new File(config.getServletContext().getRealPath(req.getRequestURI())); if (f.exists()) { res.setStatus(200); InputStream is = new FileInputStream(f); SourceUtil.copy(is, os); is.close(); } else { res.setStatus(404); } return; } res.setStatus(500); SAXTransformerFactory saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance(); InputStream is; File errorXSL = new File(config.getServletContext().getRealPath("/WEB-INF/error/error.xsl")); if (errorXSL.exists()) { is = new FileInputStream(errorXSL); } else { is = getClass().getResourceAsStream("/org/ametys/runtime/kernel/pages/error/error.xsl"); } try { StreamSource errorSource = new StreamSource(is); Templates templates = saxFactory.newTemplates(errorSource); TransformerHandler th = saxFactory.newTransformerHandler(templates); is.close(); StreamResult result = new StreamResult(os); th.setResult(result); th.startDocument(); th.startElement("", "error", "error", new AttributesImpl()); saxMaintenanceMessage(th); th.endElement("", "error", "error"); th.endDocument(); } catch (Exception ex) { throw new ServletException("Unable to send maintenance page", ex); } }
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
