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
                
    <xsl:template match="/">
        <html>
            <xsl:comment>
                 Just for Microsoft native error pages 
                Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Quisque scelerisque. 
                Donec euismod. Mauris eget pede es vel est tristique adipiscing. Integer in sem 
                a enim molestie elementum. Curabitur elementum, nisl eu dapibus dapibus, libero 
                tortor tincidunt quam, in facilisis arcu lacus ut diam. Nam ultricies felis sed 
                felis. Integer interdum. Quisque iaculis porttitor magna. Fusce aliquet. Nulla 
                felis dui, pulvinar at, rutrum in, fermentum eu, metus. Praesent purus nunc, 
                porta vel, sodales id, fermentum eget, massa.
            </xsl:comment>
            <body>
                <iframe style="position: absolute; top: -1000px; left: -1000px; width: 1px; height: 1px;" src="{ametys:uriPrefix()}/plugins/core-impl/userpopulations/credentialproviders/kerberos/skip?foo={substring-after(math:random(), '.')}"/>
            </body>
        </html>
    </xsl:template> 
                
</xsl:stylesheet>    