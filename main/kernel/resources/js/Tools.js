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

function Tools()
{
}
Tools.agt = navigator.userAgent.toLowerCase();
Tools.is_ie = ((Tools.agt.indexOf("msie") != -1) && (Tools.agt.indexOf("opera") == -1));
Tools.xmlTextContent = Tools.is_ie ? "text" : "textContent";

Tools.create = function()
{
	var xml;
	if ( typeof XMLHttpRequest == "object"  || typeof XMLHttpRequest == "function" ) {
	    xml = new XMLHttpRequest();
	}
	if ( typeof ActiveXObject == "object" || typeof ActiveXObject == "function" ) 
	{
		/*try
		{
		    xml = new ActiveXObject("Msxml2.XMLHTTP.5.0");
		}
		catch (e)
		{
		try
			{
			    xml = new ActiveXObject("Msxml2.XMLHTTP.4.0");
			}
			catch (e)
			{
				try
				{
				    xml = new ActiveXObject("Msxml2.XMLHTTP.3.0");
				}
				catch (e)
				{*/
					try
					{
					    xml = new ActiveXObject("Msxml2.XMLHTTP");
					}
					catch (e)
					{
					}
				/*}
			}
		}*/
	}
	return xml;
}

Tools.getUrlStatusCode = function (url)
{
	var xml = Tools.create();
	xml.open ("GET", url, false);
	xml.send ("");
	
	return (xml.status)
}

Tools.postUrlStatusCode = function (url, args)
{
	var xml = Tools.create();
	
	xml.open ("POST", url, false);
	xml.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	xml.send (args);
	
	return (xml.status)
}

Tools.wait = function()
{
}

Tools.stopWait = function()
{
}

Tools.getFromUrl = function (url)
{
	var xml = Tools.create();

	xml.open ("GET", url, false);
	
	Tools.wait();
	xml.send ("");
	Tools.stopWait();
	
	if (xml.status != 200)
	{
		return null;
	}

	return xml.responseXML;
}

Tools.postFromUrl = function (url, args)
{
	if (args == null)
	{
		args = "";
	}
	
	var xml = Tools.create();
	
	xml.open ("POST", url, false);
	xml.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	
	Tools.wait();
	xml.send (args);
	Tools.stopWait();
	
	if (xml.status != 200)
	{
		return null;
	}

	return xml.responseXML;
}

Tools.getFromXML = function(xml, tag)
{
  var node = xml.selectSingleNode("/*/" + tag);
  return node != null ? node[Tools.xmlTextContent] : null;
}

Tools.readFromUrl = function (url)
{
	var xml = Tools.create();
	xml.open ("GET", url, false)
	
	Tools.wait();
	xml.send ("")
	Tools.stopWait();
	
	if (xml.status != 200)
	{
		return null;
	}
	
	// REMPLACE LA NAVIGATION
	var text = xml.responseText.substring(xml.responseText.indexOf("?>")+2);
	text = text.substring(text.indexOf(">")+1);
	text = text.substring(0, text.lastIndexOf("<"));
	return text;
}

Tools.readFromPostUrl = function (url, args)
{
	var xml = Tools.create();
		
	xml.open ("POST", url, false)
	xml.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
	
	Tools.wait();
	xml.send (args)
	Tools.stopWait();
	
	if (xml.status != 200)
	{
		return null;
	}
	
	// REMPLACE LA NAVIGATION
	var text = xml.responseText.substring(xml.responseText.indexOf("?>")+2);
	text = text.substring(text.indexOf(">")+1);
	text = text.substring(0, text.lastIndexOf("<"));
	return text;
}

Tools.loadStyle = function (_document, url)
{
	var head = _document.getElementsByTagName("head")[0];
	var link = _document.createElement("link");
	link.rel = "stylesheet";
	link.href = url;
	link.type = "text/css";
	head.appendChild(link);
}

Tools.loadScript = function (_document, url, onload)
{
	var head = _document.getElementsByTagName("head")[0];
	var link = _document.createElement("script");
	link.src = url;
	link.charset = "UTF-8";
	if (onload != null)
	{
		link.onload = onload;
		link.onreadystatechange = function () { if (/loaded|complete/.test(this.readyState)) this.onload(); }
	}
	head.appendChild(link);
}

/**
 * Fonction issu des cocoon-forms
 * Build a query string with all form inputs
 */
Tools.buildQueryString = function(form) {
    // Indicate to the server that we're in ajax mode
    var result = "cocoon-ajax=true";
    // Iterate on all form controls
    for (var i = 0; i < form.elements.length; i++) {
        input = form.elements[i];
        
        if (input.name == null || input.name == "" || input.disabled) {
            continue;
        }
        
        if (typeof(input.type) == "undefined") {
            // Skip fieldset
            continue;
        }
        if (input.type == "submit" || input.type == "image") {
            // Skip buttons
            continue;
        }
        if ((input.type == "checkbox" || input.type == "radio") && !input.checked) {
            // Skip unchecked checkboxes and radio buttons
            continue;
        }
        if (input.type == "file") {
            // Can't send files in Ajax mode. Fall back to full page
            return null;
        }
        if (input.tagName.toLowerCase() == "select" && input.multiple) {
            var name = encodeURIComponent(input.name);
            var options = input.options;
            for (var zz = 0; zz < options.length; zz++) {
                if (options[zz].selected) {
                    result += "&" + name + "=" + encodeURIComponent(options[zz].value);
                }
            }
            // don't use the default fallback
            continue;
        }

        // text, passwod, textarea, hidden, single select
        result += "&" + encodeURIComponent(input.name) + "=" + encodeURIComponent(input.value);
    }
    return result;
} 

Tools.loadHTML = function (url, errorMessage)
{
  var postUrl = url;
  var args = "";
  
  //Recherche d'éventuels arguments
  var index = url.indexOf("?");
  if (index > 0)
  {
  		postUrl = url.substring(0, index);
  		args = url.substring (index + 1);
  }
  // Get dialog boxes
  var dialogContent = Tools.readFromPostUrl(postUrl, args);
  if (dialogContent == null)
  {
      alert(errorMessage + " [" + url + "]");
      return false;
  }
  
  var saveSpan = document.createElement("span");
  saveSpan.innerHTML = dialogContent;
  document.body.appendChild(saveSpan);
  return true;
}

Tools.htmlToTextarea = function(s)
{
    s = s.replace(/<br\/>/g, "\r\n");
    s = s.replace(/&#034;/g, "\"");
    s = s.replace(/&#039;/g, "'");
    s = s.replace(/&lt;/g, "<");
    s = s.replace(/&gt;/g, ">");
    s = s.replace(/&amp;/g, "&");
    return s;
}

Tools.textareaToHTML = function (s)
{
    s = s.replace(/\r?\n/g, "<br/>");
    s = s.replace(/"/g, "&#034;");
    s = s.replace(/'/g, "&#039;");
    return s;
}
    