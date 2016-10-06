<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2016 Anyware Services

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
                xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper"
                xmlns:math="java.lang.Math"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:import href="../common.xsl"/> 
    <xsl:import href="plugin:core://stylesheets/helper/common_3.3.xsl"/>    
    
    <xsl:param name="authFailure"/><!-- true to display a login failed message -->
    <xsl:param name="login"/><!-- the login previously submitted -->
    <xsl:param name="tooManyAttempts"/><!-- true when authentication failed due to too many attempts with that login -->
    <xsl:param name="cookieFailure"/><!-- true when the remember me function failed to authenticate -->
    
    <xsl:variable name="uniqueId" select="substring-after(math:random(), '.')"/>
    <xsl:variable name="uri-prefix" select="ametys:uriPrefix()"/>
    
    <xsl:template name="css-file"><xsl:value-of select="ametys:workspaceThemeURL()"/>/sass/special/login.scss</xsl:template>
    
    <xsl:template name="head"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_TITLE" i18n:catalogue="plugin.core-ui"/></xsl:template>
    
    <xsl:template name="main">
        <xsl:call-template name="login-left-column"/>
        
    	<div class="wrapin">
            <xsl:call-template name="login"/>
        </div>
        
        <xsl:call-template name="login-right-column"/>
    </xsl:template>
    
    <!-- For override purposes -->
    <xsl:template name="login-left-column"/>
    <xsl:template name="login-right-column"/>
    
    <!-- MAIN LOGIN TEMPLATE -->
            
    <xsl:template name="login">
        <div class="login">
            <xsl:call-template name="login-user-populations-standalone"/>
	        
	        <xsl:call-template name="login-credential-providers"/>
	    </div>
	</xsl:template>

    <xsl:template name="login-user-populations-standalone">
        <xsl:if test="count(/LoginScreen/UserPopulations/UserPopulation) > 1 and not(/LoginScreen/CredentialProviders/CredentialProvider)">
	        <div class="login-part">
	            <form method="post">
	                <div class="login-inner login-user-populations-standalone">
                        <xsl:call-template name="login-user-populations"/>

                        <xsl:call-template name="login-user-populations-submit"/>
                    </div>
                </form>
            </div>
        </xsl:if>
    </xsl:template>
    
    <!-- CREDENTIAL PROVIDERS -->
    
    <xsl:template name="login-credential-providers">
        <xsl:if test="/LoginScreen/CredentialProviders/CredentialProvider">
	        <div class="login-part">
		        <xsl:for-each select="/LoginScreen/CredentialProviders/CredentialProvider">
		            <xsl:if test="position() != 1">
		                <div class="login-part-credentialproviders-separator"><div class="textin"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SEPARATOR" i18n:catalogue="plugin.core-ui" /></div></div>
		            </xsl:if>
		            
	                <form method="post">
			            <input type="hidden" name="CredentialProviderIndex" value="{@index}"/>
			            <xsl:choose>
			                <xsl:when test="@isForm = 'true' and count(/LoginScreen/CredentialProviders/CredentialProvider[@isForm = 'true']) = 1">
			                    <xsl:call-template name="login-form"/>
			                </xsl:when>
			                <xsl:otherwise>
			                    <xsl:call-template name="login-credential-provider"/>
			                </xsl:otherwise>
			            </xsl:choose>
			        </form>
    	        </xsl:for-each>
    	        
                <xsl:call-template name="login-back"/>
		    </div>
		</xsl:if>
    </xsl:template>
    
    <xsl:template name="login-credential-provider">
        <div class="login-inner login-credential-provider">
            <button type="submit" style="background-color: #{color}; border: 1px solid #{color};">
                <xsl:value-of select="label"/>
                <span class="{iconGlyph}"></span>
            </button>
        </div>
    </xsl:template>
    
    <!-- BACK -->
    
    <xsl:template name="login-back">
        <xsl:if test="not(/LoginScreen/UserPopulations/UserPopulation)">
            <form method="post">
                <div class="login-inner login-back">
                    <input type="hidden" name="UserPopulation" value=""/>
                    <button type="submit"><i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_BACK' i18n:catalogue='plugin.core-ui'/></button>
                </div>
            </form>
        </xsl:if>
    </xsl:template>
    
    <!-- FORM -->
    
    <xsl:template name="login-form">
        <div class="login-inner login-form">
            <xsl:if test="count(/LoginScreen/UserPopulations/UserPopulation) > 1">
                <xsl:call-template name="login-user-populations"/>
            </xsl:if>

            <xsl:call-template name="login-form-inputs"/>

            <xsl:call-template name="login-form-submit"/>
        </div>
    </xsl:template>

    <!-- USERS POPULATIONS -->

    <xsl:template name="login-user-populations">
        <xsl:variable name="public" select="/LoginScreen/UserPopulations/@public = 'true'"/>
        <xsl:variable name="list" select="$public = true() and count(/LoginScreen/UserPopulations/UserPopulation) > 1"/>
        
        <xsl:if test="$public = true() or $list = true()">
            <xsl:call-template name="login-user-populations-part">
                <xsl:with-param name="public" select="$public = true()"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="login-user-populations-part">
        <xsl:param name="public"/>

        <div class="login-user-populations">
             <xsl:if test="/LoginScreen/populations/@invalidError = 'true'">
                 <div class="error">
                     <i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_INVALID_POPULATION" i18n:catalogue="plugin.core-ui"/>
                 </div>
             </xsl:if>
             
	        <xsl:choose>
	            <xsl:when test="$public = true()">
	                <xsl:call-template name="login-user-populations-select"/>
	            </xsl:when>
	            <xsl:otherwise>
	                <xsl:call-template name="login-user-populations-input"/>
	            </xsl:otherwise>
	        </xsl:choose>
	   </div>
    </xsl:template>

    <xsl:template name="login-user-populations-input">
        <xsl:call-template name="login-user-populations-input-internal">
            <xsl:with-param name="label">plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_POPULATION</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="login-user-populations-input-internal">
        <xsl:param name="label"/>
        
        <div class="login-input-wrapper login-input-wrapper-userpopulation login-input-wrapper-userpopulation-input">
	        <input type="text" id="UserPopulation" name="UserPopulation" autofocus="true" required="" value="{/LoginScreen/UserPopulations/@currentValue}">
	            <xsl:if test="$label != ''">
	                <xsl:attribute name="placeholder"><xsl:value-of select="$label"/></xsl:attribute>
	                <xsl:attribute name="i18n:attr">placeholder</xsl:attribute>
	            </xsl:if>
	        </input>
	    </div>
    </xsl:template>
    
    <xsl:template name="login-user-populations-select">
        <xsl:call-template name="login-user-populations-select-internal">
            <xsl:with-param name="label">plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_POPULATION</xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    <xsl:template name="login-user-populations-select-internal">
        <xsl:param name="label"/>
        
        <div class="login-input-wrapper login-input-wrapper-userpopulation login-input-wrapper-userpopulation-select">
            <xsl:if test="$label != ''">
                <xsl:attribute name="placeholder"><xsl:value-of select="$label"/></xsl:attribute>
                <xsl:attribute name="i18n:attr">placeholder</xsl:attribute>
            </xsl:if>
                    
	        <select id="Population" name="UserPopulation" autofocus="true" required="">
	            <xsl:if test="$label != ''">
	                <xsl:attribute name="placeholder"><xsl:value-of select="$label"/></xsl:attribute>
	                <xsl:attribute name="i18n:attr">placeholder</xsl:attribute>
	            </xsl:if>
	
	            <xsl:for-each select="/LoginScreen/UserPopulations/UserPopulation">
	                <xsl:sort select="label"/>
	               
	                <option value='{@id}'>
	                    <xsl:if test="../@currentValue = @id"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>    
	                    <xsl:value-of select="label" />
	                </option>
	            </xsl:for-each>
	        </select>
	        <xsl:if test="not(/LoginScreen/UserPopulations/@currentValue)">
		        <script type="text/javascript">
		            var cls = ' login-input-wrapper-userpopulation-select-empty';
		            var select = document.getElementById('Population'); 
		            select.selectedIndex = -1;
		            select.parentNode.className += cls;
		            select.onchange = function() {
		               this.parentNode.className = this.parentNode.className.replace(cls, '')
		            }
		        </script>
		    </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="login-user-populations-submit">
        <button type="submit"><i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_CONTINUE' i18n:catalogue='plugin.core-ui'/></button>
    </xsl:template>

    <!-- FORM -->
    
    <xsl:template name="login-form-submit">
        <button type="submit"><i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SUBMIT' i18n:catalogue='plugin.core-ui'/></button>
    </xsl:template>
    
    <xsl:template name="login-form-inputs">
    	<xsl:if test="/LoginScreen/LoginForm">
		    <div class="login-form-inputs">
		        <xsl:call-template name="login-form-inputs-errors"/>
		         
		        <xsl:call-template name="login-form-inputs-username"/>
		        
                <xsl:call-template name="login-form-inputs-password"/>
                      
                <xsl:if test="/LoginScreen/LoginForm/useCaptcha = 'true'">
                    <xsl:call-template name="login-form-inputs-captcha"/>
                </xsl:if>
                


                      
