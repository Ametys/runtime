<?xml version="1.0" encoding="UTF-8"?>
<!--
   Copyright 2012 Anyware Services

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
    xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
    xmlns:exslt="http://exslt.org/common"
    xmlns:ametys="org.ametys.core.util.AmetysXSLTHelper">
	
	<!-- +
	     | @private
	     | Add the javascript code that checks the current browser version
	 	 + -->
	<xsl:template name="kernel-browsers">
		<xsl:param name="authorized-browsers"/>
        
        <xsl:variable name="context-path" select="ametys:uriPrefix(false)"/>

			<script type="text/javascript">
				<xsl:if test="$authorized-browsers != ''">
				// Test for authorized browsers
				window.ametys_authorized_browsers = <xsl:value-of select="$authorized-browsers"/>;
				{
					function _serializeSupportedBrowsers()
					{
						var s = "{";
						
						for (var i in window.ametys_authorized_browsers['supported'])
						{
							if (typeof(i) == 'string')
							{
								s += "'" + i + "': '" + window.ametys_authorized_browsers['supported'][i] + "',";
							}
						}
						
						s = s.substring(0, s.length - 1);
						s += "}";
						
						return s;
					}
					
					function _getSupportValue(supportedVersions, unsupportedVersions, version)
					{
						function versionMatch(list, version)
						{
							var t = list.indexOf('-');
							if (t === -1)
							{
								// single version
								return (version === parseFloat(list))
							}
							else
							{
								var inf = parseFloat(list.substring(0, t));
								var sup = parseFloat(list.substring(t+1));
								
								return version &gt;= inf &amp;&amp; (version &lt;= sup || sup === 0);
							}
						}
						
						if (supportedVersions !== undefined &amp;&amp; versionMatch(supportedVersions, version))
						{
							return true;
						}
						else if (unsupportedVersions !== undefined &amp;&amp; versionMatch(unsupportedVersions, version))
						{
							return false;
						} 
						else
						{
							return undefined;
						}
					}
	
				    // get the user agent
					var useragent = navigator.userAgent.toLowerCase();
					
					// determine browser family and version
					var browser, browserVersion;
					
					if (/edge\/([0-9.]+)/.test(useragent))
					{
                        browser = 'ed';
                        browserVersion = RegExp.$1;
					}
					else if (/compatible; msie ([0-9.]+);/.test(useragent) || /trident.*rv:([0-9.]+)/.test(useragent))
					{
						browser = 'ie';
						browserVersion = RegExp.$1;
					}
					else if (/ firefox\/([0-9.]+)( |$)/.test(useragent))
					{
						browser = 'ff';
						browserVersion = RegExp.$1;
					}
					else if (/ chrome\/([0-9]+\.[0-9]+)\./.test(useragent))
					{
						browser = 'ch';
						browserVersion = RegExp.$1;
					}
					else if (/ version\/([0-9.]+) safari\//.test(useragent))
					{
						browser = 'sa';
						browserVersion = RegExp.$1;
					}
					else if (/opera\/9\.80 .* version\/([0-9.]+)/.test(useragent) || /opera\/([0-9.]+) /.test(useragent))
					{
						browser = 'op';
						browserVersion = RegExp.$1;
					}

					var supported = _getSupportValue(window.ametys_authorized_browsers['supported'][browser], window.ametys_authorized_browsers['not-supported'][browser], parseFloat(browserVersion));
					switch (supported)
					{
						case true: // supported
						case undefined: // unknown
					    	break;
						case false: // not supported
					    	document.location.href = "<xsl:value-of select="$context-path"/>" + window.ametys_authorized_browsers['failure-redirection'] + "?uri=" + encodeURIComponent(window.location.href) + "&amp;supported=" + encodeURIComponent(_serializeSupportedBrowsers()) + "&amp;browser=" + browser + "&amp;browserversion=" + browserVersion;
					    	throw "Unsupported browser"
				    }

				    window.ametys_authorized_browsers = undefined;
				}			
				</xsl:if>
	        </script>
	</xsl:template>
    
</xsl:stylesheet>
