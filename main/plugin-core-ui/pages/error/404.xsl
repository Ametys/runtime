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
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
    
    <xsl:import href="common.xsl"/>    
    
    <xsl:template name="text">Hmm…</xsl:template><
    
    <xsl:template name="description"><i18n:text i18n:key="KERNEL_ERROR_404" i18n:catalogue="kernel"/></xsl:template>
    
</xsl:stylesheet>