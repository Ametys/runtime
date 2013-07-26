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