/*
 *  Copyright 2012 Anyware Services
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
 * Authentication form panel
 */
Ext.define('Ametys.public.AuthDialog', {
	extend: 'Ext.form.FormPanel',
	
	/**
     * @cfg {Boolean} [authFailure=false] Set to `true' to display form in authentication failed mode
     */
    authFailure: false,
    /**
     * @cfg {Boolean} [rememberMe=false] Set to `true' to allow "Remember" check box
     */
    rememberMe: false,
    /**
     * @cfg {Boolean} [forgotPassword=true] Set to `true' to enable "Forgot password" link
     */
    forgotPassword: false,
    
    /**
     * @cfg {String} [loginField=Username] Name of login input
     */
    loginFieldName: 'Username',
    /**
     * @cfg {String} [passwordField=Password] Name of password input
     */
    pwdFieldName: 'Password',
    
	standardSubmit: true,
	autoComplete : true,
    bodyPadding : "20 20",
    border: true,
    cls : "ametys-auth-dialog",
    header : false,
    width : 415,
    
	layout : {
      type : "vbox",
      align : "stretch"
    },
    
    initComponent: function ()
    {
    	this.items = [
          	{
        		xtype : "label",
        		text : "<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_LABEL'/>",
        	}, 
        	// Login
        	{
        		xtype : "textfield",
        		cls : "ametys-auth-textbox",
        		name : this.loginFieldName,
        		// bind : "{login}",
        		height : 55,
        		hideLabel : true,
        		allowBlank : false,
        		emptyText : "<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_LOGIN'/>",
        		triggers : {
        			glyphed : {
        				cls : "trigger-glyph-noop auth-login-trigger"
        			}
        		}
            },
        	// Password
        	{
            	xtype : "textfield",
                cls : "ametys-auth-textbox",
                height : 55,
                hideLabel : true,
                emptyText : "<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PASSWORD'/>",
                inputType : "password",
                name : this.pwdFieldName,
                // bind : "{password}",
                allowBlank : false,
                triggers : {
                  glyphed : {
                    cls : "trigger-glyph-noop auth-password-trigger"
                  }
                }
            },
            {
            	cls: 'ametys-auth-error-text',
            	itemId: 'error-text',
        		xtype : "container",
        		hidden: true
        	},
            // Remember me ?
            {
                xtype : "container",
                layout : "hbox",
                items : [{
                  hidden: !this.rememberMe,
                  xtype : "checkboxfield",
                  flex : 1,
                  cls : "form-panel-font-color rememberMeCheckbox",
                  height : 30,
                  // bind : "{rememberMe}",
                  boxLabel : "<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_PERSIST'/>",
                }, {
                	xtype:'tbseparator',
            		flex:1,
            		hidden: this.rememberMe
                },{
                  xtype : "box",
                  hidden: !this.forgotPassword,
                  html : '<a href="#authentication.passwordreset" class="link-forgot-password">' + "<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_FORGOT_PASSWORD'/>" + '</a>'
                }]
             }, 
             // Submit
             {
                xtype : "button",
                scale : "large",
                // ui : "soft-green",
                iconAlign : "right",
                iconCls : "x-fa fa-angle-right",
                text : "<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_SUBMIT'/>",
                // formBind : true,
                handler: function () { 
                	var form = this.ownerCt;
                	form.submit();
                }
             }
    	]
    	this.callParent(arguments);
    },
  
    listeners: {
  	'afterrender': function () {
  		
  		if (this.authFailure)
  		{
  			this.down('#error-text').update("<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_AUTH_FAILURE'/>");
  			this.down('#error-text').show();
  			this.getForm().findField('Username').markInvalid("<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_AUTH_FAILURE'/>");
  			this.getForm().findField('Password').markInvalid("<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_FORM_AUTH_FAILURE'/>");
  		}
  	}
  }
});

/**
 * Panel to display news
 */
Ext.define('Ametys.public.NewsDialog', {
	extend: 'Ext.panel.Panel',
	
	width : 380,
	height: 640,
	bodyPadding : "20 20",
    border: true,
    cls : "ametys-news-dialog",
    
    html: '<a class="twitter-timeline" href="https://twitter.com/AmetysCMS" data-widget-id="639097471352860672" data-chrome="transparent nofooter">Tweets de @AmetysCMS</a>'
});

/**
 * Panel for a full login screen
 */
Ext.define('Ametys.public.LoginScreen', {
    extend: 'Ext.panel.Panel',
    
    title: "<i18n:text i18n:key='PLUGINS_CORE_UI_LOGIN_SCREEN_TITLE'/>",
    cls : "ametys-auth-screen",
    
    /**
     * @cfg {Boolean} [authFailure=false] Set to `true' to display form in authentication failed mode
     */
    authFailure: false,
    /**
     * @cfg {Boolean} [rememberMe=false] Set to `true' to allow "Remember" check box
     */
    rememberMe: false,
    /**
     * @cfg {Boolean} [forgotPassword=true] Set to `true' to enable "Forgot password" link
     */
    forgotPassword: false,
    
    /**
     * @cfg {String} [loginField=Username] Name of login input
     */
    loginFieldName: 'Username',
    /**
     * @cfg {String} [passwordField=Password] Name of password input
     */
    pwdFieldName: 'Password',
    

    titleAlign : "center",
    maximized : true,
    modal : true,
    frameHeader : false,
    
    layout : {
      type : "vbox",
      align: "stretch",
      pack: "center"
    },
    
    bodyStyle: {
    	padding: '20px 12% 20px 12%'
    },
    
    initComponent: function ()
    {
    	this.items = [{
        	xtype: 'container',
        	flex: 1,
        	layout : {
    	      type : "hbox",
    	      align: "middle"
    	    },
        	items:[
            	Ext.create('Ametys.public.AuthDialog', {
            		loginFieldName: this.loginFieldName,
                    pwdFieldName: this.pwdFieldName,
                    
            		authFailure: this.authFailure, 
            		rememberMe: this.rememberMe,
                    forgotPassword: this.forgotPassword
            	}),
            	{
            		xtype:'tbseparator',
            		flex:1
            	},
            	Ext.create('Ametys.public.NewsDialog', {})
            ]
        }]
    	
    	this.callParent(arguments);
    }
});


