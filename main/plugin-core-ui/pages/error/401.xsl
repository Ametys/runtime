<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2015 Anyware Services

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
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:import href="common.xsl"/>    
    
    <xsl:template name="head"><i18n:text i18n:key="PLUGINS_CORE_UI_ERROR_401_HEAD" i18n:catalogue="plugin.core-ui"/></xsl:template>
    <xsl:template name="title"><i18n:text i18n:key="PLUGINS_CORE_UI_ERROR_401_TITLE" i18n:catalogue="plugin.core-ui"/></xsl:template>
    <xsl:template name="title-class">long</xsl:template>
    <xsl:template name="text"><i18n:text i18n:key="PLUGINS_CORE_UI_ERROR_401_TEXT" i18n:catalogue="plugin.core-ui"/></xsl:template>
    <xsl:template name="text-additionnal">
        <xsl:call-template name="button">
            <xsl:with-param name="text"><i18n:text i18n:key='PLUGINS_CORE_UI_ERROR_401_LINK' i18n:catalogue='plugin.core-ui'/></xsl:with-param>
            <xsl:with-param name="href"></xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
</xsl:stylesheet>