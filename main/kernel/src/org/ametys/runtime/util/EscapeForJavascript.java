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
package org.ametys.runtime.util;

/**
 * Helper
 *
 */
public final class EscapeForJavascript
{
    private EscapeForJavascript ()
    {
        // empty
    }
    
    /**
     * Escape a String to used it in javascript
     * @param xmlString The String to escape
     * @return The escaped String
     */
    public static String escape (String xmlString)
    {
        String escapedStr = xmlString;
        escapedStr = escapedStr.replaceAll("\"", "\\\\\"");
        escapedStr = escapedStr.replaceAll("\'", "\\\\\'");
        escapedStr = escapedStr.replaceAll("&", "&amp;");
        escapedStr = escapedStr.replaceAll("\\\\", "\\\\\\\\");
        escapedStr = escapedStr.replaceAll("<", "&lt;");
        escapedStr = escapedStr.replaceAll(">", "&gt;");
        escapedStr = escapedStr.replaceAll("\r", "\\\\\r");
        escapedStr = escapedStr.replaceAll("\n", "\\\\\n");
        
        return escapedStr;
    }
}
