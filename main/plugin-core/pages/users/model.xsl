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
    
    <xsl:template match="/">
    	<xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="label">
    	<label><i18n:text i18n:key="{.}"/></label>
    </xsl:template>
    
    <xsl:template match="description">
    	<description><i18n:text i18n:key="{.}"/></description>
    </xsl:template>
    
    <xsl:template match="*">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>