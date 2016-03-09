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
package org.ametys.core.cocoon;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

import org.ametys.core.util.I18nUtils;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * This class generates a translated version of an input file. 
 * It is designed to handle the following notation : {{i18n x}} <br>
 * When encountering this pattern, we instantiate an {@link I18nizableText}
 * with x and try to translate it. <br>
 * Unknown translations are logged and do not prevent the generation process from continuing.
 */
public class I18nTextReader extends ServiceableReader implements CacheableProcessingComponent
{
    /**
     * This configuration parameter specifies the id of the catalogue to be used as
     * default catalogue, allowing to redefine the default catalogue on the pipeline
     * level.
     */
    private static final String __I18N_DEFAULT_CATALOGUE_ID = "default-catalogue-id";
    
    /** The beginning of internationalizable text we have to translate */
    private static final String __I18N_BEGINNING = "{{i18n";
    
    /** The end of internationalizable text we have to translate */
    private static final String __I18N_END = "}}";
    
    /** Avalon component gathering utility methods concerning internationalizable text, allowing their translation in several languages */
    private I18nUtils _i18nUtils;
    
    /** The locale */
    private String _locale;

    /** The default catalogue defined in the sitemap */
    private String _defaultCatalogue;
    
    /** The name of the plugin */
    private String _pluginCatalogue;
    
    /** The name of the workspace */
    private String _workspaceName; 
    
