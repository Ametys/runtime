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
			<form method="post" action="">
				<xsl:if test="/LoginScreen/populations and count(/LoginScreen/populations/population) != 1">
					<div class="connection">
						<div class="choosepopulation">
							<div>
								<xsl:if test="/LoginScreen/populations/@invalidError = 'true'">
		                            <div class="error">
	                                    <i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_INVALID_POPULATION" i18n:catalogue="plugin.core-ui"/>
		                            </div>
		                        </xsl:if>
								<xsl:choose>
									<xsl:when test="/LoginScreen/populations/population">
										<style type="text/css">
											div.connection table.population td.input.empty:before {
												content: "<i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_POPULATION" i18n:catalogue="plugin.core-ui" />";
												pointer-events: none;
											}
										</style>
										<table class="input inputtext population">
											<tr>
												<td class="input select empty">
													<select id="Population" name="hiddenPopulation" required="">
														<xsl:for-each select="/LoginScreen/populations/population">
														   <xsl:sort select="label"/>
															<option value='{@id}'><xsl:value-of select="label" /></option>
														</xsl:for-each>
													</select>
												</td>
												<td class="image"></td>
											</tr>
										</table>
	                                    <script>
	                                        document.getElementById("Population").selectedIndex = -1;
	                                        
	                                        var isIE = /*@cc_on!@*/false || !!document.documentMode;
	                                        if (isIE)
	                                        {
	                                            var elements = document.getElementsByTagName("fieldset");
	                                            for (i = 0; i &lt; elements.length; i++) {
	                                                elements[i].disabled=true;
	                                            }
	                                        }
	                                        
	                                        document.getElementById("Population").onchange = function() {
	                                            // Remove the placeholder for the select population field
	                                            this.parentNode.className='input select';
	                                            
	                                            // Enable the HTML fields
	                                            var elements = document.getElementsByTagName("fieldset");
	                                            for (i = 0; i &lt; elements.length; i++) {
	                                                elements[i].disabled=false;
	                                            }
	                                            
	                                            var authFormsEl = document.getElementById("authForms");
	                                            if (authFormsEl != null)
	                                            {
	                                                elements = authFormsEl.getElementsByTagName("table");
	                                                for (i = 0; i &lt; elements.length; i++) {
	                                                    elements[i].className = elements[i].className.replace('disabled', ''); 
	                                                }
	                                            } 
	                                        }
	                                    </script>
									</xsl:when>
									<xsl:otherwise>
										<table class="input inputtext population">
											<tr>
												<td class="input">
													<input type="text" id="Population" name="hiddenPopulation" placeholder="plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_POPULATION" i18n:attr="placeholder" autofocus="true" required="" />
												</td>
												<td class="image"></td>
											</tr>
										</table>
	                                    <script>
	                                        document.getElementById("Population").oninput = function() {
	                                            // Enable the HTML fields
	                                            var elements = document.getElementsByTagName("fieldset");
	                                            for (i = 0; i &lt; elements.length; i++) {
	                                                elements[i].disabled=false;
	                                            }
	                                            
	                                            var authFormsEl = document.getElementById("authForms");
	                                            if (authFormsEl != null)
	                                            {
	                                                elements = authFormsEl.getElementsByTagName("table");
	                                                for (i = 0; i &lt; elements.length; i++) {
	                                                    elements[i].className = elements[i].className.replace('disabled', ''); 
	                                                }
	                                            }
	                                        }
	                                    </script>
									</xsl:otherwise>
								</xsl:choose>
							</div>
							
							<xsl:choose>
								<xsl:when test="/LoginScreen/LoginForm">
									<style>
										table.input.inputtext.population {
											margin-bottom: 0;
										}
									</style>
								</xsl:when>
								<xsl:otherwise>
									<!-- Submit button only if no LoginForm -->
						    		<xsl:call-template name="submit-button">
										<xsl:with-param name="text"><i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SUBMIT' i18n:catalogue='plugin.core-ui'/></xsl:with-param>
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</div>
					</div>
				</xsl:if>
			    
				<xsl:if test="/LoginScreen/LoginForm or /LoginScreen/credentialProviders">
					<div class="connection" id="authForms">	
				    	<xsl:call-template name="login-form"/>
						
			    		<xsl:for-each select="/LoginScreen/credentialProviders/credentialProvider">
			    			<xsl:if test="/LoginScreen/PopulationsForm or /LoginScreen/LoginForm or position() != 1">
		                		<div class="separator"><div class="textin"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SEPARATOR" i18n:catalogue="plugin.core-ui" /></div></div>
		                	</xsl:if>
		                	<div>
		                		<fieldset>
		                			<xsl:if test="/LoginScreen/PopulationsForm"><xsl:attribute name="disabled"/></xsl:if>
			                		<input type="hidden" name="CredentialProviderIndex" value="{index}"/>
			                		<xsl:call-template name="submit-button">
			                			<xsl:with-param name="text"><xsl:value-of select="label"/><span class="{iconGlyph}"></span></xsl:with-param>
			                			<xsl:with-param name="style">background-color: #<xsl:value-of select="color"/>; border: 1px solid #<xsl:value-of select="color"/>;</xsl:with-param>
			                			<xsl:with-param name="class">otherauth</xsl:with-param>
			                		</xsl:call-template>
		                		</fieldset>
		                	</div>
		                </xsl:for-each>
						<xsl:if test="/LoginScreen/backButton = 'true'">
							<div class="back">
								<xsl:for-each select="/LoginScreen/backButtonParams/backButtonParam"><input type="hidden" name="{@name}" id="{@name}" value="{@value}"/></xsl:for-each>
								
								<button type="submit"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_BACK" i18n:catalogue="plugin.core-ui" /></button>
							</div>
						</xsl:if>
					</div>
				</xsl:if>
            </form>				
		</div>
		
		<xsl:call-template name="login-right-column"/>
    </xsl:template>
    
    <xsl:template name="login-form">
    	<xsl:if test="/LoginScreen/LoginForm">
		    <div class="formbased">
			    <fieldset>
			    	<xsl:if test="/LoginScreen/PopulationsForm"><xsl:attribute name="disabled"/></xsl:if>
		    	
		    		<xsl:variable name="autocomplete">
			            <xsl:choose>
			                <xsl:when test="/LoginScreen/LoginForm/autocomplete = 'true'">on</xsl:when>
			                <xsl:otherwise>off</xsl:otherwise>
			            </xsl:choose>
			        </xsl:variable>
			         
		    		<xsl:if test="/LoginScreen/LoginForm/showErrors = 'true' and $authFailure = 'true'">
                        <div class="error">
                            <xsl:choose>
                                <xsl:when test="$tooManyAttempts = 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_FAILED_MANY_TIME" i18n:catalogue="plugin.core-ui"/></xsl:when>
                                <xsl:when test="/LoginScreen/LoginForm/useCaptcha = 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_FAILED_WITH_CAPTCHA" i18n:catalogue="plugin.core-ui"/></xsl:when>
                                <xsl:when test="$cookieFailure != 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_AUTH_FAILURE" i18n:catalogue="plugin.core-ui"/></xsl:when>
                            </xsl:choose>
                        </div>
                    </xsl:if>
	                      
	                <table>
	                      	<xsl:attribute name="class">input inputtext login<xsl:if test="/LoginScreen/PopulationsForm"> disabled</xsl:if></xsl:attribute>
	                       <tr>
	                           <td class="input"><input type="text" name="Username" id="Username" autocomplete="{$autocomplete}" placeholder="plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_LOGIN" i18n:attr="placeholder" autofocus="true" value="{$login}" onFocus="this.select();"/></td>
	                           <td class="image"></td>
	                       </tr>
	                </table>
	
	                <table>
	                      	<xsl:attribute name="class">input inputtext password<xsl:if test="/LoginScreen/PopulationsForm"> disabled</xsl:if></xsl:attribute>
	                          <tr>
	                              <td class="input"><input type="password" name="Password" id="Password" autocomplete="{$autocomplete}" placeholder="plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PASSWORD" i18n:attr="placeholder"/></td>
	                              <td class="image"></td>
	                          </tr>
	                </table>                            
	                      
	                <xsl:if test="/LoginScreen/LoginForm/useCaptcha = 'true'">
	                          <table>
	                               <xsl:attribute name="class">input captcha <xsl:value-of select="ametys:config('runtime.captcha.type')"/></xsl:attribute>
	                              <tr>
	                                  <td class="input">
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
				                                  
				                                  <xsl:with-param name="placeholder">PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_CAPTCHA</xsl:with-param>
				                                  <xsl:with-param name="placeholder-i18n" select="true()"/>
                                                  <xsl:with-param name="placeholder-catalogue">plugin.core-ui</xsl:with-param>
				                                  
				                                  <xsl:with-param name="contextPath" select="$contextPath"/>
				                              </xsl:call-template>         
	                                  </td>
	                                  <td class="image"></td>
	                              </tr>
                              </table>
		            </xsl:if>
		                        
	<!-- 	                        <div class="forgotten"> -->
	<!-- 	                           <a>Mot de passe oublié</a> -->
	<!-- 	                        </div> -->
	                      
	            	<xsl:if test="/LoginScreen/LoginForm/rememberMe = 'true'">
	                          <div class="input checkbox rememberMe">
	                              <input type="checkbox" name="rememberMe" id="rememberMe" value="true"/><label for="rememberMe"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PERSIST" i18n:catalogue="plugin.core-ui"/></label>
	                          </div>
	            	</xsl:if>
		    	
					<xsl:call-template name="submit-button">
						<xsl:with-param name="formbased">true</xsl:with-param>
						<xsl:with-param name="text"><i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SUBMIT' i18n:catalogue='plugin.core-ui'/></xsl:with-param>
					</xsl:call-template>
				</fieldset>
			</div>
		</xsl:if>
    </xsl:template>
    
    <!-- For each submit button (except for the formbased one) we want to add an hidden input indicating how to get the current page for the "back feature" -->
    <xsl:template name="submit-button">
    	<xsl:param name="formbased">false</xsl:param>
    	<xsl:param name="text"/>
    	<xsl:param name="style"/>
    	<xsl:param name="class"/>
    	
    	<button type="submit">
    		<xsl:attribute name="style"><xsl:copy-of select="$style"/></xsl:attribute>
    		<xsl:attribute name="class"><xsl:copy-of select="$class"/></xsl:attribute>
    		<xsl:copy-of select="$text"/>
    	</button>
    </xsl:template>
    
    <xsl:template name="login-left-column"/>
    <xsl:template name="login-right-column"/>
    
</xsl:stylesheet>
