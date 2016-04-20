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
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.ServiceableReader;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import org.ametys.core.util.AvalonLoggerAdapter;
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
    
    /** The beginning of a valid declaration for an internationalizable text as characters */
    private static final char[] __I18N_BEGINNING_CHARS = {'{', '{', 'i', '1', '8', 'n'};
    
    /** Avalon component gathering utility methods concerning {@link I18nizableText}, allowing their translation in several languages */
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
    
    /** The URI of the input source */
    private String _sourceURI; 
    
    /** Is the last analyzed i18n declaration valid ? */
    private boolean _isDeclarationValid;
    
    /** The SL4J logger */
    private Logger _logger;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _i18nUtils = (I18nUtils) serviceManager.lookup(I18nUtils.ROLE);
    }
    
    @Override
    public void setup(SourceResolver initalResolver, Map cocoonObjectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException
    {
        _logger = new AvalonLoggerAdapter(getLogger());
        
        try 
        {
            _inputSource = initalResolver.resolveURI(src);
        } 
        catch (SourceException e) 
        {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", e);
        }
        
        super.setup(initalResolver, cocoonObjectModel, src, par);
        
        _sourceURI = _inputSource.getURI();
        
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
        
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        if (params != null)
        {
            params.put(RuntimeResourceReader.LAST_MODIFIED, _inputSource.getLastModified());
        }
    }
    
    @Override
    public void generate() throws IOException, ProcessingException
    {
        if (!_inputSource.exists())
        {
            throw new ResourceNotFoundException("Resource not found for URI : " + _sourceURI);
        }
        
        try (InputStream is = _inputSource.getInputStream())
        {
            StringBuilder outStringBuilder = new StringBuilder (); // Use a StringBuilder to dynamically allocate memory
            
            int beginLength = __I18N_BEGINNING_CHARS.length;
            int endLength = 2; // "}}"
            int minI18nDeclarationLength = beginLength + 1 + 1 + endLength; // 1 mandatory backspace and at least 1 character for the key
            
            char[] srcChars = IOUtils.toCharArray(is, "UTF-8");
            int srcLength = srcChars.length;
            
            int skip = 0; // Avoid checkstyle warning : "Control variable 'i' is modified"
            int offset = 0;  // Amount of characters to be copied between two valid i18n declarations
            for (int i = 0; i < srcLength; i = i + skip)
            {
                skip = 1;
                char c = srcChars[i];
                
                // Do not bother analyzing when there is no room for a valid declaration
                if (c == '{' && i + minI18nDeclarationLength < srcLength)
                {
                    offset++;
                    char[] candidateBeginning = {c, srcChars[i + 1], srcChars[i + 2], srcChars[i + 3], srcChars[i + 4], srcChars[i + 5]};
                    if (Arrays.equals(candidateBeginning, __I18N_BEGINNING_CHARS))
                    {
                        // Keep the analyzed characters to put them in the output stream 
                        // in case we know the character sequence won't make a viable candidate
                        char backspaceCandidate = srcChars[i + beginLength];
                        if (backspaceCandidate != ' ')
                        {
                            _logger.warn("Invalid i18n declaration in the file  '{}': '{{i18n' must be followed by a backspace.", _sourceURI);
                            
                            // Update the amount of skipped characters for the next iteration
                            skip += beginLength;
                            
                            // Update the offset
                            offset += beginLength;
                            
                            continue;
                        }
                        
                        // Valid candidate so far, check the end of the notation
                        skip = _analyzeI18nDeclaration(srcChars, i, outStringBuilder, offset);
                        
                        // Reset the offset only when the declaration is valid (i.e. when a string from the input has been replaced by another)
                        offset = _isDeclarationValid ? 0 : offset + skip - 1;
                    }
                }
                // Escape '{'
                else if (c == '\\' && i + 1 != srcLength && srcChars[i + 1] == '{')
                {
                    outStringBuilder.append(srcChars, i - offset, offset);
                    outStringBuilder.append('{');

                    offset = 0;
                    skip++;
                }
                else
                {
                    offset++;
                }
            }
            
            // No i18n declarations to be found ! Simply copy srcChars to the output stream
            if (offset == srcLength)
            {
                IOUtils.write(srcChars, out, "UTF-8");
                return;
            }
            
            // Copy the last characters
            if (offset > 0)
            {
                outStringBuilder.append(srcChars, srcLength - offset, offset);
            }
            
            IOUtils.write(outStringBuilder, out, "UTF-8");
        }
        finally
        {
            out.flush();
        }
    }

    /**
     * Analyze characters from the key beginning index to the possible closure sequence '}}',
     * and write the appropriate replacement in the output string builder
     * @param srcChars the input file as characters
     * @param candidateBeginIdx the index at which we started analyzing a viable i18n declaration
     * @param sb the string builder where we store the output string
     * @param initialOffset the initial offset 
     * @return the amount of analyzed characters
     */
    private int _analyzeI18nDeclaration(char[] srcChars, int candidateBeginIdx, StringBuilder sb, int initialOffset)
    {
        _isDeclarationValid = false;

        int beginLength = __I18N_BEGINNING_CHARS.length; // "{{i18n"
        int keyBeginningIndex = candidateBeginIdx + beginLength + 1; // "...........{{i18n "
        int srcLength = srcChars.length;
        
        boolean invalid = false;
        boolean valid = false;
        
        int j = keyBeginningIndex;
        while (j < srcLength && !invalid && !valid)
        {
            char c = srcChars[j];
            switch (c)
            {
                case '{':
                    if (j + 1 != srcLength && srcChars[j + 1] == '{')
                    {                        
                        _logger.warn("Invalid i18n declaration in the file '{}': '{{' within an i18n declaration is forbidden.", _sourceURI);
                        invalid = true;
                    }
                    break;  
                    
                case '}':
                    if (j + 1 != srcLength && srcChars[j + 1] == '}')
                    {
                        if (j == keyBeginningIndex)
                        {
                            _logger.warn("Invalid i18n declaration in the file  '{}': a key must be specified.", _sourceURI);
                            invalid = true;
                            break;
                        }
                        else
                        {
                            _isDeclarationValid = true;
                            valid = true;
                        }
                    }
                    break;
                    
                case '\n':
                    
                    _logger.warn("Invalid i18n declaration in the file  '{}': '\\n' within an i18n declaration is forbidden. Make sure all i18n declarations are closed with the sequence '}}'.", _sourceURI);
                    invalid = true;
                    break;
                    
                default:
                    break;
            }
            
            j++;
        }
        
        if (!valid && !invalid)
        {
            // We've reached the end of the file without encountering the closing sequence '}}', and the declaration has not been found valid
            // nor invalid yet
            _logger.warn("Invalid i18n declaration in the file  '{}': Reached end of the file without finding the closing sequence of an i18n declaration.", _sourceURI);
            return j - candidateBeginIdx;
        }

        if (valid)
        {
            // try to replace the key with its translation
            _translateKey(srcChars, sb, candidateBeginIdx, j, initialOffset);
        }
            
        return j - candidateBeginIdx + 1;
    }

    /**
     * Try to translate the key and write the output stream with its translation if found, the key itself if not
     * @param srcChars the input source as characters
     * @param sb the string builder where to write
     * @param candidateBeginIdx the index at which the i18n declaration started
     * @param lastIdx the last index analyzed
     * @param initialOffset the amount of characters that we have to write before the i18n declaration
     */
    private void _translateKey(char[] srcChars, StringBuilder sb, int candidateBeginIdx, int lastIdx, int initialOffset)
    {
        int keyBeginningIndex = candidateBeginIdx + __I18N_BEGINNING_CHARS.length + 1; // "...........{{i18n "
        
        // Proper i18n declaration, write the 'offset' characters that are just copied 
        sb.append(srcChars, candidateBeginIdx - initialOffset + 1, initialOffset - 1);
        
        // Extract the key and the catalogue
        int keyLength = lastIdx - 1 - keyBeginningIndex;
        String key = String.valueOf(srcChars, keyBeginningIndex, keyLength);
        
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
        
        // Attempt to translate 
        String translation = _i18nUtils.translate(new I18nizableText(catalogue, key.trim()), _locale);
        if (translation == null)
        {
            _logger.warn("Translation not found for key '{}' in catalogue '{}'.", key, catalogue);
        }
        
        if (translation == null)
        {
            char[] rawI18nDeclaration = new char[7 + keyLength + 2]; // "{{i18n "  + "KEY" + "}}"
            System.arraycopy(srcChars, keyBeginningIndex - 7, rawI18nDeclaration, 0, 7 + keyLength + 2);
            translation = String.valueOf(rawI18nDeclaration);
        }
        
        // replace the i18n declaration with its translation (can be the key itself if no translation found)
        sb.append(translation);
    }
    
    @Override
    public Serializable getKey()
    {
        return _sourceURI + "*" + _locale;
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
