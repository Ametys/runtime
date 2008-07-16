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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

    <xsl:import href="plugin:core://stylesheets/widgets.xsl"/>

    <xsl:param name="contextPath"/>
    <xsl:param name="pluginName"/>

	<xsl:template match="/Model">
	   <xml>
	       <table id="RUNTIME_Plugin_Runtime_EditUser" style="display: none">
		      <tr>
			     <td class="dialog">
				    <form name="dialog_edituser-form" target="_top" method="POST" style="padding: 0px; margin: 0px;">
                        <input type="hidden" name="mode" id="mode"/>
				        <table id="innertable" cellspacing="0" cellpadding="0" width="100%">
                            <colgroup>
                                <col width="130px"/>
                                <col width="20px"/>
                                <col width="170px"/>
                            </colgroup>
                            
                            
                            <xsl:for-each select="*">
                                <tr>
                                    <td valign="top" id="fieldlabel_{local-name(.)}" style="cursor: default; padding-top: 2px;" i18n:attr="title" title="plugin.{@plugin}:{description}"><i18n:text i18n:key="{label}" i18n:catalogue="plugin.{@plugin}"/></td>
                                    <td valign="top" style="padding-top: 2px;"><img src="{$contextPath}/plugins/{$pluginName}/resources/img/users/help.gif" i18n:attr="title" title="plugin.{@plugin}:{description}"/></td>
                                    <td>
                                            <xsl:attribute name="style">height: <xsl:choose><xsl:when test="type='password'">40px;</xsl:when><xsl:otherwise>20px;</xsl:otherwise></xsl:choose></xsl:attribute>
                                    
                                            <xsl:variable name="style">font-family: verdana; font-size: 10px; width: 167px;</xsl:variable>
                                    
                                            <xsl:choose>
                                                <xsl:when test="enumeration">
                                                    <xsl:call-template name="select-i18n-input">
                                                        <xsl:with-param name="name" select="concat('field_', local-name(.))"/>
                                                        <xsl:with-param name="values" select="enumeration"/>
                                                        <xsl:with-param name="value" select="value"/>
                                                        <xsl:with-param name="style" select="$style"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                
                                                <xsl:when test="type='double'">
                                                    <xsl:call-template name="double-input">
                                                        <xsl:with-param name="name" select="concat('field_', local-name(.))"/>
                                                        <xsl:with-param name="value" select="value"/>
                                                        <xsl:with-param name="style" select="$style"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                <xsl:when test="type='boolean'">
                                                    <xsl:call-template name="boolean-input">
                                                        <xsl:with-param name="name" select="concat('field_', local-name(.))"/>
                                                        <xsl:with-param name="value" select="value"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                <xsl:when test="type='date'">
                                                    <xsl:call-template name="calendar-input">
                                                        <xsl:with-param name="name" select="concat('field_', local-name(.))"/>
                                                        <xsl:with-param name="value" select="value"/>
                                                        <xsl:with-param name="style" select="$style"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                <xsl:when test="type='long'">
                                                    <xsl:call-template name="long-input">
                                                        <xsl:with-param name="name" select="concat('field_', local-name(.))"/>
                                                        <xsl:with-param name="value" select="value"/>
                                                        <xsl:with-param name="style" select="$style"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                <xsl:when test="type='password'">
                                                    <xsl:call-template name="password-input">
                                                        <xsl:with-param name="name" select="concat('field_', local-name(.))"/>
                                                        <xsl:with-param name="value" select="value"/>
                                                        <xsl:with-param name="style" select="concat($style, '; width: 150px;')"/>
                                                        <xsl:with-param name="styleTable" select="'height: 40px'"/>
                                                        <xsl:with-param name="styleText" select="'font-family: verdana; font-size: 9px; font-style: italic; height: 20px'"/>
                                                    </xsl:call-template>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:call-template name="string-input">
                                                        <xsl:with-param name="name" select="concat('field_', local-name(.))"/>
                                                        <xsl:with-param name="value" select="value"/>
                                                        <xsl:with-param name="style" select="$style"/>
                                                    </xsl:call-template>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                                            
                                    </td>
                                </tr>
                            
                            </xsl:for-each>
                            
        					<!-- **************** -->
        					<tr>
        						<td colspan="3" style="padding-top: 10px; text-align: center; vertical-align: bottom; ">
        							<button onclick="parent.RUNTIME_Plugin_Runtime_EditUser.ok(); return false;" style="width: 80px">
        								<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_OK"/>
        							</button>
        							&#160;&#160;
        							<button onclick="parent.RUNTIME_Plugin_Runtime_EditUser.cancel(); return false;" style="width: 80px">
        								<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_CANCEL"/>
        							</button>
        						</td>
        					</tr>
				        </table>
				    </form>
			      </td>
			  </tr>
		   </table>
		</xml>
	</xsl:template>
    
</xsl:stylesheet>