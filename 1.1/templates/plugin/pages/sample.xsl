<?xml version="1.0" encoding="UTF-8"?>
<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    <xsl:param name="workspaceName"/>
    <xsl:param name="workspaceURI"/>

    <xsl:template match="/sample">
        <html>
            <head>
                <link type="text/css" href="{$contextPath}/plugins/{$pluginName}/resources/css/sample.css" rel="stylesheet"/>
                <script src="{$contextPath}/plugins/{$pluginName}/resources/js/sample.js">// empty</script>
                <script src="{$contextPath}/plugins/{$pluginName}/resources/js/sample.js.i18n">// empty</script>
            </head>
            <body>
                <img src="{$contextPath}/plugins/{$pluginName}/resources/img/sample.jpg" width="400px" height="50px"/>
                <table border="1">
                    <colgroup>
                        <col width="100px"/>
                        <col width="40px"/>
                    </colgroup>
                    <thead>
                        <tr>
                            <th><i18n:text i18n:key="PLUGINS_PLUGIN_COLNAME"/></th>
                            <th><i18n:text i18n:key="PLUGINS_PLUGIN_COLAGE"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><xsl:value-of select="Author/Name"/></td>
                            <td><xsl:value-of select="Author/Age"/></td>
                        </tr>
                    </tbody>
                </table>
                <button onclick="myFunction(); return false;"><i18n:text i18n:key="PLUGINS_PLUGIN_BUTTON"/></button>
                &#160;
                <button onclick="myI18nFunction(); return false;"><i18n:text i18n:key="PLUGINS_PLUGIN_BUTTON"/></button>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>