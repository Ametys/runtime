package org.ametys.runtime.plugins.core;

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
