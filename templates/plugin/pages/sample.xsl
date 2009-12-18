<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2009 Anyware Services

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   -->
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
                <script src="{$contextPath}/plugins/{$pluginName}/resources/js/sample.i18n.js">// empty</script>
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