<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2013 Anyware Services

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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="todevmode">true</xsl:param>

    <xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

    <!-- CHANGE CLASSES OUTPUT DIRECTORY -->	
	<xsl:template match="classpathentry[@kind='output' and $todevmode='true']">
	   <classpathentry kind="output" path="templates/application/WEB-INF/classes"/>
	</xsl:template>
    <xsl:template match="classpathentry[@kind='output' and $todevmode='false']">
       <classpathentry kind="output" path="bin"/>
    </xsl:template>
    
    <!-- CHANGES IVYREP CONFIGURATION -->
    <xsl:template match="classpathentry[@kind='con' and starts-with(@path, 'org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER') and count(/classpath/classpathentry[@kind='con' and starts-with(@path, 'org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER')]) = 1 and $todevmode='true']">
        <xsl:variable name="path1">
             <xsl:call-template name="change-parameter">
                 <xsl:with-param name="string" select="@path"/>
                 <xsl:with-param name="parameter" select="'confs'"/>
                 <xsl:with-param name="new-value" select="'compile_dependencies%2Ctest'"/>
             </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="path2">
             <xsl:call-template name="change-parameter">
                 <xsl:with-param name="string" select="@path"/>
                 <xsl:with-param name="parameter" select="'confs'"/>
                 <xsl:with-param name="new-value" select="'runtime_dependencies'"/>
             </xsl:call-template>
             <xsl:text>&amp;acceptedTypes=jar%2Cbundle%2Cejb%2Cmaven-plugin</xsl:text>
             <xsl:text>&amp;alphaOrder=true</xsl:text>
             <xsl:text>&amp;resolveInWorkspace=false</xsl:text>
             <xsl:text>&amp;transitiveResolve=true</xsl:text>
             <xsl:text>&amp;readOSGiMetadata=false</xsl:text>
             <xsl:text>&amp;retrievedClasspath=true</xsl:text>
             <xsl:text>&amp;retrievedClasspathPattern=templates%2Fapplication%2FWEB-INF%2Flib%2F%5Bartifact%5D-%5Brevision%5D.%5Bext%5D</xsl:text>
             <xsl:text>&amp;retrievedClasspathSync=true</xsl:text>
             <xsl:text>&amp;retrievedClasspathTypes=jar%2Cbundle%2Clib</xsl:text>
        </xsl:variable>
    
    
       <classpathentry kind="con" path="{$path1}"/><xsl:text>
    </xsl:text><classpathentry kind="con" path="{$path2}"/>
    </xsl:template>
    <xsl:template match="classpathentry[@kind='con' and starts-with(@path, 'org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER') and count(/classpath/classpathentry[@kind='con' and starts-with(@path, 'org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER')]) = 2 and $todevmode='false']">
        <xsl:if test="count(following-sibling::classpathentry[@kind='con' and starts-with(@path, 'org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER')]) = 0">
            <xsl:variable name="pathWithIvyPathModifiedAndConf">
             <xsl:call-template name="change-parameter">
                 <xsl:with-param name="string" select="@path"/>
                 <xsl:with-param name="parameter" select="'confs'"/>
                 <xsl:with-param name="new-value" select="'compile%2Ctest'"/>
             </xsl:call-template>
           </xsl:variable>
           <xsl:variable name="path" select="substring-before($pathWithIvyPathModifiedAndConf, '&amp;acceptedTypes')"/>
	
	       <classpathentry kind="con" path="{$path}"/>
	   </xsl:if>
    </xsl:template>
    
    <xsl:template name="change-parameter">
        <xsl:param name="string"/>
        <xsl:param name="parameter"/>
        <xsl:param name="new-value"/>
        
        <xsl:variable name="begin" select="substring-before($string, $parameter)"/>
        
        <xsl:variable name="end" select="substring-after(substring-after($string, $parameter), '&amp;')"/>
        
        
        <xsl:value-of select="$begin"/><xsl:value-of select="$parameter"/>=<xsl:value-of select="$new-value"/>&amp;<xsl:value-of select="$end"/>
    </xsl:template>
</xsl:stylesheet>
