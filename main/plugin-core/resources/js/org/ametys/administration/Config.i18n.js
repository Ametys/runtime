Ext.namespace('org.ametys.administration.Config');

org.ametys.administration.Config = function ()
{
}

/**
* The configuration fields
* @private
 */
org.ametys.administration.Config._fields = [];

/**
 * {Ext.form.BasicForm} The configuration form
 */
org.ametys.administration.Config._form;

/**
 * {Object []} The navigation items
 * @private
 */
org.ametys.administration.Config._navItems;

org.ametys.administration.Config.initialize = function (pluginName)
{
	org.ametys.administration.Config.pluginName = pluginName;
}

org.ametys.administration.Config.createPanel = function ()
{
	// The form
	org.ametys.administration.Config._form  = new Ext.form.FormPanel({
		region:'center',
		
		baseCls: 'transparent-panel',
		labelWidth :230,
		bodyStyle: 'position:relative;',
			
		border: false,
		autoScroll : true,
		
		id : 'config-inner',
		formId : 'save-config',
		
		html: ''
	});
	
	org.ametys.administration.Config._contextualPanel = new org.ametys.HtmlContainer({
		region:'east',
	
		cls : 'admin-right-panel',
		border: false,
		width: 277,
	    
		items: [org.ametys.administration.Config._drawNavigationPanel (),
		        org.ametys.administration.Config._drawHandlePanel (),
		        org.ametys.administration.Config._drawHelpPanel ()]
	});
	
	return new Ext.Panel({
		region: 'center',
		
		autoScroll: false,
		
		baseCls: 'transparent-panel',
		border: false,
		layout: 'border',
		
		items: [org.ametys.administration.Config._form , 
		        org.ametys.administration.Config._contextualPanel]
	});
}

org.ametys.administration.Config.createFieldSet = function (id, label)
{
	return new org.ametys.Fieldset({
		id : id,
		title : label,
		
		layout: 'form',
		
		width: 595
	});
}

org.ametys.administration.Config.addGroupCategory = function (fd, name)
{
	fd.add (new org.ametys.HtmlContainer ({
		html: name,
		cls: 'ametys-subcategory'
	}));
}

org.ametys.administration.Config.addInputField = function (ct, type, name, value, label, description)
{
	switch (type) {
		case 'double':
			ct.add(org.ametys.administration.Config._createDoubleField (name, value, label, description));
			break;
		case 'long':
			ct.add(org.ametys.administration.Config._createLongField (name, value, label, description));
			break;
		case 'password':
			ct.add(org.ametys.administration.Config._createPasswordField (name, value, label, description));
			break;
		case 'date':
			ct.add(org.ametys.administration.Config._createDateField (name, value, label, description));
			break;
		case 'boolean':
			ct.add(org.ametys.administration.Config._createBooleanField (name, value, label, description));
			break;
		default:
			ct.add(org.ametys.administration.Config._createTextField (name, value, label, description));
			break;
	}
	
	org.ametys.administration.Config._fields.push(name);
}

org.ametys.administration.Config.getInputHeight = function (input)
{
	if (typeof input == 'org.ametys.form.PasswordWidget')
	{
		return 75;
	}
	else if (typeof input == 'org.ametys.form.BooleanField')
	{
		return 22;
	}
	else
	{
		return 36;
	}
		
}

org.ametys.administration.Config._createDoubleField = function (name, value, label, description)
{
	return new org.ametys.form.DoubleField ({
		name: name,
        fieldLabel: label,
        desc: description,
        
        value: value,
        
        width: 250
	});
}

org.ametys.administration.Config._createLongField = function (name, value, label, description)
{
	return new org.ametys.form.LongField ({
		name: name,
		fieldLabel: label,
        desc: description,
        
        value: value,
        
        width: 250
	});
}

org.ametys.administration.Config._createPasswordField = function (name, value, label, description)
{
	return new org.ametys.form.PasswordWidget ({
		name: name,
		
	    fdLabel: label,
	    desc: description,
	    
	    value: value,
	    
	    fdLabelWidth :230
	});
}

org.ametys.administration.Config._createDateField = function (name, value, label, description)
{
	return new org.ametys.form.DateField ({
		name: name,
		 
        fieldLabel: label,
        desc: description,
        value: value,
        
        width: 250
	});
}

org.ametys.administration.Config._createBooleanField = function (name, value, label, description)
{
	return new org.ametys.form.BooleanField ({
		name: name,
		 
        fieldLabel: label,
        desc: description,
        
        checked: (value == "true")
        
	});
}

org.ametys.administration.Config._createTextField = function (name, value, label, description)
{
	return new org.ametys.form.TextField ({
		name: name,
		
        fieldLabel: label,
        desc: description,
        value: value,
        
        width: 250
	});
}

/**
 * Draw the navigation panel. This function needs the org.ametys.administration.Config._navItems was filled first.
 * @return {org.ametys.NavigationPanel} The navigation panel
 * @private
 */
org.ametys.administration.Config._drawNavigationPanel = function ()
{
	org.ametys.administration.Config._nav = new org.ametys.NavigationPanel ({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_MENU"/>"});
	
	for (var i=0; i &lt; org.ametys.administration.Config._navItems.length; i++)
	{
		var item = new org.ametys.NavigationItem ({
			id : "a" + org.ametys.administration.Config._navItems[i].id,
			text: org.ametys.administration.Config._navItems[i].label,
			
			divToScroll: org.ametys.administration.Config._navItems[i].id,
			ctToScroll:  'config-inner',
			
			bindScroll: org.ametys.administration.Config._bindScroll,
			unbindScroll:  org.ametys.administration.Config._unbindScroll,
			
			toggleGroup : 'config-menu'
		});
		
		org.ametys.administration.Config._nav.add(item);
	}
	
	return org.ametys.administration.Config._nav;
}

