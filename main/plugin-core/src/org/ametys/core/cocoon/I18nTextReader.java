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
                            if (getLogger().isWarnEnabled())
                            {
                                getLogger().warn("Invalid i18n declaration in the file  '" + _sourceURI + "': '{{i18n' must be followed by a backspace.");
                            }
                            
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
                else if (c == '\\' && i < srcLength && srcChars[i + 1] == '{')
                {
                    _append(outStringBuilder, srcChars, i - offset, i);
                    outStringBuilder.append('{');

                    offset = 0;
                    skip++;
                }
                else
                {
                    offset++;
                }
            }
            
            // Copy the last characters
            if (offset > 0)
            {
                _append(outStringBuilder, srcChars, srcLength - offset, srcLength);
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
        int beginLength = __I18N_BEGINNING_CHARS.length; // "{{i18n"
        int keyBeginningIndex = candidateBeginIdx + beginLength + 1; 
        boolean invalid = false;
        boolean valid = false;
        int srcLength = srcChars.length;
        int j = keyBeginningIndex;
        int offset = beginLength;
        _isDeclarationValid = false;
        
        while (j < srcLength && !invalid && !valid)
        {
            char c = srcChars[j];
            offset++;
            switch (c)
            {
                case '{':
                    if (srcChars[j + 1] == '{')
                    {                        
                        if (getLogger().isWarnEnabled())
                        {
                            getLogger().warn("Invalid i18n declaration in the file  '" + _sourceURI + "': '{{' within an i18n declaration is forbidden.");
                        }
                        offset++;
                        invalid = true;
                    }
                    break;  
                    
                case '}':
                    if (srcChars[j + 1] == '}')
                    {
                        offset++;
                        if (j == keyBeginningIndex)
                        {
                            if (getLogger().isWarnEnabled())
                            {
                                getLogger().warn("Invalid i18n declaration in the file  '" + _sourceURI + "': a key must be specified.");
                            }
                            invalid = true;
                        }
                        else
                        {
                            _isDeclarationValid = true;
                            valid = true;
                        }
                    }
                    break;
                    
                case '\n':
                    
                    if (getLogger().isWarnEnabled())
                    {
                        getLogger().warn("Invalid i18n declaration in the file  '" + _sourceURI + "': '\\n' within an i18n declaration is forbidden. Make sure all i18n declarations are closed with the sequence '}}'.");
                    }
                    offset++;
                    invalid = true;
                    break;
                    
                default:
                    break;
            }
            
            j++;
        }
        
        return _computeOutputAndSkip(srcChars, sb, candidateBeginIdx, invalid, valid, j + 1, offset, initialOffset);
    }

    /**
     * Write the output stream with the provided information
     * @param srcChars the input source as characters
     * @param sb the string builder where to write
     * @param candidateBeginIdx the index at which the i18n declaration started
     * @param invalid true if the declaration is invalid, false otherwise
     * @param valid true if the declaration is valid, false otherwise
     * @param lastIdx the last index analyzed
     * @param offset the amount of analyzed characters after the valid i18n declaration beginning
     * @param initialOffset the amount of characters that we have to write before the i18n declaration
     * @return the amount of characters to skip
     */
    private int _computeOutputAndSkip(char[] srcChars, StringBuilder sb, int candidateBeginIdx, boolean invalid, boolean valid, int lastIdx, int offset, int initialOffset)
    {
        int srcLength = srcChars.length;
        int beginningLength = __I18N_BEGINNING_CHARS.length; // "{{i18n"
        int keyBeginningIndex = candidateBeginIdx + beginningLength + 1; 
        
        if (valid)
        {
            // Proper i18n declaration, write the 'offset' characters that are just copied 
            _append(sb, srcChars, candidateBeginIdx - initialOffset + 1,  candidateBeginIdx);
            
            // extract the key, translate it and replace the i18n declaration with its translation
            sb.append(_getTranslation(srcChars, keyBeginningIndex, lastIdx - 2)); // "}}"
        }
        
        if (lastIdx == srcLength + 1 && !valid && !invalid)
        {
            // We've reached the end of the file without encountering the closing sequence '}}', and the declaration has not been found valid
            // nor invalid yet
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Invalid i18n declaration in the file  '" + _sourceURI + "': Reached end of the file without finding the closing sequence of an i18n declaration.");
            }
            
            return lastIdx - 1 - candidateBeginIdx;
        }
        
        return lastIdx - candidateBeginIdx;
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
        
        System.arraycopy(srcChars, beginIndex, keyAsChars, 0, keyLength);

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
        
        // Attempt to translate and return either the translation or the i18n declaration
        String translation = _i18nUtils.translate(new I18nizableText(catalogue, key.trim()), _locale);
        if (translation == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Translation not found for key '" + key + "' in catalogue '" + catalogue + "'.");
            }
        }
        
        if (translation != null)
        {
            return translation;
        }
        else
        {
            char[] rawI18nDeclaration = new char[7 + keyLength + 2];
            System.arraycopy(srcChars, beginIndex - 7, rawI18nDeclaration, 0, 7 + keyLength + 2);
            return String.valueOf(rawI18nDeclaration);
        }
    }
    
    /**
     * Append to the given string builder the characters from the given array in the selected range 
     * @param sb the string builder to write in
     * @param chars the characters' array
     * @param beginIdx the begin index
     * @param endIdx the end index
     */
    private void _append(StringBuilder sb, char[] chars, int beginIdx, int endIdx)
    {
        for (int i = beginIdx; i < endIdx; i++)
        {
            sb.append(chars[i]);
        }
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
