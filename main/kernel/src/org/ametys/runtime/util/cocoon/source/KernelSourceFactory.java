package org.ametys.runtime.util.cocoon.source;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.excalibur.source.SourceFactory;

import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * {@link SourceFactory} lokking for the kernel in the classpath first and then in the external kernel, if any.
 */
public class KernelSourceFactory extends ProxySourceFactory
{
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _matcher = Pattern.compile("kernel://(.*)");
        
        _protocols = new ArrayList<String>();
        _protocols.add("resource://org/ametys/runtime/kernel/{1}");
        
        File externalKernel = RuntimeConfig.getInstance().getExternalKernel();
        
        if (externalKernel != null)
        {
            _protocols.add(externalKernel.toURI() + "{1}");
        }
    }
}
