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

    <xsl:template name="plugins-load">
        <xsl:param name="scripts"/>
        <xsl:param name="actions"/>

        <xsl:for-each select="$scripts">
            <xsl:variable name="position" select="position()"/>
            <xsl:variable name="value" select="."/>
            
            <!-- check that the src was not already loaded (by another plugin for example) -->
            <xsl:if test="not($scripts[position() &lt; $position and . = $value])">
                <script src="{$contextPath}{.}"><xsl:comment>empty</xsl:comment></script>
            </xsl:if>
        </xsl:for-each>


        <!-- Initialize actions -->
        <script>
            <xsl:for-each select="$actions">
                <xsl:variable name="position" select="position()"/>
                <xsl:variable name="value" select="."/>
                
                <!-- check that the action was not already initialized (by another plugin for example) -->
                <xsl:if test="not($actions[position() &lt; $position and . = $value])">
                    if (typeof <xsl:value-of select="."/>.initialize == "function") { <xsl:value-of select="."/>.initialize("<xsl:value-of select="../@plugin"/>"); }
                    <xsl:text>
                    </xsl:text>
                </xsl:if>
            </xsl:for-each>
        </script>
    </xsl:template>
    
</xsl:stylesheet>