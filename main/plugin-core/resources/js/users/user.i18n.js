<!--+
    | Copyright (c) 2007 Anyware Technologies and others.
    | All rights reserved. This program and the accompanying materials
    | are made available under the terms of the Eclipse Public License v1.0
    | which accompanies this distribution, and is available at
    | http:// www.opensource.org/licenses/eclipse-1.0.php
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
RUNTIME_Plugin_Runtime_EditUser.initialized = false;
RUNTIME_Plugin_Runtime_EditUser.initialize = function(plugin, fieldsNum)
{
	RUNTIME_Plugin_Runtime_EditUser.plugin = plugin;
    RUNTIME_Plugin_Runtime_EditUser.fieldsNum = fieldsNum;
}

RUNTIME_Plugin_Runtime_EditUser.delayedInitialize = function()
{
	if (RUNTIME_Plugin_Runtime_EditUser.initialized)
		return true;
	
	var formPanel = new Ext.FormPanel( {
		formId : 'edit-user-form',
		bodyStyle : 'padding:10px',
		labelWidth :100,
		width :300,
		defaultType :'textfield'
	});
	
	for (var i=0; i &lt; formInputs.length; i++)
	{
		formPanel.add(formInputs[i]);
	}
	
	RUNTIME_Plugin_Runtime_EditUser.form = formPanel.getForm();
	
	RUNTIME_Plugin_Runtime_EditUser.box = new org.ametys.DialogBox({
		title :"<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>",
		layout :'fit',
		width :380,
		height : (80 + 25*RUNTIME_Plugin_Runtime_EditUser.fieldsNum),
		items : [ formPanel ],
		closeAction: 'hide',
		buttons : [ {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_OK"/>",
			handler : function() {
			RUNTIME_Plugin_Runtime_EditUser.ok();
			}
		}, {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_CANCEL"/>",
			handler : function() {
			RUNTIME_Plugin_Runtime_EditUser.cancel();
			}
		} ]
	});
	
	if (RUNTIME_Plugin_Runtime_EditUser.params['mode'] == 'new')
	{
		RUNTIME_Plugin_Runtime_EditUser.box.setTitle("<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>", 'new-user-icon-box');
	}
	else
	{
		RUNTIME_Plugin_Runtime_EditUser.box.setTitle("<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_LABEL"/>", 'edit-user-icon-box');
	}
	
	RUNTIME_Plugin_Runtime_EditUser.initialized = true;
	
    return true;
}

RUNTIME_Plugin_Runtime_EditUser.act2 = function()
{
    var params = RUNTIME_Plugin_Runtime_EditUser.params;
    
    if (params['mode'] == 'new')
    {
    	RUNTIME_Plugin_Runtime_EditUser.form.findField('field_login').setDisabled(false);
    	var elements = RUNTIME_Plugin_Runtime_EditUser.form.getEl().dom.elements;
    	// Réinitialisation des champs
        for (var i = 0; i &lt; elements.length; i++)
        {
        	var elt = elements[i];
            if (elt.name.indexOf('field_') == 0)
            {
                elt.value = "";
            }
        }
    }
    else
    {
    	RUNTIME_Plugin_Runtime_EditUser.form.findField('field_login').setDisabled(true);
      
	    var url = getPluginDirectUrl(RUNTIME_Plugin_Runtime_EditUser.plugin) + "/users/info";
	    var arg = "login=" + params['login'];
	      
	    var nodes = Tools.postFromUrl(url, arg);
	    if (nodes == null)
	    {
	    	Ext.Msg.show ({
        		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
        		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_ERROR"/>",
        		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
        	});
	        return;
	    }
	    var userInfo = nodes.selectSingleNode("/users-info/users/user[@login='" + params['login'] + "']");
	      
	    RUNTIME_Plugin_Runtime_EditUser.form.findField('field_login').setValue(userInfo.getAttribute("login"));
	   
	    var fields = userInfo.selectNodes('*');
	    for (var i = 0; i &lt; fields.length; i++)
	    {
	    	var field = fields[i];
	        var fieldName = fields[i].nodeName;
	        var fieldValue = fields[i][Tools.xmlTextContent];
	        
	        var elt = RUNTIME_Plugin_Runtime_EditUser.form.findField('field_' + fieldName);
	        elt.setValue(fieldValue);
	    }
    }
    // TODO focus()
}

RUNTIME_Plugin_Runtime_EditUser.act = function(params, callback)
{
	RUNTIME_Plugin_Runtime_EditUser.params = params;
	RUNTIME_Plugin_Runtime_EditUser.callback = callback;
	
	if (!RUNTIME_Plugin_Runtime_EditUser.delayedInitialize())
        return;
	
	RUNTIME_Plugin_Runtime_EditUser.form.reset();
	RUNTIME_Plugin_Runtime_EditUser.box.show();
	RUNTIME_Plugin_Runtime_EditUser.act2 ();
}

RUNTIME_Plugin_Runtime_EditUser.cancel = function ()
{
	RUNTIME_Plugin_Runtime_EditUser.box.hide();
}

RUNTIME_Plugin_Runtime_EditUser.ok = function ()
{
	var form = RUNTIME_Plugin_Runtime_EditUser.form;
  
	var url = getPluginDirectUrl(RUNTIME_Plugin_Runtime_EditUser.plugin) + "/users/edit";
	var args = Tools.buildQueryString(form.getEl().dom);
    args += "&amp;field_login=" + form.findField('field_login').getValue();
    args += "&amp;mode=" + RUNTIME_Plugin_Runtime_EditUser.params['mode'];
    var result = Tools.postFromUrl(url, args);

	if (result == null)
	{
		Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_ERROR_DIALOG_TITLE"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_ERROR"/>",
    		buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.ERROR
    	});
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
          var elt = form.findField("field_"+ field);
          if (elt != null)
          {
            elt.markInvalid("<i18n:text i18n:key='PLUGINS_CORE_USERS_DIALOG_INVALID_FIELD'/>");
          }
        }
      }
      return;
    }	
  
    // mise à jour graphique
    function getValue(name)
    {
    	var e = form.findField("field_" + name);
    	if (e == null)
    		return null;
    	else
    		return e.getValue();
    }

    var firstname = getValue("firstname");
    var lastname = getValue("lastname");
    var login = getValue("login");
    var email = getValue("email");

    if (typeof RUNTIME_Plugin_Runtime_EditUser.callback == 'function')
    {
    	RUNTIME_Plugin_Runtime_EditUser.callback (login, firstname, lastname, email);
    }
	
	RUNTIME_Plugin_Runtime_EditUser.box.hide();
}