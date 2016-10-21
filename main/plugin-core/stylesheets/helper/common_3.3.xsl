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
			    xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper"
                xmlns:captcha="org.ametys.core.captcha.CaptchaHelper"
			    exclude-result-prefixes="math captcha">
    
	<xsl:template name="captcha">
		<xsl:param name="key-name">captcha-key</xsl:param>
		<xsl:param name="key-id" select="concat('id', substring-after(math:random(), '.'))"/>

		<xsl:param name="color">#000000</xsl:param>
		
		<xsl:param name="value-name">captcha-value</xsl:param>
		<xsl:param name="value-id" select="concat('id', substring-after(math:random(), '.'))"/>
		<xsl:param name="value-style"/>
		<xsl:param name="value-class"/>
		
        <xsl:param name="placeholder"/>
        <xsl:param name="placeholder-i18n"/>
        <xsl:param name="placeholder-catalogue"/>
        
		<xsl:param name="image-id" select="concat('id', substring-after(math:random(), '.'))"/>
		<xsl:param name="image-alt"/>
		<xsl:param name="image-alt-i18n" select="false()"/>
		<xsl:param name="image-alt-catalogue"/>
		<xsl:param name="image-style"/>
		<xsl:param name="image-class"/>
		<xsl:param name="image-width">200</xsl:param>
		<xsl:param name="image-height">50</xsl:param>
		
        <xsl:param name="recaptcha-theme">light</xsl:param>
        <xsl:param name="recaptcha-size">normal</xsl:param>
        
		<xsl:param name="allow-refresh" select="true()"/>
		
		<xsl:param name="js-funcname-torefresh" select="concat('refresh_captcha_', substring-after(math:random(), '.'))"/>

        <xsl:param name="plugin" select="'core'"/>
        
        <xsl:variable name ="captcha-type"><xsl:value-of select="ametys:config('runtime.captcha.type')" /></xsl:variable>
        
        <xsl:if test="$captcha-type = 'jcaptcha'">
            
            <xsl:call-template name="captcha_impl">
                <xsl:with-param name="key-name" select="$key-name"/>
                <xsl:with-param name="key-id" select="$key-id"/>
                <xsl:with-param name="color" select="$color"/>
                <xsl:with-param name="value-name" select="$value-name"/>
                <xsl:with-param name="value-id" select="$value-id"/>
                <xsl:with-param name="value-style" select="$value-style"/>
                <xsl:with-param name="value-class" select="$value-class"/>
                <xsl:with-param name="placeholder" select="$placeholder"/>
                <xsl:with-param name="placeholder-i18n" select="$placeholder-i18n"/>
                <xsl:with-param name="placeholder-catalogue" select="$placeholder-catalogue"/>
                <xsl:with-param name="image-id" select="$image-id"/>
                <xsl:with-param name="image-alt" select="$image-alt"/>
                <xsl:with-param name="image-alt-i18n" select="$image-alt-i18n"/>
                <xsl:with-param name="image-alt-catalogue" select="$image-alt-catalogue"/>
                <xsl:with-param name="image-style" select="$image-style"/>
                <xsl:with-param name="image-class" select="$image-class"/>
                <xsl:with-param name="image-width" select="$image-width"/>
                <xsl:with-param name="image-height" select="$image-height"/>
                <xsl:with-param name="allow-refresh" select="$allow-refresh"/>
                <xsl:with-param name="js-funcname-torefresh" select="$js-funcname-torefresh"/>
                <xsl:with-param name="plugin" select="$plugin"/>
            </xsl:call-template>
            
        </xsl:if>
        
        <xsl:if test="$captcha-type = 'recaptcha'">
            <xsl:call-template name="recaptcha_impl">
                <xsl:with-param name="key-name" select="$key-name"/>
                <xsl:with-param name="key-id" select="$key-id"/>
                <xsl:with-param name="value-name" select="$value-name"/>
                <xsl:with-param name="value-id" select="$value-id"/>
                <xsl:with-param name="recaptcha-theme" select="$recaptcha-theme"/>
                <xsl:with-param name="recaptcha-size" select="$recaptcha-size"/>
                <xsl:with-param name="js-funcname-torefresh" select="$js-funcname-torefresh"/>
            </xsl:call-template>
        </xsl:if>
	</xsl:template>
	
	<xsl:template name="captcha_impl">
        <xsl:param name="key-name"/>
        <xsl:param name="key-id"/>
        <xsl:param name="color"/>
        <xsl:param name="value-name"/>
        <xsl:param name="value-id"/>
        <xsl:param name="value-style"/>
        <xsl:param name="value-class"/>
        <xsl:param name="placeholder"/>
        <xsl:param name="placeholder-i18n"/>
        <xsl:param name="placeholder-catalogue"/>
        <xsl:param name="image-id"/>
        <xsl:param name="image-alt"/>
        <xsl:param name="image-alt-i18n"/>
        <xsl:param name="image-alt-catalogue"/>
        <xsl:param name="image-style"/>
        <xsl:param name="image-class"/>
        <xsl:param name="image-width"/>
        <xsl:param name="image-height"/>
        <xsl:param name="allow-refresh"/>
        <xsl:param name="js-funcname-torefresh"/>
        <xsl:param name="plugin"/>
        
        <noscript>
            <xsl:variable name="id" select="concat('STATIC-', substring-after(math:random(), '.'))"/>
            
            <div>
                <input type="hidden" name="{$key-name}" id="{$key-id}" value="{$id}"/>
            
                <input type="text" name="{$value-name}" maxlength="6" id="{$value-id}" style="{$value-style}" class="{$value-class}">
                    <xsl:if test="$placeholder != ''">
                        <xsl:attribute name="placeholder">
                            <xsl:if test="$placeholder-i18n and $placeholder-catalogue != ''">
                                <xsl:value-of select="$placeholder-catalogue"/>
                                <xsl:text>:</xsl:text>
                            </xsl:if>
                            <xsl:value-of select="$placeholder"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$placeholder-i18n"><xsl:attribute name="i18n:attr">placeholder</xsl:attribute></xsl:if>
                </input>

                <div class="captcha">
                    <img style="{$image-style}" class="{$image-class}" src="{$uri-prefix}/plugins/{$plugin}/captcha/{$id}/image.png?width={$image-width}&amp;height={$image-height}&amp;color={$color}" id="{$image-id}">
                        <xsl:attribute name="alt">
                            <xsl:if test="$image-alt-i18n and $image-alt-catalogue != ''">
                                <xsl:value-of select="$image-alt-catalogue"/>
                                <xsl:text>:</xsl:text>
                            </xsl:if>
                            <xsl:value-of select="$image-alt"/>
                        </xsl:attribute>
                        <xsl:if test="$image-alt-i18n"><xsl:attribute name="i18n:attr">alt</xsl:attribute></xsl:if>
                    </img>
                
                    <div class="image-captcha-help"><i18n:text i18n:key="PLUGINS_CORE_CAPTCHA_REFRESH_NOSCRIPT" i18n:catalogue="plugin.core"/></div>
                    <div class="captcha-end"></div>
                </div>
            </div>
        </noscript>
        
        <script type="text/javascript">
            document.write("&lt;input type=\"hidden\" name=\"<xsl:value-of select='$key-name'/>\" id=\"<xsl:value-of select="$key-id"/>\"/&gt;")
            document.write("&lt;input type=\"text\" name=\"<xsl:value-of select='$value-name'/>\" id=\"<xsl:value-of select="$value-id"/>\" <xsl:if test="$placeholder != ''">placeholder=\"<xsl:choose><xsl:when test="$placeholder-i18n"><i18n:text i18n:key="{$placeholder}"><xsl:if test="$placeholder-catalogue != ''"><xsl:attribute name="i18n:catalogue"><xsl:value-of select="$placeholder-catalogue"/></xsl:attribute></xsl:if></i18n:text></xsl:when><xsl:otherwise><xsl:value-of select="$placeholder"/></xsl:otherwise></xsl:choose>\" </xsl:if>style=\"<xsl:value-of select="$value-style"/>\" class=\"<xsl:value-of select="$value-class"/>\"/ autocomplete=\"off\"&gt;")
            document.write("&lt;div class='captcha'&gt;&lt;img style=\"<xsl:value-of select="$image-style"/>\" class=\"<xsl:value-of select="$image-class"/>\" alt=\"<xsl:choose><xsl:when test="$image-alt-i18n"><i18n:text i18n:key="{$image-alt}"><xsl:if test="$image-alt-catalogue != ''"><xsl:attribute name="i18n:catalogue"><xsl:value-of select="$image-alt-catalogue"/></xsl:attribute></xsl:if></i18n:text></xsl:when><xsl:otherwise><xsl:value-of select="$image-alt"/></xsl:otherwise></xsl:choose>\" id=\"<xsl:value-of select="$image-id"/>\" src=\"\"/&gt;<xsl:if test="$allow-refresh = true()">&lt;button type=\"button\" title=\"<i18n:text i18n:key="PLUGINS_CORE_CAPTCHA_REFRESH_ALT" i18n:catalogue="plugin.core"/>\" class=\"captcha-refresh-btn\" onclick=\"<xsl:value-of select="$js-funcname-torefresh"/>(false); return false;\"&gt;&lt;span&gt;<i18n:text i18n:key="PLUGINS_CORE_CAPTCHA_REFRESH" i18n:catalogue="plugin.core"/>&lt;/span&gt;&lt;/button&gt;</xsl:if>&lt;div class='captcha-end'&gt;&lt;/div&gt;&lt;/div&gt;");
            
            function <xsl:value-of select="$js-funcname-torefresh"/>(focusNow)
            {
                var newId = "DYNAMIC-" + new Date().getTime() + (''+Math.random()).replace(/\./g, '');
            
                var oldId = document.getElementById("<xsl:value-of select="$key-id"/>").value;
                document.getElementById("<xsl:value-of select="$key-id"/>").value = newId;
                document.getElementById("<xsl:value-of select="$value-id"/>").value = "";
                document.getElementById("<xsl:value-of select="$image-id"/>").src = "<xsl:value-of select="$uri-prefix"/>/plugins/<xsl:value-of select="$plugin"/>/captcha/" + newId + "/image.png?cancelledKey=" + oldId + "&amp;width=<xsl:value-of select="$image-width"/>" + "&amp;height=<xsl:value-of select="$image-height"/>" + "&amp;color=<xsl:value-of select="$color"/>";
                
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
        <!-- Keep the bellow comment -->
        <xsl:comment>Those 2 scripts are separated to ensure write is done and flushed before the getElementId occures</xsl:comment>    
        <script type="text/javascript">
            <xsl:value-of select="$js-funcname-torefresh"/>(false);
        </script>
    </xsl:template>

    <xsl:template name="recaptcha_impl">
        <xsl:param name="key-name"/>
        <xsl:param name="key-id"/>
        <xsl:param name="value-name"/>
        <xsl:param name="value-id"/>
        <xsl:param name="recaptcha-theme"/>
        <xsl:param name="recaptcha-size"/>
        <xsl:param name="js-funcname-torefresh"/>
        
        <xsl:variable name="captcha-id"><xsl:value-of select="concat('captcha', substring-after(math:random(), '.'))"/></xsl:variable>
        <xsl:variable name="public-key"><xsl:value-of select="ametys:config('runtime.captcha.recaptcha.publickey')" /></xsl:variable>
        
        <noscript>
          <div style="width: 302px; height: 462px;">
            <div style="width: 302px; height: 422px; position: relative;">
              <div style="width: 302px; height: 422px;">
                <iframe src="https://www.google.com/recaptcha/api/fallback?k={$public-key}"
                        frameborder="0" scrolling="no"
                        style="width: 302px; height:422px; border-style: none;">
                </iframe>
              </div>
              <div style="width: 300px; height: 60px; border-style: none;
                          bottom: 12px; left: 25px; margin: 0px; padding: 0px; right: 25px;
                          background: #f9f9f9; border: 1px solid #c1c1c1; border-radius: 3px;">
                <textarea id="g-recaptcha-response" name="{$value-name}"
                          class="g-recaptcha-response"
                          style="width: 250px; height: 40px; border: 1px solid #c1c1c1;
                                 margin: 10px 25px; padding: 0px; resize: none;" >
                </textarea>
                <input type="hidden" name="{$key-name}" />
              </div>
            </div>
          </div>
        </noscript>
        
        <script type="text/javascript">
            document.write("&lt;input type=\"hidden\" name=\"<xsl:value-of select='$key-name'/>\" id=\"<xsl:value-of select="$key-id"/>\"/&gt;")
            document.write("&lt;input type=\"hidden\" name=\"<xsl:value-of select='$value-name'/>\" id=\"<xsl:value-of select="$value-id"/>\"/&gt;")
            document.write("&lt;div class=\"g-recaptcha\" id=\"<xsl:value-of select='$captcha-id'/>\"&gt;&lt;/div&gt;")    
            function loadValueToSend_<xsl:value-of select="$captcha-id"/>(value) {
                document.getElementById("<xsl:value-of select="$value-id"/>").value = value;
            }
            
            function ReCaptchaCallback_<xsl:value-of select='$captcha-id'/> ()
            {
                recaptchaLoaded = true;
                for (var i = 0; i &lt; recaptchaFieldsToLoad.length; i++)
                {
                    grecaptcha.render(recaptchaFieldsToLoad[i].captchaId, {
                        'sitekey' : '<xsl:value-of select='$public-key'/>',
                        'callback' : recaptchaFieldsToLoad[i].callback, 
                        'theme' : recaptchaFieldsToLoad[i].theme,
                        'size' : recaptchaFieldsToLoad[i].size
                    });
                }
            }
            
            if (typeof recaptchaLoaded == 'undefined')
            {
                // The recaptcha script was never loaded
                recaptchaLoaded = false;
                recaptchaFieldsToLoad = [{
                    'captchaId' : "<xsl:value-of select='$captcha-id'/>",
                    'callback' : loadValueToSend_<xsl:value-of select='$captcha-id'/> ,
                    'theme' : '<xsl:value-of select="$recaptcha-theme" />',
                    'size' : '<xsl:value-of select="$recaptcha-size" />',
                }];
                document.write("&lt;script src=\"https://www.google.com/recaptcha/api.js?onload=ReCaptchaCallback_<xsl:value-of select='$captcha-id'/>&amp;render=explicit\" async defer&gt;&lt;/script&gt;");
            }
            else if (recaptchaLoaded == false)
            {
                // The recaptcha script is loading but not ready.
                recaptchaFieldsToLoad.push({
                    'captchaId' : "<xsl:value-of select='$captcha-id'/>",
                    'callback' : loadValueToSend_<xsl:value-of select='$captcha-id'/> ,
                    'theme' : '<xsl:value-of select="$recaptcha-theme" />',
                    'size' : '<xsl:value-of select="$recaptcha-size" />'
                })
            }
            else
            {
                // the recaptcha script is loaded and ready
                grecaptcha.render("<xsl:value-of select='$captcha-id'/>", {
                    'sitekey' : '<xsl:value-of select='$public-key'/>',
                    'callback' : loadValueToSend_<xsl:value-of select='$captcha-id'/>,
                    'theme' : '<xsl:value-of select="$recaptcha-theme" />',
                    'size' : '<xsl:value-of select="$recaptcha-size" />'
                });
            }
            
            function <xsl:value-of select="$js-funcname-torefresh"/>(focusNow)
            {
                // No refresh
            }
            
        </script>
        
    </xsl:template>
</xsl:stylesheet>
