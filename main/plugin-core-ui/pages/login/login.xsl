<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2012 Anyware Services

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
    <xsl:import href="plugin:core://stylesheets/helper/common.xsl"/>    
    
    <xsl:param name="authFailure"/><!-- true to display a login failed message -->
    <xsl:param name="tooManyAttempts"/><!-- true when authentication failed due to too many attempts with that login -->
    <xsl:param name="cookieFailure"/><!-- true when the remember me function failed to authenticate -->
    
    <xsl:variable name="uniqueId" select="substring-after(math:random(), '.')"/>
    
    <xsl:template name="css-file">/plugins/core-ui/resources/css/special/login.css</xsl:template>
    
    <xsl:template name="head"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_TITLE" i18n:catalogue="plugin.core-ui"/></xsl:template>
    
    <xsl:template name="main">
         <xsl:variable name="autocomplete">
             <xsl:choose>
                 <xsl:when test="/LoginForm/autocomplete = 'true'">on</xsl:when>
                 <xsl:otherwise>off</xsl:otherwise>
             </xsl:choose>
         </xsl:variable>

        <div class="wrapin">        
	        <div class="connection">
<!--                 <button class="cas" type="submit">Se connecter avec CAS</button> -->
<!--                 <div class="separator"><div class="text">ou</div></div> -->
                
	            <form method="post" class="formbased" action="">
	                <div>
	                        <xsl:if test="$authFailure = 'true'">
	                            <div class="error">
	                                <xsl:choose>
	                                    <xsl:when test="$tooManyAttempts = 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_FAILED_MANY_TIME" i18n:catalogue="plugin.core-ui"/></xsl:when>
	                                    <xsl:when test="/LoginForm/useCaptcha = 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_FAILED_WITH_CAPTCHA" i18n:catalogue="plugin.core-ui"/></xsl:when>
	                                    <xsl:when test="$cookieFailure != 'true'"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_AUTH_FAILURE" i18n:catalogue="plugin.core-ui"/></xsl:when>
	                                </xsl:choose>
	                            </div>
	                        </xsl:if>
	                        
		                    <table class="input inputtext login">
		                        <tr>
		                            <td class="input"><input type="text" name="Username" id="Username" autocomplete="{$autocomplete}" placeholder="plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_LOGIN" i18n:attr="placeholder" autofocus="true"/></td>
		                            <td class="image"></td>
		                        </tr>
		                    </table>
	
	                        <table class="input inputtext password">
	                            <tr>
	                                <td class="input"><input type="password" name="Password" id="Password" autocomplete="{$autocomplete}" placeholder="plugin.core-ui:PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PASSWORD" i18n:attr="placeholder"/></td>
	                                <td class="image"></td>
	                            </tr>
	                        </table>                            
	                        
	                        <xsl:if test="/LoginForm/useCaptcha = 'true'">
	                            <div class="input">
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
	                                    
	                                    <xsl:with-param name="contextPath" select="$contextPath"/>
	                                </xsl:call-template>         
	                            </div>
	                            <br style="clear: left"/>
	                        </xsl:if>
	                        
<!-- 	                        <div class="forgotten"> -->
<!-- 	                           <a>Mot de passe oublié</a> -->
<!-- 	                        </div> -->
	                        
	                        <xsl:if test="/LoginForm/rememberMe = 'true'">
	                            <div class="input checkbox rememberMe">
	                                <input type="checkbox" name="rememberMe" id="rememberMe" value="true"/><label for="rememberMe"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PERSIST" i18n:catalogue="plugin.core-ui"/></label>
	                            </div>
	                        </xsl:if>
	                        
	                        <button type="submit"><i18n:text i18n:key="PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SUBMIT" i18n:catalogue="plugin.core-ui"/></button>
	                </div>
	            </form>
                
<!-- 	            <div class="separator"><div class="text">ou</div></div> -->
<!-- 	            <button class="facebook" type="submit">Se connecter avec Facebook</button> -->
	            
<!-- 	            <div class="separator"><div class="text">ou</div></div> -->
<!-- 	            <button class="twitter" type="submit">Se connecter avec Twitter</button> -->
	            
<!-- 	            <div class="separator"><div class="text">ou</div></div> -->
<!-- 	            <button class="google" type="submit">Se connecter avec Google</button> -->
	        </div>
        </div>

        <div class="wrapin">        
           <div class="intro">
               <div class="textin">
                      <h1>Ametys demo</h1>
                      <p>Bienvenue sur le CMS Ametys!</p>
                      <p>Les identifiants suivants sont disponibles :</p>
                      <ul>
                          <li>admin / admin</li>
                          <li>webmaster / webmaster</li>
                          <li>manager / manager</li>
                          <li>contrib / contrib</li>
                      </ul>

                      <h1>Ressources</h1>
                      <p>Trouvez la réponse à vos questions :</p>
                      <div class="links">
                           <a class="website" href="http://www.ametys.org" title="Site web" target="_blank"><span>Site web</span></a>
                           <a class="doc" href="http://wiki.ametys.org" title="Documentation" target="_blank"><span>Documentation</span></a>
                           <a class="forum" href="http://www.ametys.org/forum" title="Forum" target="_blank"><span>Forum</span></a>
                           <a class="issues" href="http://issues.ametys.org" title="Jira" target="_blank"><span>Jira</span></a>
                      </div>
               
               </div>
               <div class="textin">
                      <h1>Suivez-nous</h1>
                      <p>Suivez-nous sur les réseaux :</p>
                      <div class="links">
                           <a class="facebook" title="Suivez-nous sur Facebook" href="https://www.facebook.com/AmetysCMS" target="_blank"><span>Suivez-nous sur Facebook</span></a>
                           <a class="twitter" title="Suivez-nous sur Twitter" href="https://twitter.com/AmetysCMS" target="_blank"><span>Suivez-nous sur Twitter</span></a>
                           <a class="googleplus" title="Suivez-nous sur Google+" href="https://plus.google.com/+AmetysOrg" target="_blank"><span>Suivez-nous sur Google+</span></a>
                      </div>
                      <div class="twitter-timeline-wrapper">
			                <a class="twitter-timeline" href="https://twitter.com/AmetysCMS" data-widget-id="639097471352860672" data-chrome="transparent noheader nofooter noborders noscrollbar" data-screen-name="" data-tweet-limit="20">
			                   <div class="la-ball-pulse la-dark la-2x" id="pulse">
			                          <div></div>
			                          <div></div>
			                          <div></div>
			                   </div>
			                </a>
					        <script type="text/javascript">
					           !function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+"://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");
					        </script>
		             </div>
	           </div>
           </div>
        </div>
    </xsl:template>

    <xsl:template name="head-more">
    </xsl:template>
    
</xsl:stylesheet>
