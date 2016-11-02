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
    
    <xsl:import href="plugin:core://stylesheets/helper/common_3.3.xsl"/>    
    
    <xsl:param name="authFailure" select="ametys:requestParameter('authFailure')"/><!-- true to display a login failed message -->
    <xsl:param name="login" select="ametys:requestParameter('login')"/><!-- the login previously submitted -->
    <xsl:param name="tooManyAttempts" select="ametys:requestParameter('tooManyAttempts')"/><!-- true when authentication failed due to too many attempts with that login -->
    <xsl:param name="cookieFailure" select="ametys:requestParameter('cookieFailure')"/><!-- true when the remember me function failed to authenticate -->
    
    <xsl:variable name="uniqueId" select="substring-after(math:random(), '.')"/>
    <xsl:variable name="uri-prefix" select="ametys:uriPrefix()"/>
    <xsl:variable name="contextPath" select="ametys:uriPrefix(false())"/>

    <!-- MAIN LOGIN TEMPLATE -->
            
    <xsl:template name="login">
        <div class="login">
            <xsl:call-template name="login-user-populations-standalone"/>
	        
	        <xsl:call-template name="login-credential-providers"/>
	    </div>
	</xsl:template>

    <xsl:template name="login-user-populations-standalone">
        <xsl:if test="not(/LoginScreen/CredentialProviders/CredentialProvider) or (count(LoginScreen/CredentialProviders/CredentialProvider) = 1 and /LoginScreen/CredentialProviders/CredentialProvider[not(@isForm = 'true')])">
	        <div class="login-part">
	            <form method="post">
	                <div class="login-inner login-user-populations-standalone">
	                    <input type="hidden" name="CredentialProviderIndex" value="-1"/>
	                
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
            <xsl:choose>
		        <xsl:when test="not((count(LoginScreen/CredentialProviders/CredentialProvider) = 1 and /LoginScreen/CredentialProviders/CredentialProvider[not(@isForm = 'true')]))">
			        <div class="login-part">
				        <xsl:for-each select="/LoginScreen/CredentialProviders/CredentialProvider">
				            <xsl:if test="position() != 1">
				                <div class="login-part-credentialproviders-separator"><div class="textin"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SEPARATOR" i18n:catalogue="plugin.core-ui" /></div></div>
				            </xsl:if>
				            
			                <form method="post">
					            <input type="hidden" name="CredentialProviderIndex" value="{@index}"/>
					            <xsl:choose>
					                <xsl:when test="@isForm = 'true' and (count(/LoginScreen/CredentialProviders/CredentialProvider[@isForm = 'true']) = 1 or @selected = 'true')">
					                    <xsl:call-template name="login-form"/>
					                </xsl:when>
		                            <xsl:when test="@isNewWindowRequired = 'false'">
		                                <xsl:call-template name="login-credential-provider-reload"/>
		                            </xsl:when>
					                <xsl:otherwise>
					                    <xsl:call-template name="login-credential-provider-popup"/>
					                </xsl:otherwise>
					            </xsl:choose>
					        </form>
		    	        </xsl:for-each>
		    	        
		                <xsl:call-template name="login-back"/>
				    </div>
				</xsl:when>
				<xsl:when test="/LoginScreen/CredentialProviders/CredentialProvider[@isNewWindowRequired = 'true']">
				    <script type="text/javascript">
				        <xsl:call-template name="login-credential-provider-script"/>
				    </script>
				</xsl:when>
				<xsl:otherwise>
				    <!-- We are in a population with a single blocking CP that does not require a new window... this should not happen -->
				</xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    
    <xsl:template name="login-credential-provider-reload">
        <div class="login-inner login-credential-provider">
            <button type="submit" style="background-color: #{color}; border: 1px solid #{color};">
                <i18n:translate>
                    <i18n:text i18n:key="PLUGINS_CORE_AUTHENTICATION_BLOCKING_CONNECTION_LABEL" i18n:catalogue="plugin.core-impl"/>
                    <i18n:param>
		                <xsl:choose>
		                    <xsl:when test="additionalLabel"><xsl:value-of select="additionalLabel"/></xsl:when>
		                    <xsl:otherwise><xsl:value-of select="label"/></xsl:otherwise>
		                </xsl:choose>
                    </i18n:param>
                </i18n:translate>
                <span class="glyph {iconGlyph}"></span>
            </button>
        </div>
    </xsl:template>
    
    <xsl:template name="login-credential-provider-popup">
        <div class="login-inner login-credential-provider">
            <button style="background-color: #{color}; border: 1px solid #{color};">
                <xsl:attribute name="onclick"><xsl:call-template name="login-credential-provider-script"><xsl:with-param name="index" select="@index"/></xsl:call-template> return false;</xsl:attribute>
            
                <i18n:translate>
                    <i18n:text i18n:key="PLUGINS_CORE_AUTHENTICATION_BLOCKING_CONNECTION_LABEL" i18n:catalogue="plugin.core-impl"/>
                    <i18n:param>
                        <xsl:choose>
                            <xsl:when test="additionalLabel"><xsl:value-of select="additionalLabel"/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="label"/></xsl:otherwise>
                        </xsl:choose>
                    </i18n:param>
                </i18n:translate>
                <span class="glyph {iconGlyph}"></span>
            </button>
        </div>
    </xsl:template>
    
    <xsl:template name="login-credential-provider-script">
        <xsl:param name="index">0</xsl:param>
        
        try { window.open("<xsl:value-of select="ametys:uriPrefix()"/>/plugins/core/authenticate/<xsl:value-of select="$index"/>?contexts=<xsl:value-of select="ametys:urlEncode(/LoginScreen/contexts)"/>", null, "height: ;") } catch (e) { }
    </xsl:template>
    
    
    <!-- BACK -->
    
    <xsl:template name="login-back">
        <xsl:if test="not(/LoginScreen/UserPopulations)">
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
            <xsl:if test="/LoginScreen/UserPopulations/@size > 1">
                <xsl:call-template name="login-user-populations"/>
            </xsl:if>

            <xsl:call-template name="login-form-inputs"/>

            <xsl:call-template name="login-form-submit"/>
        </div>
    </xsl:template>

    <!-- USERS POPULATIONS -->

    <xsl:template name="login-user-populations">
        <xsl:variable name="public" select="/LoginScreen/UserPopulations/@public = 'true'"/>
        <xsl:variable name="list" select="$public = true() and /LoginScreen/UserPopulations/@size > 1"/>

        <xsl:if test="$public = false() or $list = true()">
            <xsl:call-template name="login-user-populations-part">
                <xsl:with-param name="public" select="$public = true()"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="login-user-populations-part">
        <xsl:param name="public"/>

        <div class="login-user-populations">
             <xsl:if test="/LoginScreen/UserPopulations/@invalid = 'true'">
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
	                    <xsl:copy-of select="label" />
	                </option>
	            </xsl:for-each>
	        </select>
	        <xsl:if test="not(/LoginScreen/UserPopulations/@currentValue)">
		        <script type="text/javascript">
		            var cls = ' login-input-wrapper-userpopulation-select-empty';
		            var select = document.getElementById('Population');
		            select.selectedIndex = -1;
                    select.parentNode.className += cls;
		            window.addEventListener('load', function() {
  	                    if (select.selectedIndex != -1)
  	                    {
  	                        // BACK was done
			                select.onchange();
			            }
			        });
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
        <button type="submit">
            <i18n:translate>
                <i18n:text i18n:key="PLUGINS_CORE_AUTHENTICATION_BLOCKING_CONNECTION_LABEL" i18n:catalogue="plugin.core-impl"/>
                <i18n:param>
                    <xsl:choose>
                        <xsl:when test="/LoginScreen/LoginForm/additionalLabel"><xsl:value-of select="/LoginScreen/LoginForm/additionalLabel"/></xsl:when>
                        <xsl:otherwise><i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SUBMIT' i18n:catalogue='plugin.core-ui'/></xsl:otherwise>
                    </xsl:choose>
                </i18n:param>
            </i18n:translate>
        </button>
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
                
                <xsl:call-template name="login-form-inputs-forgotten"/>
                
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
                    <xsl:when test="not($cookieFailure = 'true')"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_AUTH_FAILURE" i18n:catalogue="plugin.core-ui"/></xsl:when>
                </xsl:choose>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template name="login-form-inputs-forgotten">
<!--         Feature not supported in backoffice -->
<!--         <xsl:call-template name="login-form-inputs-forgotten-internal"> -->
<!--             <xsl:with-param name="label"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PASSWORD_FORGOTTEN" i18n:catalogue="plugin.core-ui"/></xsl:with-param> -->
<!--             <xsl:with-param name="url"/> -->
<!--         </xsl:call-template> -->
    </xsl:template>
    <xsl:template name="login-form-inputs-forgotten-internal">
        <xsl:param name="label"/>
        <xsl:param name="url"/>
    
        <div class="login-link-wrapper-forgotten">
            <a href="{$url}"><xsl:copy-of select="$label"/></a>
        </div>
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
	        </xsl:call-template> 
	    </div>        
    </xsl:template>   
</xsl:stylesheet>
