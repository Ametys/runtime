<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2012 Anyware Services

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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:plugin="http://www.ametys.org/schema/plugin"
                exclude-result-prefixes="i18n plugin">

	<xsl:template match="CMS">
		<xsl:apply-templates select="runtime|comment()"/>
	</xsl:template>
	
	<xsl:template match="comment()">
		<xsl:copy-of select="."/>
	</xsl:template>
	
	<!-- For adding missing components -->
	<xsl:template match="/CMS/runtime/components"> 
		<xsl:copy>
			<xsl:copy-of select="@*"></xsl:copy-of>
		
			<xsl:apply-templates/>
			
			<xsl:for-each select="/CMS/transformations/components/*">
				<xsl:variable name="cmpName" select="name()"/>

				<xsl:choose>
					<xsl:when test="/CMS/runtime/components/*[name() = $cmpName]">
						<!-- nothing -->	
					</xsl:when>
					<xsl:otherwise>
						<xsl:element name="{$cmpName}">
							<xsl:value-of select="."/>
						</xsl:element>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	
	<!-- For changing existing components -->
	<xsl:template match="/CMS/runtime/components/*">
		<xsl:variable name="cmpName" select="name()"/>

		<xsl:choose>	
			<xsl:when test="/CMS/transformations/components/*[name() = $cmpName]">
				<xsl:copy>
					<xsl:copy-of select="@*"></xsl:copy-of>
					<xsl:for-each select="/CMS/transformations/components/*[name() = $cmpName]">
						<xsl:value-of select="."/>
					</xsl:for-each>
				</xsl:copy>			 
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:copy-of select="@*"></xsl:copy-of>
				
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- ReActivate extensions -->
	<xsl:template match="/CMS/runtime/plugins/exclude/feature[. = /CMS/transformations/features/feature[. = 'true']/@name]">
		<!-- Nothing -->
	</xsl:template>
	
	<!-- Deactivate extensions -->
	<xsl:template match="/CMS/runtime/plugins/exclude">
		<xsl:copy>
			<xsl:copy-of select="@*"></xsl:copy-of>

			<xsl:apply-templates/>

            <xsl:call-template name="excluding"/>			
		</xsl:copy>
	</xsl:template>
    <xsl:template match="/CMS/runtime/plugins[not(exclude)]">
        <xsl:copy>
            <xsl:copy-of select="@*"></xsl:copy-of>

            <xsl:apply-templates/>

	        <exclude>
	            <xsl:call-template name="excluding"/>           
	        </exclude>
	    </xsl:copy>
    </xsl:template>
    	
	<xsl:template name="excluding">
           <xsl:for-each select="/CMS/transformations/features/feature[. = 'false']">
               <xsl:variable name="fName" select="@name"/>
               <xsl:choose>
                   <xsl:when test="/CMS/runtime/plugins/exclude/feature[. = $fName]">
                       <!-- Already deactivated -->
                   </xsl:when>
                   <xsl:otherwise>
                       <feature><xsl:value-of select="@name"/></feature>
                   </xsl:otherwise>
               </xsl:choose>
           </xsl:for-each>
	</xsl:template>
	
	<!-- For copying -->
	<xsl:template match="*">
		<xsl:copy>
			<xsl:copy-of select="@*"></xsl:copy-of>
		
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>