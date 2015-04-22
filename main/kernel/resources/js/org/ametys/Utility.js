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
function Utils()
{
}

Utils.buildParams = function (form) 
{
	var result = {};
	
    // Iterate on all form controls
    for (var i = 0; i < form.elements.length; i++) 
    {
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
            var name = input.name;
            var options = input.options;
            for (var zz = 0; zz < options.length; zz++) {
                if (options[zz].selected) {
                	result['name'] = options[zz].value;
                }
            }
            // don't use the default fallback
            continue;
        }

        // text, passwod, textarea, hidden, single select
        result[input.name] = input.value
    }
    return result;
} 

Utils.htmlToTextarea = function(s)
{
    s = s.replace(/<br\/>/g, "\r\n");
    s = s.replace(/&#034;/g, "\"");
    s = s.replace(/&#039;/g, "'");
    s = s.replace(/&lt;/g, "<");
    s = s.replace(/&gt;/g, ">");
    s = s.replace(/&amp;/g, "&");
    return s;
}

Utils.textareaToHTML = function (s)
{
    s = s.replace(/\r?\n/g, "<br/>");
    s = s.replace(/"/g, "&#034;");
    s = s.replace(/'/g, "&#039;");
    return s;
}

Utils.loadStyle = function (url)
{
	var head = document.getElementsByTagName("head")[0];
	var link = document.createElement("link");
	link.rel = "stylesheet";
	link.href = url;
	link.type = "text/css";
	head.appendChild(link);
}

Utils.loadScript = function (url, onload)
{
	var head = document.getElementsByTagName("head")[0];
	var link = document.createElement("script");
	link.src = url;
	link.charset = "UTF-8";
	if (onload != null)
	{
		link.onload = onload;
		link.onreadystatechange = function () { if (/loaded|complete/.test(this.readyState)) this.onload(); }
	}
	head.appendChild(link);
}

Utils.sortAsNonAccentedUCString = function (s)
{
	s = s.toLowerCase();
	
	s = s.replace(new RegExp(/[ÀÁÂÃÄÅ]/g),"A");
	s = s.replace(new RegExp(/[àáâãäå]/g),"a");
	s = s.replace(new RegExp(/Æ/g),"AE");
	s = s.replace(new RegExp(/æ/g),"ae");
	s = s.replace(new RegExp(/Ç/g),"C");
	s = s.replace(new RegExp(/ç/g),"c");
	s = s.replace(new RegExp(/[ÈÉÊË]/g),"E");
	s = s.replace(new RegExp(/[èéêë]/g),"e");
	s = s.replace(new RegExp(/[ÌÍÎÏ]/g),"I");
	s = s.replace(new RegExp(/[ìíîï]/g),"i");
	s = s.replace(new RegExp(/Ñ/g),"N");
	s = s.replace(new RegExp(/ñ/g),"n");
	s = s.replace(new RegExp(/[ÒÓÔÕÖ]/g),"O");
	s = s.replace(new RegExp(/[òóôõö]/g),"o");
	s = s.replace(new RegExp(/Œ/g),"OE");
	s = s.replace(new RegExp(/œ/g),"oe");
	s = s.replace(new RegExp(/[ÙÚÛÜ]/g),"U");
	s = s.replace(new RegExp(/[ùúûü]/g),"u");
	s = s.replace(new RegExp(/[ÝŸ]/g),"y");
	s = s.replace(new RegExp(/[ýÿ]/g),"y");
    
    return Ext.data.SortTypes.asUCString(s);
}

/**
 * Open a popup window with http POST or GET data
 * @param {String} url the window url
 * @param {Object} data the request parameters
 * @param {String} [method="POST"] the request method for data, 'GET' or 'POST', defaults to "POST"
 * @param {String} [target="_blank"] an optional opening target (a name, or "_self"), defaults to "_blank"
 */
Utils.openWindow = function (url, data, method, target)
{
	var form = document.createElement("form");
	form.action = url;
	form.method = method || "POST";
	form.target = target || "_blank";
	
	if (data) 
	{
		for (var key in data) 
		{
			var input = document.createElement("input");
			input.type = 'hidden';
			input.name = key;
			input.value = typeof data[key] === "object" ? Ext.JSON.encode(data[key]) : data[key];
			form.appendChild(input);
	    }
	}
	
	form.style.position = "absolute";
	form.style.left = "-10000px";
	
	document.body.appendChild(form);
	form.submit();
	
	// delete form
	document.body.removeChild(form);
}