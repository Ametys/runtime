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
    		<xsl:for-each select="right[not(category = preceding-sibling::right/category)]">
				<xsl:variable name="category" select="category"/>
					<category id="{category}">
						<label><i18n:text i18n:key="{category}" i18n:catalogue="{@catalogue}"/></label>
						
						<xsl:for-each select="../right[category = $category]">
							<right id="{@id}">
								<label><i18n:text i18n:key="{label}" i18n:catalogue="{@catalogue}"/></label>
								<description><i18n:text i18n:key="{description}" i18n:catalogue="{@catalogue}"/></description>
								<category><xsl:value-of select="category"/></category>
							</right>
						</xsl:for-each>
					</category>		
				
			</xsl:for-each>
    	</rights>
    </xsl:template>          

</xsl:stylesheet>