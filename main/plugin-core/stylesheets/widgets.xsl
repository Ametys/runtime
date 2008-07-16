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
	
	<xsl:template name="string-input">
		<xsl:param name="class"/>
        <xsl:param name="style"/>
		<xsl:param name="size">30</xsl:param>
		<xsl:param name="value"/>
		<xsl:param name="name"/>
		<xsl:param name="id"><xsl:value-of select="$name"/></xsl:param>
		<xsl:param name="disabled"/>
		
		<input id="{$id}" style="{$style}" class="{$class}" type="text" value="{$value}" name="{$name}" size="{$size}">
			<xsl:if test="$disabled=true()"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
		</input>
	</xsl:template>
	
    <xsl:template name="password-input">
        <xsl:param name="class"/>
        <xsl:param name="style"/>
        <xsl:param name="styleText"/>
        <xsl:param name="styleTable"/>
        <xsl:param name="size">30</xsl:param>
        <xsl:param name="value"/>
        <xsl:param name="name"/>
        <xsl:param name="id"><xsl:value-of select="$name"/></xsl:param>
        <xsl:param name="disabled"/>
        
        <xsl:choose>
            <xsl:when test="$disabled=true()">
                <input id="{$id}" disabled="disabled" style="{$style}" class="{$class}" type="password" value="{$value}" name="{$name}" size="{$size}"/>
            </xsl:when>
            <xsl:otherwise>
                <passwordMark name="{$name}"/>
                <table cellspacing="0" cellpadding="0" border="0" style="{$styleTable}">
                    <tr>
                        <td>
                            <input id="{$id}_password" style="{$style};" class="{$class}" type="password" size="{$size}" onblur="runtime_password('{$id}');" onfocus="runtime_passwordType('{$id}');"/>
                            <input id="{$id}" disabled="true" name="{$name}" type="hidden" value="{$value}"/>
                        </td>
                        <td rowspan="2" width="6px" style="vertical-align: middle">
                            <img id="{$id}_password_image" border="0" style="margin: 0px; padding: 0px; margin-left: 1px"/>
                        </td>
                        <td>
                            <span id="{$id}_password_reset" i18n:attr="title" title="plugin.core:PLUGINS_CORE_WIDGET_PASSWORD_KEEPOLD" onclick="runtime_clearPassword('{$id}'); return false;" style="margin: 0px; margin-left: 2px; padding: 0px; cursor: pointer;">X</span>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input id="{$id}_password2" style="{$style};" class="{$class}" type="password" onblur="runtime_checkPassword('{$id}')"/>
                            <div id="{$id}_password2_text" style="{$styleText}; cursor: pointer;" onclick="document.getElementById('{$id}_password').focus()"><i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_HINT" i18n:catalogue="plugin.core"/></div>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
	<xsl:template name="long-input">
		<xsl:param name="class"/>
        <xsl:param name="style"/>
		<xsl:param name="size">30</xsl:param>
		<xsl:param name="value"/>
		<xsl:param name="name"/>
        <xsl:param name="id"><xsl:value-of select="$name"/></xsl:param>
		<xsl:param name="disabled"/>
		
		<input id="{$id}" class="{$class}" style="{$style}" maxlength="12" onkeyup="runtime_setIntNumber(this)" onchange="return runtime_verifyIntNumber(this)" type="text" value="{$value}" name="{$name}" size="{$size}">
			<xsl:if test="$disabled=true()"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
		</input>
	</xsl:template>
	
	<xsl:template name="double-input">
		<xsl:param name="class"/>
        <xsl:param name="style"/>
		<xsl:param name="size">30</xsl:param>
		<xsl:param name="value"/>
		<xsl:param name="name"/>
        <xsl:param name="id"><xsl:value-of select="$name"/></xsl:param>
		<xsl:param name="disabled"/>
		
		<input id="{$id}" class="{$class}" style="{$style}" maxlength="20" onkeyup="runtime_setDbleNumber(this)" onchange="return runtime_verifyDbleNumber(this)" type="text" value="{$value}" name="{$name}" size="{$size}">
			<xsl:if test="$disabled=true()"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
		</input>
	</xsl:template>
	
	<xsl:template name="boolean-input">
		<xsl:param name="class"/>
        <xsl:param name="style"/>
		<xsl:param name="value">false</xsl:param>
		<xsl:param name="name"/>
		<xsl:param name="disabled"/>		
		<xsl:param name="uniqueId" select="generate-id()"/>

		<input class="{$class}" style="{$style}" type="checkbox" onchange="runtime_booleanChange(this, document.getElementById('boolean_{$uniqueId}'))" id="boolean_{$uniqueId}_checkbox">
			<xsl:if test="$value='true'"><xsl:attribute name="checked">checked</xsl:attribute></xsl:if>
			<xsl:if test="$disabled=true()"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
		</input>
		<input type="hidden" name="{$name}" value="$value" id="boolean_{$uniqueId}">
			<xsl:attribute name="value"><xsl:choose><xsl:when test="$value = 'true'">true</xsl:when><xsl:otherwise>false</xsl:otherwise></xsl:choose></xsl:attribute>
		</input>
	</xsl:template>
	
	<xsl:template name="calendar-input">
		<xsl:param name="name"/>
		<xsl:param name="value"/>
		<xsl:param name="disabled"/>
		<xsl:param name="width">180px</xsl:param>

		<dateMark name="{$name}"/>
		<table style="margin-top: 4px;" cellspacing="0" cellpadding="0">
			<tr>
				<td style="text-align: left; width: {$width}; white-space: nowrap;" id="{$name}_span"></td>
				<xsl:if test="$disabled != true()">
					<td style="text-align: right; white-space: nowrap; width: 32px; padding: 0px">
						<input name="{$name}" id="{$name}_date" type="hidden" readonly="1" value="{$value}"/>
						<img id="{$name}_img" style="cursor: pointer;"/>
						<span onclick="runtime_clearDate('{$name}'); return false;" style="margin: 0px; margin-left: 2px; padding: 0px; cursor: pointer;">X</span>
					</td>
				</xsl:if>
			</tr>
		</table>
	</xsl:template>
	
	<xsl:template name="select-input">
		<xsl:param name="class"/>
        <xsl:param name="style"/>
		<xsl:param name="values"/>
		<xsl:param name="value"/>
		<xsl:param name="name"/>
		<xsl:param name="disabled"/>
		
		<select name="{$name}" id="{$name}" class="{$class}" style="{$style}">
			<xsl:if test="$disabled=true()"><xsl:attribute name="disabled">disabled</xsl:attribute></xsl:if>
			
			<xsl:for-each select="$values/option">
				<option value="{@value}"><xsl:if test="$value=@value"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if><xsl:copy-of select="node()"/></option>
			</xsl:for-each>
		</select>
	</xsl:template>
	
</xsl:stylesheet>