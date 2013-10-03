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

<!-- 
  This sample of js will be xmlized so you cannot use xml special characters 
  This comment will not be part of the output
  The url to call this file is 'resources/js/sample.i18n.js'
  
  I18n namespace will be automatically added so you can use i18n tags
  and default catalogue will be the plugin one
-->

function myI18nFunction()
{
  alert("<i18n:text i18n:key='PLUGINS_PLUGIN_MYJAVASCRIPTKEY'/>");
  
  var test = true;
  for (var i = 0; i < 5 && test; i++)
  {
    test = i == 3;
  }
}