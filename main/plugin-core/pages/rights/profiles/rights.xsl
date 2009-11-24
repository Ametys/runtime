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
      
	<xsl:template match="rights">
    	<rights>
    		<xsl:for-each select="right[not(category/@id = preceding-sibling::right/category/@id)]">
				<xsl:variable name="category" select="category/@id"/>
				<xsl:variable name="categoryKey" select="category"/>

				<category id="{$category}">
					<label><xsl:copy-of select="$categoryKey/*"/></label>
					
					<xsl:for-each select="../right[category/@id = $category]">
						<right id="{@id}">
							<xsl:copy-of select="label"/>
							<xsl:copy-of select="description"/>
							<xsl:copy-of select="$categoryKey"/>
						</right>
					</xsl:for-each>
				</category>		
			</xsl:for-each>
    	</rights>
    </xsl:template>          

</xsl:stylesheet>