    /** The input source */
    private Source _inputSource;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _i18nUtils = (I18nUtils) serviceManager.lookup(I18nUtils.ROLE);
    }
    
    @Override
    public void setup(SourceResolver initalResolver, Map cocoonObjectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        try 
        {
            _inputSource = initalResolver.resolveURI(src);
        } 
        catch (SourceException e) 
        {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", e);
        }
        
        super.setup(initalResolver, cocoonObjectModel, src, par);
        
        // Compute the locale
        _locale = par.getParameter("locale", null);
        if (_locale == null)
        {
            Locale locale = org.apache.cocoon.i18n.I18nUtils.findLocale(cocoonObjectModel, "locale", null, Locale.getDefault(), true);
            _locale = locale.getLanguage();
        }
        
        int catalogueDefinitionAmount = 0;
        String plugin = par.getParameter("plugin", null);
        if (plugin != null)
        {
            _pluginCatalogue = "plugin." + plugin;
            catalogueDefinitionAmount++;
        }
        
        String workspaceName = par.getParameter("workspace", null);
        if (workspaceName != null)
        {
            _workspaceName = "workspace." + workspaceName;
            catalogueDefinitionAmount++;
        }
        
        String defaultCatalogue = par.getParameter(__I18N_DEFAULT_CATALOGUE_ID, null);
        if (defaultCatalogue != null)
        {
            _defaultCatalogue = par.getParameter(__I18N_DEFAULT_CATALOGUE_ID, null);
            catalogueDefinitionAmount++;
        }
        
        if (catalogueDefinitionAmount > 1)
        {
            throw new ProcessingException("There is a conflict for the selection of the catalogue to use. You can only pick one catalogue at a time, "
                    + "either with the id of a default catalogue, the name of a plugin or of a workspace.");
        }
    }
    
    @Override
    public void generate() throws IOException, ProcessingException
    {
        if (!_inputSource.exists())
        {
            throw new ResourceNotFoundException("Resource not found for URI : " + _inputSource.getURI());
        }
        
        try (InputStream is = _inputSource.getInputStream())
        {
            StringBuilder outStringBuilder = new StringBuilder (); // Use a StringBuilder to dynamically allocate memory
            
            int beginningLength = __I18N_BEGINNING.length();
            int endLength = __I18N_END.length();
            int minI18nDeclarationLength = beginningLength + 1 + 1 + endLength; // 1 mandatory backspace and at least 1 character for the key
            
            char[] srcChars = IOUtils.toCharArray(is, "UTF-8");
            int srcLength = srcChars.length;
            
            int skip = 0; // Avoid checkstyle warning : "Control variable 'i' is modified"
            for (int i = 0; i < srcLength; i = i + skip)
            {
                skip = 1;
                char c = srcChars[i];
                
                // Do not bother analyzing when there is no room for a valid declaration
                if (c == '{' && i + minI18nDeclarationLength < srcLength)
                {
                    char[] candidateBeginning = {c, srcChars[i + 1], srcChars[i + 2], srcChars[i + 3], srcChars[i + 4], srcChars[i + 5]};
                    if (String.valueOf(candidateBeginning).equals(__I18N_BEGINNING))
                    {
                        // Keep the analyzed characters to put them in the output stream 
                        // in case we know the character sequence won't make a viable candidate
                        StringBuilder invalidDeclarationBuffer = new StringBuilder ();
                        invalidDeclarationBuffer.append(__I18N_BEGINNING);

                        Character backspaceCandidate = srcChars[i + beginningLength];
                        invalidDeclarationBuffer.append(backspaceCandidate);
                        
                        if (backspaceCandidate != ' ')
                        {
                            _warnIfEnabled("Invalid i18n declaration in the file  '" + _inputSource.getURI() + "': '{{i18n' must be followed by a backspace.");
                            // Copy the analyzed characters, since we won't find a valid candidate before ' '
                            outStringBuilder.append(invalidDeclarationBuffer);

                            // Update the amount of skipped characters for the next iteration
                            skip += beginningLength;
                            
                            continue;
                        }
                        
                        // Valid candidate so far, check the end of the notation
                        skip = _analyzeAndWriteOutput(srcChars, i, outStringBuilder, invalidDeclarationBuffer);
                    }
                    else
                    {
                        outStringBuilder.append(c);
                    }
                }
                // Escape '{'
                else if (c == '\\' && i < srcLength && srcChars[i + 1] == '{')
                {
                    outStringBuilder.append('{');
                    skip++;
                }
                else
                {
                    // Blindly copy the input character 
                    outStringBuilder.append(c);
                }
            }
            
            IOUtils.write(outStringBuilder.toString(), out, "UTF-8");
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
    
    /**
     * Analyze characters from the key beginning index to the possible closure sequence '}}',
     * and write the appropriate replacement in the output string builder
     * @param srcChars the input file as characters
     * @param candidateBeginingIndex the index at which we started analyzing a viable i18n declaration
     * @param outStringBuilder the string builder where we store the output string
     * @param invalidDeclarationBuffer the string builder where we build the string to use in case the declaration is invalid
     * @return the amount of analyzed characters
     */
    private int _analyzeAndWriteOutput(char[] srcChars, int candidateBeginingIndex, StringBuilder outStringBuilder, StringBuilder invalidDeclarationBuffer)
    {
        int keyBeginningIndex = candidateBeginingIndex + __I18N_BEGINNING.length() + 1; 
        boolean invalid = false;
        boolean valid = false;
        int srcLength = srcChars.length;
        int j = keyBeginningIndex;
        
        while (j < srcLength && !invalid && !valid)
        {
            char c = srcChars[j];
            invalidDeclarationBuffer.append(c);
            switch (c)
            {
                case '{':
                    if (srcChars[j + 1] == '{')
                    {
                        _warnIfEnabled("Invalid i18n declaration in the file  '" + _inputSource.getURI() + "': '{{' within an i18n declaration is forbidden.");
                        invalidDeclarationBuffer.append(srcChars[j + 1]);
                        invalid = true;
                    }
                    break;  
                    
                case '}':
                    if (srcChars[j + 1] == '}')
                    {
                        invalidDeclarationBuffer.append(srcChars[j + 1]);
                        if (j == keyBeginningIndex)
                        {
                            _warnIfEnabled("Invalid i18n declaration in the file  '" + _inputSource.getURI() + "': a key must be specified.");
                            invalid = true;
                        }
                        else
                        {
                            valid = true;
                        }
                    }
                    break;
                    
                case '\n':
                    _warnIfEnabled("Invalid i18n declaration in the file  '" + _inputSource.getURI() + "': '\\n' within an i18n declaration is forbidden."
                            + " Make sure all i18n declarations are closed with the sequence '}}'.");
                    invalidDeclarationBuffer.append(srcChars[j + 1]);
                    invalid = true;
                    break;
                    
                default:
                    break;
            }
            
            j++;
        }
        
        if (valid)
        {
            // Proper i18n declaration : extract the key, translate it and replace the i18n declaration with its translation
            outStringBuilder.append(_getTranslation(srcChars, keyBeginningIndex, j - 1));
        }
        
        if (invalid)
        {
            // Copy the entirety of the tested sequence and update cursor since there is no valid termination 
            // to be found for an i18n declaration amongst the analyzed characters
            outStringBuilder.append(invalidDeclarationBuffer.toString());
        }
        
        if (j == srcLength && !valid && !invalid)
        {
            // We've reached the end of the file without encountering the closing sequence '}}', and the declaration has not been found valid
            // nor invalid yet
            _warnIfEnabled("Invalid i18n declaration in the file  '" + _inputSource.getURI() + "': Reached end of the file without finding the closing sequence of an i18n declaration.");
            outStringBuilder.append(invalidDeclarationBuffer.toString());
        }
        
        return j - candidateBeginingIndex + 1;
    }

    /**
     * Get the i18n key in the input characters and return its translation
     * @param srcChars the source characters
     * @param beginIndex the beginning index of the i18n key
     * @param endIndex the end index of the i18n key
     * @return the translated key
     */
    private String _getTranslation(char[] srcChars, int beginIndex, int endIndex)
    {
        // Extract the key and the catalogue
        int keyLength = endIndex - beginIndex;
        char[] keyAsChars = new char[keyLength];
        for (int k = 0; k < keyLength; k++)
        {
            keyAsChars[k] = srcChars[beginIndex + k];
        }
        
        String key = String.valueOf(keyAsChars);
        int indexOfSemiColon = key.indexOf(':');
        String catalogue = null;
        if (indexOfSemiColon != -1)
        {
            catalogue = key.substring(0, key.indexOf(':'));
            key = key.substring(indexOfSemiColon + 1, key.length());
        }
        
        if (catalogue == null)
        {
            if (_pluginCatalogue != null)
            {
                catalogue = _pluginCatalogue;
            }
            else if (_workspaceName != null)
            {
                catalogue = _workspaceName;
            }
            else if (_defaultCatalogue != null)
            {
                catalogue = _defaultCatalogue;
            }
        }
        
        // Attempt to translate and return either the translation or the key
        String translation = _i18nUtils.translate(new I18nizableText(catalogue, key.trim()), _locale);
        if (translation == null)
        {
            _warnIfEnabled("Translation not found for key '" + key + "' in catalogue '" + catalogue + "'.");
        }
        
        return translation != null ? translation : __I18N_BEGINNING + " " + key + __I18N_END; 
    }
    
    /**
     * Log the given string as a warning, if warnings are enabled
     * @param message the message to write
     */
    private void _warnIfEnabled(String message)
    {
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn(message);
        }
    }
    
    @Override
    public Serializable getKey()
    {
        return _inputSource.getURI() + "*" + _locale;
    }
    
    @Override
    public SourceValidity getValidity()
    {
        return _inputSource.getValidity();
    }
    
    @Override
    public String getMimeType()
    {
        return "text/javascript;charset=utf-8";
    }
}
