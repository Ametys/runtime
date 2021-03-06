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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:import href="plugin:core-ui://stylesheets/workspace.xsl"/>

    <xsl:template name="ui-extension-load">
            <!-- CSS -->
            <link rel="stylesheet" type="text/css" href="{$workspace-resources}/css/workspace.css" />
            
            <!-- JS -->
            <script type="text/javascript" src="{$workspace-resources}/js/workspace.js" />
    </xsl:template>
    
</xsl:stylesheet>
