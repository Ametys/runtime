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
<!-- 
  This sample of js will be xmlized so you cannot use xml specialcaracters 
  This comment will not be part of the output
  The url to call this file is 'resources/js/sample.i18n.js'
  
  I18n namespace will be automatically added so you can use i18n tags
  and default catalogue will be the plugin one
-->

function myI18nFunction()
{
  alert("<i18n:text i18n:key="PLUGINS_PLUGIN_MYJAVASCRIPTKEY"/>");
  
  var test = true;
  for (var i = 0; i &lt; 5 &amp;&amp; test; i++)
  {
    test = i == 3;
  }
}