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
/**
 * Edit User
 */
function RUNTIME_Plugin_Runtime_EditUser()
{
}

RUNTIME_Plugin_Runtime_EditUser.initialize = function(plugin, fieldsNum)
{
	RUNTIME_Plugin_Runtime_EditUser.plugin = plugin;
    RUNTIME_Plugin_Runtime_EditUser.fieldsNum = fieldsNum;
}

RUNTIME_Plugin_Runtime_EditUser.delayedInitialize = function()
{
	if (RUNTIME_Plugin_Runtime_EditUser.initialized == true)
    {
        RUNTIME_Plugin_Runtime_EditUser.act2();
		return true;
    }
    
    var plugin = RUNTIME_Plugin_Runtime_EditUser.plugin;
	
    if (!Tools.loadHTML(getPluginDirectUrl(plugin) + "/users/dialog.html", "<i18n:text i18n:key="KERNEL_DIALOG_ERRORREADINGURL" i18n:catalogue="kernel"/>"))
        return false;

	var config = new SDialog.Config()
	config.innerTableClass = "dialog";
	RUNTIME_Plugin_Runtime_EditUser.box = new SDialog("RUNTIME_Plugin_Runtime_EditUser", 
								"<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_LABEL"/>", 
								getPluginResourcesUrl(plugin) + "/img/users/icon_small.gif", 
								320, 70 + 22*RUNTIME_Plugin_Runtime_EditUser.fieldsNum, config, RUNTIME_Plugin_Runtime_EditUser);
	RUNTIME_Plugin_Runtime_EditUser.box.paint();

	var _document = RUNTIME_Plugin_Runtime_EditUser.box.ui.iframe.contentWindow.document;
	_document.charset = "UTF-8";
	
	Tools.loadStyle(_document, context.contextPath + "/kernel/resources/css/dialog.css");
    Tools.loadScript(_document, getPluginResourcesUrl(plugin) + "/js/widgets.js.i18n", RUNTIME_Plugin_Runtime_EditUser.actIntermediate);

    RUNTIME_Plugin_Runtime_EditUser.initialized = true;
    
    return true;
}

RUNTIME_Plugin_Runtime_EditUser.actIntermediate = function()
{
  var _window = RUNTIME_Plugin_Runtime_EditUser.box.ui.iframe.contentWindow;
  if (new _window.runtime_window.Date().print != null &amp;&amp; _window.Calendar != null &amp;&amp; _window.Calendar._SDN != null)
  {
    RUNTIME_Plugin_Runtime_EditUser.act2();
  }
  else
  {
    window.setTimeout("RUNTIME_Plugin_Runtime_EditUser.actIntermediate()", 100);
  }
}

