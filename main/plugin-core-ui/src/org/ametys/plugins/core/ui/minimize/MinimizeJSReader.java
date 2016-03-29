/*
 *  Copyright 2016 Anyware Services
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

package org.ametys.plugins.core.ui.minimize;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;

import org.ametys.plugins.core.ui.minimize.MinimizeTransformer.FileData;

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
    protected String _handleFile(FileData file, String contextPath)
    {
        StringBuffer sb = new StringBuffer();
        
        Source jssource = null;
        try
        {
            URI uriToResolve = new URI(file.getUri());
            if (!uriToResolve.isAbsolute())
            {
                uriToResolve = new URI("cocoon:/" + file.getUri());
            }
            jssource = _resolver.resolveURI(uriToResolve.normalize().toString());
            
            String s;
            try (InputStream is = jssource.getInputStream())
            {
                s = IOUtils.toString(is);
            }
            
            JavaScriptCompressor compressor;
            try (Reader r = new StringReader(s))
            {
                compressor = new JavaScriptCompressor(r, new LoggerErrorReporter(getLogger()));
            }
            
            Writer w = new StringWriter();
            compressor.compress(w, 8000, false, false, true, true);
            
            sb.append("/** File : " + file.getUri() + " */\n");
            sb.append(w.toString());
            sb.append("\n");
        }
        catch (Exception e)
        {
            getLogger().error("Cannot minimize JS for aggregation " + file, e);
            sb.append("/** ERROR " + e.getMessage() + "*/");
        }
        finally
        {
            _resolver.release(jssource);
        }

        return sb.toString();
    }
}
