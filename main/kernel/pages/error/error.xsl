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
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                exclude-result-prefixes="ex">

    <xsl:param name="pageTitle">An error has occurred</xsl:param>
    <xsl:param name="contextPath" />
    <xsl:param name="realpath" />
    <xsl:param name="code" />

    <xsl:variable name="backslashedRealpath" select="translate($realpath, '\', '/')" />

	<xsl:template match="/ex:exception-report">
		<html>
			<!-- ****** HEAD ****** -->
			<head> 
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                    <xsl:value-of select="$pageTitle" />
                </title>

                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/error/error.css" type="text/css" />
                <link rel="stylesheet" href="{$contextPath}/kernel/resources/css/homepage/view.css" type="text/css" />

                <link rel="icon" type="image/gif" href="{$contextPath}/kernel/resources/img/runtime_favico.gif" />
                <link rel="shortcut icon" type="image/x-icon" href="{$contextPath}/kernel/resources/img/runtime_favico.ico" />
		        
                <xsl:comment>[if lt IE 7]&gt;
						&lt;script defer="defer" type="text/javascript" src="_admin/resources/js/pngfix.js">&lt;/script&gt;
				&lt;![endif]</xsl:comment>
				
				<script>
					<xsl:comment>
						function toggleDetails()
						{
							var button = document.getElementById('details-button');
							var text = document.getElementById('details-place');
							
							if (text.style.display == 'none')
							{
								button.innerHTML = 'Hide details ';
								text.style.display = '';
							}
							else
							{
								button.innerHTML = 'Show details';
								text.style.display = 'none';
							}
						}
					</xsl:comment>
				</script>
			</head>
		
			<!-- ****** BODY ****** -->
			<body>
		     	<div id="wrapper" class="" style="width: 930px; margin-right: auto; margin-left: auto;">
		     		<div id="content_left" style="width: 44px;"><xsl:comment></xsl:comment></div>
		     		
		     		<div id="content_center" style="width: 930px;">
		     			<div id="top" style="height: 90px; width: 930px;">
							<div id="logo"><xsl:comment></xsl:comment></div>
						</div>
						
						<div id="main">
							<div id="top-panel" class="top-panel"> 

								<div class="title">
	 								<xsl:value-of select="$pageTitle" />
	 							</div>
 
								<p class="message">
									<xsl:if test="@class">
										<xsl:value-of select="@class" />
									</xsl:if>
									<xsl:if test="string-length (ex:message) != 0">
										<xsl:if test="@class">
											:
										</xsl:if>
										<xsl:value-of select="ex:message" />
										<xsl:if test="ex:location">
											<br />
											<span style="font-weight: normal">
												<xsl:apply-templates select="ex:location" />
											</span>
										</xsl:if>
									</xsl:if>
								</p>
									
								<div class="details"><a id="details-button" href="javascript: toggleDetails();">Show details</a></div>
								<div class="details-place" id="details-place" style="display: none;">
									<xsl:value-of select="/" />
								</div>
							</div>
						</div>
						
						<div id="footer" style="height: 35px; width: 930px;">
		     			</div>
		     		</div>
		     	</div>
		     	<div id="column-right" style="width: 100%;"/>			
			</body>
		</html>		
	</xsl:template>

    <xsl:template match="ex:location">
        <xsl:if test="string-length(.) > 0">
            <em>
                <xsl:value-of select="." />
            </em>
            <xsl:text> - </xsl:text>
        </xsl:if>
        <xsl:call-template name="print-location" />
    </xsl:template>

    <xsl:template name="print-location">
        <xsl:choose>
            <xsl:when test="contains(@uri, $backslashedRealpath)">
                <xsl:text>context:/</xsl:text>
                <xsl:value-of select="substring-after(@uri, $backslashedRealpath)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@uri" />
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text> - </xsl:text><xsl:value-of select="@line" /> : <xsl:value-of select="@column" />
    </xsl:template>
</xsl:stylesheet>