RUNTIME_Plugin_Runtime_EditUser.act2 = function()
{
    var params = RUNTIME_Plugin_Runtime_EditUser.params;

    var _window = RUNTIME_Plugin_Runtime_EditUser.box.ui.iframe.contentWindow;
    var _document = _window.document;
  
    _document.getElementById('mode').value = params['mode'];
    
    // remet les erreurs en noir
    var table = _document.getElementById('innertable');
    for (var i = 0; i &lt; table.rows.length - 1; i++)
    {
      table.rows[i].cells[0].style.color = "";
    }
    
    if (params['mode'] == 'new')
    {
          _document.getElementById('field_login').disabled = false;
          for (var i = 0; i &lt; _document.forms[0].elements.length; i++)
          {
            var elt = _document.forms[0].elements[i];
            if (elt.name.indexOf('field_') == 0)
            {
                elt.value = '';
                
                if (_document.getElementById(elt.name + '_date') != null)
                {
                  _window.runtime_update(elt.name);
                }
                else if (_document.getElementById(elt.name + '_password') != null)
                {
                  var field = _document.getElementById(elt.name + '_password');
                  field.value = '';
                  _window.runtime_initPassword(elt.name);
                }
            }
          }
    }
    else
    {
      _document.getElementById('field_login').disabled = true;
      
      var url = getPluginDirectUrl(RUNTIME_Plugin_Runtime_EditUser.plugin) + "/users/details.xml";
      var arg = "login=" + params['login'];
      
      var nodes = Tools.postFromUrl(url, arg);
      if (nodes == null)
      {
        alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_ERROR"/>");
        return;
      }
      var userInfo = nodes.selectSingleNode("/UsersDetail/users/user[@login='" + params['login'] + "']");
      
      _document.getElementById('field_login').value = userInfo.getAttribute("login");
   
      var fields = userInfo.selectNodes('*');
      for (var i = 0; i &lt; fields.length; i++)
      {
        var field = fields[i];
        var fieldName = fields[i].nodeName;
        var fieldValue = fields[i][Tools.xmlTextContent];
        
        var elt = _document.forms[0].elements['field_' + fieldName];
        elt.value  = fieldValue;
        
        if (_document.getElementById('field_' + fieldName + '_date') != null)
        {
          _window.runtime_update('field_' + fieldName);
        }
        else if (_document.getElementById('field_' + fieldName + '_password') != null)
        {
          var input = _document.getElementById('field_' + fieldName);
          input.value = 'PASSWORD';
          _window.runtime_initPassword('field_' + fieldName);
        }
        else if (_document.getElementById(elt.id+'_checkbox') != null)
        {
          var checkboxElt = _document.getElementById(elt.id+'_checkbox');
          checkboxElt.checked = fieldValue == "true" ? true : false;
        }
      }
    }
    
    RUNTIME_Plugin_Runtime_EditUser.box.showModal();
    
    for (var i = 0; i &lt; _document.forms[0].elements.length; i++)
    {
      if (_document.forms[0].elements[i].type != "password")
      {
       try
       {
          _document.forms[0].elements[i].focus();
          _document.forms[0].elements[i].select();
          break;
       }
       catch (e)
       {
          // try next
       }
      }
    }
}

RUNTIME_Plugin_Runtime_EditUser.act = function(params)
{
    RUNTIME_Plugin_Runtime_EditUser.params = params;
	if (!RUNTIME_Plugin_Runtime_EditUser.delayedInitialize())
        return;
}

RUNTIME_Plugin_Runtime_EditUser.cancel = function ()
{
	RUNTIME_Plugin_Runtime_EditUser.box.close();
}

RUNTIME_Plugin_Runtime_EditUser.ok = function ()
{
	var _document = RUNTIME_Plugin_Runtime_EditUser.box.ui.iframe.contentWindow.document;
  
	var url = getPluginDirectUrl(RUNTIME_Plugin_Runtime_EditUser.plugin) + "/users/edit";
	var args = Tools.buildQueryString(_document.forms['dialog_edituser-form']);
    args += "&amp;field_login=" + _document.getElementById('field_login').value;
	
    var result = Tools.postFromUrl(url, args);

    // remet les erreurs en noir
    var table = _document.getElementById('innertable');
    for (var i = 0; i &lt; table.rows.length - 1; i++)
    {
      table.rows[i].cells[0].style.color = "";
    }

	if (result == null)
	{
		alert("<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_ERROR"/>")
		return;
	}
  
    // passe les erreurs en rouges
    var fieldsString = Tools.getFromXML(result, "error");
    if (fieldsString != null &amp;&amp; fieldsString.length &gt; 0)
    {
      var fields = fieldsString.split(",");
      for (var i = 0; i &lt; fields.length; i++)
      {
        var field = fields[i];
        if (field.length &gt; 0)
        {
          var elt = _document.getElementById("fieldlabel_" + field);
          if (elt != null)
            elt.style.color = "red";
        }
      }
      return;
    }
  
    // mise à jour graphique
    function getValue(name)
    {
      var e = _document.getElementById("field_" + name);
      if (e == null)
        return null;
      else
        return e.value;
    }

    var firstname = getValue("firstname");
    var lastname = getValue("lastname");
    var login = getValue("login");
    var email = getValue("email");

	// update view
	if (_document.getElementById('mode').value == "new")
	{
		addElement(firstname, lastname, login, email);
	}
	else
	{
		var element = slistview.getSelection()[0];
		updateElement (element, firstname, lastname, email);
	}
  
	slistview.paint();

	RUNTIME_Plugin_Runtime_EditUser.box.close();
}