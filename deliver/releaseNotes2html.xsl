<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="version">v2_1_0</xsl:param>

    <xsl:template match="releases">
        <html>
          <head>
            <title>Release notes <xsl:value-of select="$version"/></title>
          </head>
          <body>
              <hr size="2" width="100%"/>               
              <table border="0" cellpadding="2" cellspacing="2" width="100%" style="background-image: url(img/bg_top.gif); background-repeat: repeat-x; background-position: top; background-attachment: fixed;">
              	<tbody>
              		<tr>
              			<td align="justify" valign="top">
                            &#160;
                            <img src="../img/runtime.jpg" alt="Logo Ametys"/>
                            <br/>
                        </td>
                        <td align="center" valign="middle">
                          <font color="#0e3787"><big><big><b>
                              Ametys Runtime <xsl:value-of select="$version"/> - Release notes
                          </b></big></big></font><br/>
                        </td>
                        <td align="right" valign="top">
                            	&#160;<img src="../img/runtime.jpg" alt="Logo Ametys"/>
                        </td>
              		</tr>
              	</tbody>               
              </table>
                                       
              <hr size="2" width="100%"/><br/>

            <xsl:apply-templates select="release[@name=$version]"/>
          </body>
        </html>
    </xsl:template>
    
    <xsl:template match="release">
        <table cellspacing="0" cellpadding="0" align="center" style="border: 2px solid #cfcfcf">
            <tr bgcolor="#a0a0ff" color="black">
                <th style="border-right: 2px solid #cfcfcf;">Module</th>
                <th style="border-right: 2px solid #cfcfcf;">Type</th>
                <th>Libellé</th>
            </tr>
            <xsl:apply-templates select="note">
                <xsl:sort select="@module"/>
                <xsl:sort select="@type"/>
            </xsl:apply-templates>
        </table>
    </xsl:template>
    
    <xsl:template match="note">
        <xsl:variable name="style">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">background-color: #dfdfdf</xsl:when>
            <xsl:otherwise>background-color: #efefef</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <tr>
            <td style="font-weight: bold; border-right: 2px solid #cfcfcf; padding-left: 3px; padding-right: 10px; {$style}"><xsl:value-of select="@module"/></td>
            <td style="border-right: 2px solid #cfcfcf; text-align: center; padding-left: 5px; padding-right: 5px; {$style}"><xsl:value-of select="@type"/></td>
            <td style="padding-left: 10px; padding-right: 3px; {$style}"><xsl:value-of select="."/></td>
        </tr>
    </xsl:template>
</xsl:stylesheet>
