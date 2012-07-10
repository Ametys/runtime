<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2009 Anyware Services

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
                xmlns:ex="http://apache.org/cocoon/exception/1.0" 
                exclude-result-prefixes="ex">

    <xsl:import href="../home/home.xsl"/>
    
    <xsl:param name="pageTitle">An error has occurred</xsl:param>
    <xsl:param name="contextPath" />
    <xsl:param name="realpath" />
    <xsl:param name="code" />

    <xsl:variable name="backslashedRealpath" select="translate($realpath, '\', '/')" />
    

    <xsl:template match="/ex:exception-report">
    	<xsl:call-template name="home">
		    <xsl:with-param name="needs-kernel-ui" select="false()"/>
		    <xsl:with-param name="context-path" select="$contextPath"/>
		    
		    <xsl:with-param name="head-title">
			    <xsl:value-of select="$pageTitle" />
			</xsl:with-param>
			
		    <xsl:with-param name="head-meta">
		    	<link rel="stylesheet" href="{$contextPath}/kernel/resources/css/error.css" type="text/css"/>
				<script type="text/javascript">
					<xsl:comment>
						function toggleDetails(start)
						{
							var button = document.getElementById('details-button');
							var text = document.getElementById('details-place');
							
							if (text.style.display == 'none' &amp;&amp; start !== true)
							{
								button.innerHTML = 'Hide details ';
								text.style.display = '';
								document.body.childNodes[0].style.height = "";
							}
							else
							{
								button.href = "javascript: toggleDetails();";
								button.innerHTML = 'Show details';
								text.style.display = 'none';
								document.body.childNodes[0].style.height = "auto";
							}
						}
					</xsl:comment>
				</script>
		    </xsl:with-param>

		    <xsl:with-param name="body-col-main">
		    	<table class="error">
		    		<tr>
		    			<td>
							<h1>
								<xsl:value-of select="$pageTitle" />
							</h1>
			
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
								
							<p class="details">
								<a id="details-button"></a>
							</p>
		    			</td>
		    		</tr>
		    		<tr class="details">
		    			<td>
							<div class="details-place" id="details-place">
								<script type="text/javascript">
                                    <xsl:comment>
                                        toggleDetails(true);
                                    </xsl:comment>
								</script>
								<p>
									<xsl:value-of select="/" />
								</p>
							</div>
		    			</td>
		    		</tr>
		    	</table>
		    </xsl:with-param>
    	</xsl:call-template>
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
