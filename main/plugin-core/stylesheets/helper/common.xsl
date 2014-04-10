<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2010 Anyware Services

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
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
			    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
			    xmlns:math="http://exslt.org/math"
			    xmlns:ametys="org.ametys.cms.transformation.xslt.AmetysXSLTHelper"
			    exclude-result-prefixes="math">
    
	<xsl:template name="captcha">
		<xsl:param name="key-name">captcha-key</xsl:param>
		<xsl:param name="key-id" select="concat('id', substring-after(math:random(), '.'))"/>
		
		<xsl:param name="value-name">captcha-value</xsl:param>
		<xsl:param name="value-id" select="concat('id', substring-after(math:random(), '.'))"/>
		<xsl:param name="value-style"/>
		<xsl:param name="value-class"/>
		
		<xsl:param name="image-id" select="concat('id', substring-after(math:random(), '.'))"/>
		<xsl:param name="image-alt"/>
		<xsl:param name="image-alt-i18n" select="false()"/>
		<xsl:param name="image-alt-catalogue"/>
		<xsl:param name="image-style"/>
		<xsl:param name="image-class"/>
		<xsl:param name="image-width">200</xsl:param>
		<xsl:param name="image-height">50</xsl:param>
		
		<xsl:param name="allow-refresh" select="true()"/>
        
        <xsl:param name="js-funcname-torefresh" select="concat('refresh_captcha_', substring-after(math:random(), '.'))"/>
        
        <xsl:param name="contextPath"/>
	
		<noscript>
			<xsl:variable name="id" select="concat('STATIC-', substring-after(math:random(), '.'))"/>
		
			<input type="hidden" name="{$key-name}" id="{$key-id}" value="{$id}"/>
			
			<input type="text" name="{$value-name}" maxlength="6" id="{$value-id}" style="{$value-style}" class="{$value-class}"/>
			
			<img style="{$image-style}" class="{$image-class}" src="{$contextPath}/plugins/core/captcha/{$id}/image.png?width={$image-width}&amp;height={$image-height}" id="{$image-id}">
                <xsl:attribute name="alt">
                    <xsl:if test="$image-alt-i18n and $image-alt-catalogue != ''">
                        <xsl:value-of select="$image-alt-catalogue"/>
                        <xsl:text>:</xsl:text>
                    </xsl:if>
                    <xsl:value-of select="$image-alt"/>
                </xsl:attribute>
				<xsl:if test="$image-alt-i18n"><xsl:attribute name="i18n:attr">alt</xsl:attribute></xsl:if>
			</img>
			
			<div class="image-captcha-help"><i18n:text i18n:key="PLUGINS_CMS_CONTENT_CAPTCHA_REFRESH_NOSCRIPT" i18n:catalogue="plugin.cms"/></div>
		</noscript>
		
		<script type="text/javascript">
			document.write("&lt;input type=\"hidden\" name=\"<xsl:value-of select='$key-name'/>\" id=\"<xsl:value-of select="$key-id"/>\"/&gt;")
			document.write("&lt;input type=\"text\" name=\"<xsl:value-of select='$value-name'/>\" id=\"<xsl:value-of select="$value-id"/>\" style=\"<xsl:value-of select="$value-style"/>\" class=\"<xsl:value-of select="$value-class"/>\"/ autocomplete=\"off\"&gt;")
			document.write("&lt;img style=\"<xsl:value-of select="$image-style"/>\" class=\"<xsl:value-of select="$image-class"/>\" alt=\"<xsl:choose><xsl:when test="$image-alt-i18n"><i18n:text i18n:key="{$image-alt}"><xsl:if test="$image-alt-catalogue != ''"><xsl:attribute name="i18n:catalogue"><xsl:value-of select="$image-alt-catalogue"/></xsl:attribute></xsl:if></i18n:text></xsl:when><xsl:otherwise><xsl:value-of select="$image-alt"/></xsl:otherwise></xsl:choose>\" id=\"<xsl:value-of select="$image-id"/>\" src=\"\"/&gt;");
			<xsl:if test="$allow-refresh = true()">
				document.write("&lt;button type=\"button\" title=\"<i18n:text i18n:key="PLUGINS_CMS_CONTENT_CAPTCHA_REFRESH_ALT" i18n:catalogue="plugin.cms"/>\" class=\"captcha-refresh-btn\" onclick=\"<xsl:value-of select="$js-funcname-torefresh"/>(false); return false;\"&gt;&lt;span&gt;<i18n:text i18n:key="PLUGINS_CMS_CONTENT_CAPTCHA_REFRESH" i18n:catalogue="plugin.cms"/>&lt;/span&gt;&lt;/button&gt;");
			</xsl:if>
			
			function <xsl:value-of select="$js-funcname-torefresh"/>(focusNow)
			{
				var newId = "DYNAMIC-" + new Date().getTime() + (''+Math.random()).replace(/\./g, '');
			
				var oldId = document.getElementById("<xsl:value-of select="$key-id"/>").value;
				document.getElementById("<xsl:value-of select="$key-id"/>").value = newId;
				document.getElementById("<xsl:value-of select="$value-id"/>").value = "";
				document.getElementById("<xsl:value-of select="$image-id"/>").src = "<xsl:value-of select="$contextPath"/>/plugins/core/captcha/" + newId + "/image.png?cancelledKey=" + oldId + "&amp;width=<xsl:value-of select="$image-width"/>" + "&amp;height=<xsl:value-of select="$image-height"/>";
				
				if (focusNow == true)
				{
					try
					{
						document.getElementById("<xsl:value-of select="$value-id"/>").focus();
					}
					catch(e)
					{
					}
				}
			}
		</script>
		<!-- Keep the comment below -->
		<xsl:comment>Those 2 scripts are separated to ensure write is done and flushed before the getElementId occurs</xsl:comment>	
		<script type="text/javascript">
			<xsl:value-of select="$js-funcname-torefresh"/>(false);
		</script>
	</xsl:template>
    
</xsl:stylesheet>
