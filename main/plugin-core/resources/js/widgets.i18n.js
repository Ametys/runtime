/*
 *  Copyright 2009 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**************
  * CALENDAR  * 
  *************/
var parent_window = parent != null ? parent : window; 
var parent_document = parent_window.document;

var runtime_window = window;
var runtime_document = runtime_window.document;

var runtime_callback;
var runtime_widgetAlreadyLoaded;
      
function runtime_loadCalendarScripts()
{
    if (runtime_widgetAlreadyLoaded == true)
      return;
      
    runtime_widgetAlreadyLoaded = true;
          
	parent_window.Tools.loadStyle(runtime_document, parent_window.getPluginResourcesUrl("core") + "/css/calendar/calendar.css");
	runtime_loadCalendarScripts_1();
}
function runtime_loadCalendarScripts_1()
{
	parent_window.Tools.loadScript(parent_document, parent_window.getPluginResourcesUrl("core") + "/js/calendar/calendar.js", runtime_loadCalendarScripts_2);
}
function runtime_loadCalendarScripts_2()
{
	parent_window.Tools.loadScript(parent_document, parent_window.getPluginResourcesUrl("core") + "/js/calendar/calendar-setup.js", runtime_loadCalendarScripts_3);
}
function runtime_loadCalendarScripts_3()
{
	parent_window.Tools.loadScript(parent_document, parent_window.getPluginResourcesUrl("core") + "/js/calendar/calendar.i18n.js", (parent_document != runtime_document) ? runtime_loadCalendarScripts_4 : runtime_initAllCalendar);
}
function runtime_loadCalendarScripts_4()
{
	parent_window.Tools.loadScript(runtime_document, parent_window.getPluginResourcesUrl("core") + "/js/calendar/calendar.js", runtime_loadCalendarScripts_5);
}
function runtime_loadCalendarScripts_5()
{
	parent_window.Tools.loadScript(runtime_document, parent_window.getPluginResourcesUrl("core") + "/js/calendar/calendar-setup.js", runtime_loadCalendarScripts_6);
}
function runtime_loadCalendarScripts_6()
{
	parent_window.Tools.loadScript(runtime_document, parent_window.getPluginResourcesUrl("core") + "/js/calendar/calendar.i18n.js", runtime_initAllCalendar);
}
function runtime_initAllCalendar ()
{
	var calendars = parent_window.STools.getChildrenByTagName(runtime_document.body, "dateMark");
	for (var i=0; i &lt; calendars.length; i++)
	{
		runtime_initCalendar(calendars[i].getAttribute("name"));
	}
	
	if (runtime_callback != null &amp;&amp; typeof runtime_callback == "function")
	{
		runtime_callback();
	}
}
runtime_loadCalendarScripts();

function runtime_initCalendar (name)
{
	runtime_window.Calendar.setup({
				inputField     :    name + "_date",
				ifFormat       :    "%Y-%m-%dT%H:%M",
				displayArea    :    name + "_span",
				daFormat       :    "%e %B %Y",
				showsTime      :    false,            
				button         :    name + "_img",
				singleClick    :    true,         
				timeFormat     :    "24",
				step           :    1             
				});	
	runtime_document.getElementById(name + "_img").src = parent_window.getPluginResourcesUrl("core") + "/img/widgets/calendar/calendar.gif";
	       
	runtime_update(name); 
}
function runtime_update(name)
{
	var input = runtime_document.getElementById(name + "_date");
      if (input.value != '')
      {			
      	var val = input.value;
      	runtime_document.getElementById(name + "_span").innerHTML = new runtime_window.Date(val.substring(0,4), val.substring(5,7)-1, val.substring(8, 10), val.substring(11, 13), val.substring(14, 16)).print("%e %B %Y");
      }
      else
      {
      	runtime_clearDate(name);
      }
}
function runtime_clearDate(id)
{
	runtime_document.getElementById(id + "_date").value = "";
	runtime_document.getElementById(id + "_span").innerHTML = "&lt;span style='color: gray; font-style: italic;'&gt;<i18n:text i18n:key="PLUGINS_CORE_WIDGET_DATE_MESSAGE_CLICKHERE"/>&lt;/span&gt;";
}

/*************
  * PASSWORD * 
  *************/
