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
    <xsl:import href="login_form.xsl"/> 
    
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
</xsl:stylesheet>
