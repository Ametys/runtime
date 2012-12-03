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

package org.ametys.runtime.plugins.core.ui.minimize;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * This generator generates a single JS file to load all ui items js files.
 * Can generates a list of imports of directly intergrates all js files.
 */
public class MinimizeJSReader extends AbstractMinimizeReader
{
    @Override
    public String getMimeType()
    {
        return "text/javascript;charset=utf-8";
    }

    @Override
    protected String getListCode()
    {
        return "$js";
    }

    @Override
    protected String _handleFileDirect(String file, String contextPath)
    {
        StringBuffer sb = new StringBuffer();
        
        Source jssource = null;
        InputStream is = null;
        try
        {
            jssource = _resolver.resolveURI(StringUtils.startsWith(file, "~") ? "cocoon:/" + org.apache.cocoon.util.NetUtils.normalize(file.substring(1)) : file);
            is = jssource.getInputStream();
            
            String s = IOUtils.toString(is);
            
            Reader r = new StringReader(s);
            JavaScriptCompressor compressor = new JavaScriptCompressor(r, new LoggerErrorReporter(getLogger()));
            r.close();
            
            Writer w = new StringWriter();
            compressor.compress(w, 8000, false, false, true, true);
            
            sb.append(w.toString());
        }
        catch (Exception e)
        {
            getLogger().error("Cannot minimize JS for aggregation " + file, e);
            sb.append("/** ERROR " + e.getMessage() + "*/");
        }
        finally
        {
            IOUtils.closeQuietly(is);
            _resolver.release(jssource);
        }

        return sb.toString();
    }
    
    @Override
    protected String _handleFileImport(String file, String contextPath)
    {
        return "document.write(\"<script type='text/javascript' src='" + (StringUtils.startsWith(file, "~") ? contextPath + org.apache.cocoon.util.NetUtils.normalize(file.substring(1)) : file)  + "'><!-- import --></script>\");\n";
    }
}
