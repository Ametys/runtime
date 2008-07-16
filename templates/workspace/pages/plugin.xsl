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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
        
    <xsl:import href="template.xsl"/>
        
	<xsl:template name="workspace-title"><xsl:copy-of select="/Workspace/html/head/title/node()"/></xsl:template>

    <xsl:template name="workspace-head"><xsl:copy-of select="/Workspace/html/head/*[local-name(.) != 'title']"/></xsl:template>

    <xsl:template name="workspace-body"><xsl:copy-of select="/Workspace/html/body/node()"/></xsl:template>
        
	<xsl:template name="workspace-script"><xsl:copy-of select="/Workspace/html/script/node()"/></xsl:template>
        
</xsl:stylesheet>