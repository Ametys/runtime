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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    
    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>
    
    <xsl:template match="/">
        <html>
            <head>
                <title>Monitoring</title>
                <style>
                	a img {border-width: 0}
                </style>
            </head>
            <body>
                <table>
                <xsl:for-each select="samples/sample">
                	<tr>
                		<td>
                			:: <xsl:value-of select="name"/><br/>
                			<a href="monitoring/{name}.html" title="Details">
                				<img src="monitoring/graph/{name}/hour.png" />
                			</a>
                			<span>&#160;&#160;</span>
                			<a href="monitoring/{name}.html" title="Details">
                				<img src="monitoring/graph/{name}/day.png"/>
                			</a>
                		</td>
                	</tr>
                </xsl:for-each>
               	</table>
            </body>
        </html>
    </xsl:template>    
    
</xsl:stylesheet>