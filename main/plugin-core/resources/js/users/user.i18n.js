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
		
		autoScroll: true,
		
		labelWidth :100,
		defaultType :'textfield',
		defaults: {
			msgTarget: 'side'
		}
	});
	
	for (var i in formInputs)
	{
		formPanel.add(formInputs[i]);
	}
	
	RUNTIME_Plugin_Runtime_EditUser.form = formPanel.getForm();
	
	RUNTIME_Plugin_Runtime_EditUser.box = new org.ametys.DialogBox({
		
		title: "<i18n:text i18n:key="PLUGINS_CORE_USERS_HANDLE_NEW"/>",
		icon: getPluginResourcesUrl('core') + '/img/users/icon_small.png',
		
		width : 430,
		//height : (85 + 32 * RUNTIME_Plugin_Runtime_EditUser.fieldsNum),
		
		items : [ formPanel ],
		
		defaultButton: formPanel.getForm().items.get(0),
		closeAction: 'hide',
		buttons : [ {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_OK"/>",
			handler : RUNTIME_Plugin_Runtime_EditUser.ok
		}, {
			text :"<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_CANCEL"/>",
			handler : RUNTIME_Plugin_Runtime_EditUser.cancel
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
      
    	
    	var serverMessage = new org.ametys.servercomm.ServerMessage(RUNTIME_Plugin_Runtime_EditUser.plugin, "/users/info", { login: params['login'] }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
    	var nodes = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

        if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_ERROR"/>", nodes, "RUNTIME_Plugin_Runtime_EditUser.delayedInitialize"))
        {
           return;
        }

	    var userInfo = nodes.selectSingleNode("users-info/users/user[@login='" + params['login'] + "']");
	      
	    RUNTIME_Plugin_Runtime_EditUser.form.findField('field_login').setValue(userInfo.getAttribute("login"));
	   
	    var fields = userInfo.selectNodes('*');
	    for (var i = 0; i &lt; fields.length; i++)
	    {
	    	var field = fields[i];
	        var fieldName = fields[i].nodeName;
	        var fieldValue = fields[i][org.ametys.servercomm.ServerComm.xmlTextContent];
	        
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

	var args = RUNTIME_Plugin_Runtime_EditUser._buildParams(form);
    args['field_login'] = form.findField('field_login').getValue();
    args['mode'] = RUNTIME_Plugin_Runtime_EditUser.params['mode'];

	var serverMessage = new org.ametys.servercomm.ServerMessage(RUNTIME_Plugin_Runtime_EditUser.plugin, "users/edit", args, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var result = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

    if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_USERS_DIALOG_ERROR"/>", result, "org.ametys.cms.tool.profile.ProfilesTool"))
    {
       return;
    }
  
    // passe les erreurs en rouges
	var fieldsString = org.ametys.servercomm.ServerComm.handleResponse(result, "error");
    if (fieldsString != null &amp;&amp; fieldsString.length &gt; 0)
    {
      var fields = fieldsString.split(",");
      for (var i = 0; i &lt; fields.length; i++)
      {
        var field = fields[i];
        
        if (field.length &gt; 0)
        {
          var elt = formInputs[field];
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

RUNTIME_Plugin_Runtime_EditUser._buildParams = function (form)
{
	var params = {};
	
	var formDom = form.getEl().dom;
	for (var i = 0; i &lt; formDom.elements.length; i++) 
	{
        input = formDom.elements[i];
        
        if (input.name == null || input.name == "" || input.disabled) {
            continue;
        }
        
        if (typeof(input.type) == "undefined" || input.type == "submit" || input.type == "image" || input.type == "file") {
            // Skip buttons; Type file is not supported
            continue;
        }
        
        if (input.type == "checkbox") {
        	params[input.name] = input.checked ? '1' : '0';
        	continue;
        }
        
        if (input.type == "radio" &amp;&amp; !input.checked) {
            // Skip unchecked checkboxes and radio buttons
            continue;
        }

        var fd = form.findField(input.name) || Ext.getCmp(input.id);
        if (fd == null)
        {
        	params[input.name] = input.value;
        }
        else
        {
	        if ((fd.xtype == 'datefield' || fd.xtype == 'datetimefield') &amp;&amp; fd.getValue() != '')
			{
	        	// Date
	        	params[input.name] = fd.getValue().format(Date.patterns.ISO8601Long); 
	            continue;
			}
	        
	        var val = fd.getValue();
	        if (fd.multiple &amp;&amp; !Ext.isArray(val))
	        {
	        	var values = val.split(',');
	        	var trimValues = [];
	        	for (var j = 0; j &lt; values.length; j++)
	        	{
	        		trimValues.push(values[j].trim());
	        	}
	        	params[input.name] = trimValues;
	        	continue;
	        }
	        
	        // text, password, textarea, hidden, single select
	        params[input.name] = val;
        }
    }
	
	return params;
}