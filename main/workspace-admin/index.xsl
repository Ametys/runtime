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
                xmlns:escaper="org.apache.commons.lang.StringEscapeUtils"
                xmlns:ametys="org.ametys.runtime.plugins.core.ui.AmetysXSLTHelper"
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1">
                
    <xsl:import href="kernel://stylesheets/kernel.xsl"/>
    
    <xsl:template match="/">
        <html>
            <head>
                <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
                <title>Admin test</title>
                <xsl:call-template name="kernel-base"/>
                
                <xsl:variable name="uriPrefix" select="ametys:uriPrefix()"/>
<!--                 <script type="text/javascript" src="{$uriPrefix}/resources/js/simple.js"></script> -->
<!-- 		        <script type="text/javascript" src="{$uriPrefix}/resources/js/home.js"></script> -->
<!-- 		        <script type="text/javascript" src="{$uriPrefix}/resources/js/toggle.js"></script> -->
<!-- 		        <script type="text/javascript" src="{$uriPrefix}/resources/js/menu.js"></script> -->
<!-- 		        <script type="text/javascript" src="{$uriPrefix}/resources/js/split.js"></script> -->
<!-- 		        <script type="text/javascript" src="{$uriPrefix}/resources/js/splittoggle.js"></script> -->
<!-- 		        <script type="text/javascript" src="{$uriPrefix}/resources/js/components.js"></script> -->
<!-- 		        <script type="text/javascript" src="{$uriPrefix}/resources/js/contextual.js"></script> -->
		        <script type="text/javascript" src="{$uriPrefix}/resources/js/base.js"></script>
            </head>
            <body>
            </body>
        </html>
    </xsl:template>
    
</xsl:stylesheet>