function runtime_password(id)
{
    var field2 = runtime_document.getElementById(id + '_password2');
    field2.focus();
}
function runtime_clearPassword(id)
{
    var input = runtime_document.getElementById(id);
    var field = runtime_document.getElementById(id + '_password');
    var field2 = runtime_document.getElementById(id + '_password2');
    var text2 = runtime_document.getElementById(id + '_password2_text');
    var reset = runtime_document.getElementById(id + '_password_reset');
    var image = runtime_document.getElementById(id + "_password_image");

    field.value = field.getAttribute("defaultValue");
    field2.value = "";
    field.setAttribute("typing", "false");
    input.value = '';
    
    if (field.value != '')
    {
      input.disabled = true;
      field.style.backgroundColor = "#ece9d8";
      field.style.borderColor = "#ece9d8";
      field.style.borderStyle = "solid";
      field2.style.display = "none";
      text2.style.display = "";
      reset.style.display = "none";
      image.style.display = "none";
    }
    else
    {
      input.disabled = false;
      field.style.backgroundColor = "";
      field.style.borderColor = "";
      field.style.borderStyle = "";
      field2.style.backgroundColor = "";
      field2.style.borderColor = "";
      field2.style.borderStyle = "";
      field2.style.display = "";
      text2.style.display = "none";
      image.style.display = "";
      reset.style.display = "none";
    }
}
function runtime_checkPassword(id)
{
    var input = runtime_document.getElementById(id);
    var field = runtime_document.getElementById(id + '_password');
    var field2 = runtime_document.getElementById(id + '_password2');

    if (field2.style.display == 'none')
    {
      return;
    }

    if (field2.value == field.value)
    {
      input.value = field.value;
    }
    else
    {
      alert("<i18n:text i18n:key="PLUGINS_CORE_WIDGET_PASSWORD_ERROR"/>")
      field.value = "";
      field2.value = "";
      field.focus();
    }
}
function runtime_passwordType(id)
{
    var field = runtime_document.getElementById(id + '_password');

    if (field.getAttribute("typing") == "true")
      return;

    var input = runtime_document.getElementById(id);
    var field2 = runtime_document.getElementById(id + '_password2');
    var text2 = runtime_document.getElementById(id + '_password2_text');
    var reset = runtime_document.getElementById(id + '_password_reset');
    var image = runtime_document.getElementById(id + "_password_image");
      
    field.setAttribute("typing", "true");
    field.value = "";
    field.style.backgroundColor = "";
    field.style.borderColor = "";
    field.style.borderStyle = "";
    field2.value = "";
    field2.style.backgroundColor = "";
    field2.style.borderColor = "";
    field2.style.borderStyle = "";
    field2.style.display = "";
    text2.style.display = "none";
    image.style.display = "";
    input.disabled = false;
    
    if (field.getAttribute("defaultValue") != '')
    {
      reset.style.display = "";
    }
}
function runtime_initPassword(id)
{
    var input = runtime_document.getElementById(id);
    var field = runtime_document.getElementById(id + '_password');
    var field2 = runtime_document.getElementById(id + '_password2');

    field.setAttribute("defaultValue", input.value);
    field.value = '';
    field2.value = '';
    runtime_clearPassword(id);
}
function runtime_initAllPassword()
{
  var passwords = parent_window.STools.getChildrenByTagName(runtime_document.body, "passwordMark");
  for (var i=0; i &lt; passwords.length; i++)
  {
    var name = passwords[i].getAttribute("name");

    var image = runtime_document.getElementById(name + "_password_image");
      image.src =  parent_window.getPluginResourcesUrl("core") + "/img/widgets/repeat_password.gif";

    runtime_initPassword(name);
  }
}
runtime_initAllPassword();

/*************
  * BOOLEAN  * 
  ************/
function runtime_booleanChange(checkbox, input)
{
	input.value = (checkbox.checked) ? "true" : "false";
}			

/*************
  * INTEGER  * 
  ************/
function runtime_isIntNumber(value)
{
	return !(isNaN(parseInt(value)) || ((parseInt(value) + "") != (value + "")));
}

function runtime_verifyIntNumber(input)
{
	if (input.value == '')
		return true;	

	if (!runtime_isIntNumber(input.value))
	{
		return false;
	}

	return true;
}

function runtime_setIntNumber(input)
{
	if (input.value == '')
		return;	

	if (!runtime_isIntNumber(input.value))
	{
		if (isNaN(parseInt(input.value)))
			input.value = ''
		else
			input.value = parseInt(input.value)
	}
}
		
/***********
  * DOUBLE * 
  ***********/
function runtime_isDbleNumber(value)
{
	return !(isNaN(parseFloat(value)) || ((parseFloat(value)) != (value)));
}

function runtime_verifyDbleNumber(input)
{
	if (input.value == '')
		return true;	

	if (!runtime_isDbleNumber(input.value))
	{
		return false;
	}

	return true;
}

function runtime_setDbleNumber(input)
{
	if (input.value == '')
		return;	

	if (!runtime_isDbleNumber(input.value))
	{
		if (isNaN(parseFloat(input.value)))
			input.value = ''
		else
			input.value = parseFloat(input.value)
	}
}