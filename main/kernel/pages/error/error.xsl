<?xml version="1.0" encoding="UTF-8"?>
<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http://www.opensource.org/licenses/eclipse-1.0.php
    | 
    | Contributors:
    |     Anyware Technologies - initial API and implementation
    +-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ex="http://apache.org/cocoon/exception/1.0">

	<xsl:param name="pageTitle">An error has occurred</xsl:param>
	<xsl:param name="contextPath"/>
	<xsl:param name="realpath"/>
	<xsl:param name="code"/>

    <xsl:variable name="backslashedRealpath" select="translate($realpath, '\', '/')"/>

	<xsl:template match="/ex:exception-report">
		<HTML>
			<!-- ****** HEAD ****** -->
			<HEAD> 
				<TITLE>
					<xsl:value-of select="$pageTitle"/>
				</TITLE>  
				
				<META http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
				
				<LINK rel="stylesheet" href="{$contextPath}/kernel/resources/css/error/error.css" type="text/css"/>
				<style>
			          h1 { 
			          		font-size: 200%; 
			          		color: #336699; 
			          		text-align: left; 
			          		margin: 0px 0px 30px 0px; 
			          		padding: 0px; 
			          		border-width: 0px 0px 1px 0px; 
			          		border-style: solid; 
			          		border-color: #336699;
			          }
          			  p.message { 
                            margin: 10px;
          			  		padding: 10px 30px 10px 30px; 
          			  		font-weight: bold; 
          			  		font-size: 110%; 
          			  		border-width: 1px; 
          			  		border-style: dashed; 
          			  		border-color: #336699; 
          			  }
				</style>
			</HEAD>
		
			<!-- ****** BODY ****** -->
			<BODY>
				<TABLE class="error_head" style="background-image: url({$contextPath}/kernel/resources/img/bg_top.gif);">
					<!-- En-tête -->
					<TR height="119px">
						<TD width="275px">
							<IMG src="{$contextPath}/kernel/resources/img/runtime.jpg"/>
						</TD>
						<TD class="error_head">
							<span class="error_head"><xsl:value-of select="$pageTitle"/> [ERROR]</span>
						</TD>
					</TR>
					<!-- Contenu -->
					<TR>
						<TD colspan="2" class="error_main_area">
						
							<xsl:if test="$code = '500'">
						
						        <p class="message">
                                    <xsl:if test="@class"><xsl:value-of select="@class"/></xsl:if>
                                    <xsl:if test="string-length (ex:message) != 0">
                                       <xsl:if test="@class"> : </xsl:if><xsl:value-of select="ex:message"/>
        						          <xsl:if test="ex:location">
        						             <br/><span style="font-weight: normal"><xsl:apply-templates select="ex:location"/></span>
        						          </xsl:if>
                                    </xsl:if>
						        </p>
						        
						        <xsl:comment>
						        	<xsl:copy-of select="/"/>
						        </xsl:comment>
						        
						    </xsl:if>
						
						</TD>
					</TR>
				</TABLE>
			</BODY>
		</HTML>		
	</xsl:template>

  <xsl:template match="ex:location">
   <xsl:if test="string-length(.) > 0">
     <em><xsl:value-of select="."/></em>
     <xsl:text> - </xsl:text>
   </xsl:if>
   <xsl:call-template name="print-location"/>
  </xsl:template>
  
  <xsl:template name="print-location">
     <xsl:choose>
       <xsl:when test="contains(@uri, $backslashedRealpath)">
            <xsl:text>context:/</xsl:text>
            <xsl:value-of select="substring-after(@uri, $backslashedRealpath)"/>
       </xsl:when>
       <xsl:otherwise>
            <xsl:value-of select="@uri"/>
       </xsl:otherwise>
      </xsl:choose>
      
      <xsl:text> - </xsl:text>
      
      <xsl:value-of select="@line"/>:<xsl:value-of select="@column"/>
  </xsl:template>
</xsl:stylesheet>