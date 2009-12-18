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