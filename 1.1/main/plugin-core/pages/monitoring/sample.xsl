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
    <xsl:param name="sampleName"/>
    
    <xsl:template match="/">
       	<xsl:choose>
       		<xsl:when test="samples/sample[name = $sampleName]">
       			<xsl:for-each select="samples/sample[name = $sampleName]">
			        <html>
			            <head>
			                <title>Monitoring of <xsl:value-of select="name"/></title>
			            </head>
			            <body>
			            	<a href="{name}.xml">Export XML</a>
			            	<br/><br/>
			            	<xsl:variable name="sample" select="."/>
			            	<xsl:for-each select="/samples/periods/period">
			            		<img src="graph/{$sample/name}/{.}.png"/>
				            	<span>&#160;&#160;</span>
				            	<xsl:if test="position() mod 2 = 0">
				            		<br/>
				            	</xsl:if>
			            	</xsl:for-each>
			            </body>
			       	</html>
			     </xsl:for-each>
		    </xsl:when>
       		<xsl:otherwise>
		        <html>
		            <head>
		                <title>Monitoring</title>
		            </head>
		            <body>
		            	No graph for sample <xsl:value-of select="$sampleName"/>.
		        	</body>
		        </html>
       		</xsl:otherwise>
       	</xsl:choose>
    </xsl:template>    
    
</xsl:stylesheet>