/**
 * Draw the actions panel.
 * @return {org.ametys.ActionsPanel} The action panel
 * @private
 */
org.ametys.administration.Config._drawHandlePanel = function ()
{
	org.ametys.administration.Config._actions = new org.ametys.ActionsPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE"/>"});
	
	// Save action
	org.ametys.administration.Config._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_SAVE"/>", 
					 getPluginResourcesUrl(org.ametys.administration.Config.pluginName) + '/img/administrator/config/save.png',
					 org.ametys.administration.Config.save);
	
	// Quit action
	org.ametys.administration.Config._actions.addAction("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HANDLE_QUIT"/>", 
				     getPluginResourcesUrl(org.ametys.administration.Config.pluginName) + '/img/administrator/config/quit.png',
				     org.ametys.administration.Config.goBack);
	
	return org.ametys.administration.Config._actions;
}

/**
 * Draw the help panel.
 * @return {org.ametys.TextPanel} The help panel
 * @private
 */
org.ametys.administration.Config._drawHelpPanel = function ()
{
	var helpPanel = new org.ametys.TextPanel({title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP"/>"});
	helpPanel.addText("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_HELP_TEXT"/>");
	
	return helpPanel;
}

/**
 * Quit
 */
org.ametys.administration.Config.goBack = function ()
{
    document.location.href = context.workspaceContext;
}

/**
 * Save configuration
 */
org.ametys.administration.Config.save = function ()
{
	if (!org.ametys.administration.Config._form.getForm().isValid())
	{
		return;
    }
    
	// TODO Use servercomm api
    var url = getPluginDirectUrl(org.ametys.administration.Config.pluginName) + "/administrator/config/set";
    var args = org.ametys.administration.Config._getFormParameters (org.ametys.administration.Config._form.getForm());

    var result = Tools.postFromUrl(url, args);
    if (result == null)
    {
    	Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_FATALERROR"/>",
    		buttons: Ext.Msg.OK,
			icon: Ext.MessageBox.ERROR
    	});
        return;
    }
    
    var error = Tools.getFromXML(result, "error");
    if (error != null &amp;&amp; error != "")
    {
    	Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_ERROR"/>",
    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.ERROR
    	});
        return;
    }
    
    Ext.Msg.show ({
    		title: "<i18n:text i18n:key="PLUGINS_CORE_SAVE_DIALOG_TITLE"/>",
    		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_CONFIG_SAVE_OK"/>",
    		buttons: Ext.Msg.OK,
				icon: Ext.MessageBox.INFO
    });
    
    // Back
    org.ametys.administration.Config.goBack();
}

org.ametys.administration.Config._getFormParameters = function (form)
{
	var args = "";
	for (var i=0; i &lt; org.ametys.administration.Config._fields.length; i++)
	{
		var field = form.findField(org.ametys.administration.Config._fields[i]);
		if (field.getXType() == 'datefield' &amp;&amp; field.getValue() != '')
		{
			args += "&amp;" + field.getName() + "=" + field.getValue().format(Date.patterns.ISO8601Long);
		}
		else if (field.getXType() == 'hidden')
		{
			if (field.getValue() != '')
			{
				args += "&amp;" + field.getName() + "=" + field.getValue();
			}
		}
		else
		{
			args += "&amp;" + field.getName() + "=" + field.getValue();
		}
	}
	return args;
}

org.ametys.administration.Config._bound;

org.ametys.administration.Config._bindScroll = function ()
{
	org.ametys.administration.Config._bound = true;
}
org.ametys.administration.Config._unbindScroll = function ()
{
	org.ametys.administration.Config._bound = false;
}

org.ametys.administration.Config._calcScrollPosition = function ()
{
	if (!org.ametys.administration.Config._bound)
		return;
		 
	var last;
	var anchors = org.ametys.administration.Config._ct.select('a[name]', true);
	var min = 0;
	var max = org.ametys.administration.Config._form.getEl().child('form').dom.scrollHeight - org.ametys.administration.Config._form.getEl().child('form').getHeight();
	
	var scrollPosition = org.ametys.administration.Config._form.getEl().child('form').dom.scrollTop;
	var p = (scrollPosition - min) / (max - min);
	p = p * org.ametys.administration.Config._form.getInnerHeight();
	
	var a0 = anchors.elements[0].getTop();
	
	for (var i=0;  i &lt; anchors.elements.length; i++)
	{
		var anchor = anchors.elements[i];
		if (i > 0) {
			last = anchors.elements[i-1];
		}
		else {
			last = anchor;
		}
		var posY = anchor.getTop() - a0;
		if(posY >= scrollPosition + p)
		{
			org.ametys.administration.Config._activateItemMenu(last.dom.name);
			return;
		}
	
	}
	org.ametys.administration.Config._activateItemMenu(anchors.elements[anchors.elements.length - 1].dom.name);
}

org.ametys.administration.Config._activateItemMenu = function (id)
{
	var button = Ext.getCmp("a" + id);
	if	(button != null)
	{	
		Ext.getCmp("a" + id).toggle(true);
	}
}

org.ametys.administration.Config._ct;

org.ametys.administration.Config.onready = function () 
{
	org.ametys.administration.Config._bound = true;
	
	org.ametys.administration.Config._ct = Ext.getCmp("config-inner").getEl().child("div:first").child("*:first");
	
	org.ametys.administration.Config._ct.on('scroll', org.ametys.administration.Config._calcScrollPosition);
	
	org.ametys.administration.Config._calcScrollPosition();

}
Ext.onReady(org.ametys.administration.Config.onready);