<!-- 	                        <div class="forgotten"> -->
<!-- 	                           <a>Mot de passe oublié</a> -->
<!-- 	                        </div> -->
                      
            	<xsl:if test="/LoginScreen/LoginForm/rememberMe = 'true'">
                    <xsl:call-template name="login-form-inputs-rememberme"/>
            	</xsl:if>
			</div>
		</xsl:if>
    </xsl:template>
    
    <xsl:template name="login-form-inputs-errors">
        <xsl:if test="/LoginScreen/LoginForm/showErrors = 'true' and $authFailure = 'true'">
            <div class="error">
                <xsl:choose>
                    <xsl:when test="$tooManyAttempts = 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_FAILED_MANY_TIME_{ametys:config('runtime.captcha.type')}" i18n:catalogue="plugin.core-ui"/></xsl:when>
                    <xsl:when test="/LoginScreen/LoginForm/useCaptcha = 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_FAILED_WITH_CAPTCHA_{ametys:config('runtime.captcha.type')}" i18n:catalogue="plugin.core-ui"/></xsl:when>
                    <xsl:when test="$cookieFailure != 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_AUTH_FAILURE" i18n:catalogue="plugin.core-ui"/></xsl:when>
                </xsl:choose>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="login-form-inputs-username">
        <xsl:call-template name="login-form-inputs-username-internal">
            <xsl:with-param name="label">plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_LOGIN</xsl:with-param>
        </xsl:call-template>
    </xsl:template>    
    <xsl:template name="login-form-inputs-username-internal">
        <xsl:param name="label"/>

        <xsl:variable name="autocomplete">
            <xsl:choose>
                <xsl:when test="/LoginScreen/LoginForm/autocomplete = 'true'">on</xsl:when>
                <xsl:otherwise>off</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <div class="login-input-wrapper login-input-wrapper-username">
            <input type="text" name="Username" id="Username" autocomplete="{$autocomplete}" autofocus="true" value="{$login}" onFocus="this.select();">
                <xsl:if test="$label != ''">
                    <xsl:attribute name="placeholder"><xsl:value-of select="$label"/></xsl:attribute>
                    <xsl:attribute name="i18n:attr">placeholder</xsl:attribute>
                </xsl:if>
            </input>
        </div>
    </xsl:template>
    

    <xsl:template name="login-form-inputs-password">
        <xsl:call-template name="login-form-inputs-password-internal">
            <xsl:with-param name="label">plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PASSWORD</xsl:with-param>
        </xsl:call-template>
    </xsl:template>    
    <xsl:template name="login-form-inputs-password-internal">
        <xsl:param name="label"/>

        <xsl:variable name="autocomplete">
            <xsl:choose>
                <xsl:when test="/LoginScreen/LoginForm/autocomplete = 'true'">on</xsl:when>
                <xsl:otherwise>new-password</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <div class="login-input-wrapper login-input-wrapper-password">
	        <input type="password" name="Password" id="Password" autocomplete="{$autocomplete}">
	            <xsl:if test="$label != ''">
	                <xsl:attribute name="placeholder"><xsl:value-of select="$label"/></xsl:attribute>
	                <xsl:attribute name="i18n:attr">placeholder</xsl:attribute>
	            </xsl:if>
	        </input>
	    </div>
    </xsl:template>    
    
    <xsl:template name="login-form-inputs-rememberme">
        <xsl:call-template name="login-form-inputs-rememberme-internal">
            <xsl:with-param name="label">plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PERSIST</xsl:with-param>
        </xsl:call-template>
    </xsl:template>    
    <xsl:template name="login-form-inputs-rememberme-internal">
        <xsl:param name="label"/>
        
         <div class="login-input-wrapper login-input-wrapper-rememberme">
             <input type="checkbox" name="rememberMe" id="rememberMe" value="true"/>
             <xsl:if test="$label != ''">
                <label for="rememberMe"><i18n:text i18n:key="{substring-after($label, ':')}" i18n:catalogue="{substring-before($label, ':')}"/></label>
             </xsl:if>
         </div>
    </xsl:template>
    
    <xsl:template name="login-form-inputs-captcha">
        <xsl:call-template name="login-form-inputs-captcha-internal">
            <xsl:with-param name="label">plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_CAPTCHA</xsl:with-param>
        </xsl:call-template>
    </xsl:template>    
    <xsl:template name="login-form-inputs-captcha-internal">
        <xsl:param name="label"/>
        
        <div class="login-input-wrapper login-input-wrapper-captcha login-input-wrapper-captcha-{ametys:config('runtime.captcha.type')}">
	        <xsl:call-template name="captcha">
	            <xsl:with-param name="key-name" select="'CaptchaKey'"/>
	
	            <xsl:with-param name="value-name" select="'Captcha'"/>
	            <xsl:with-param name="value-id" select="concat('captcha-', $uniqueId)"/>
	            <xsl:with-param name="value-class">captcha</xsl:with-param>
	
	            <xsl:with-param name="image-alt"/>
	            <xsl:with-param name="image-alt-i18n" select="false()"/>
	            <xsl:with-param name="image-class">captcha-image</xsl:with-param>
	            
	            <xsl:with-param name="image-height">50</xsl:with-param>
	            <xsl:with-param name="image-width">160</xsl:with-param>
	            
	            <xsl:with-param name="placeholder" select="substring-after($label, ':')"/>
	            <xsl:with-param name="placeholder-i18n" select="true()"/>
	            <xsl:with-param name="placeholder-catalogue" select="substring-before($label, ':')"/>
	            
	            <xsl:with-param name="contextPath" select="$contextPath"/>
	        </xsl:call-template> 
	    </div>        
    </xsl:template>    
</xsl:stylesheet>
