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
 * This panel display a full screen for an action of redirection.
 * This is an example of use:
 * 
 * 
 * 		Ext.create('Ametys.public.RedirectActionScreen', {
 *             text: "My action title",
 *             description: "My action description",
 *             
 *             image: "/my/action/image.png",
 *             
 *             btnText: "Action",
 *             redirectUrl: "/my/action/url"
 *       });
 * 	
 */
Ext.define('Ametys.public.RedirectActionScreen', {
    extend: 'Ext.panel.Panel',
    
    /**
     * @cfg {String} text The main text
     */
    text: '',
    
    /**
     * @cfg {String} btnText The text of button to redirect
     */
    btnText: '',
    
    /**
     * @cfg {String} redirectUrl The URL to redirect to.
     */
    redirectUrl: '#',
    
    /**
     * @cfg {String} [description] A HTML description
     */
    description: '',
    
    /**
     * @cfg {String} [image] The path to the image illustration in size 128x128 pixels.
     */
    
    cls : 'ametys-public-page',
    
    title: "<i18n:text i18n:catalogue='application' i18n:key='APPLICATION_PRODUCT_LABEL'/>",
    titleAlign : "center",
    
    maximized : true,
    frameHeader : false,
    
    layout : {
      type : "vbox",
      align: "center",
      pack: "center"
    },
    
    initComponent: function ()
    {
    	this.items = [{
    	    xtype : "panel",
    	    bodyPadding: '30 20',
    	    width : "80%",
    	    cls : "ametys-public-page-inner-container",
    	    layout : {
    	      type : "vbox",
    	      align : "center",
    	      pack : "center"
    	    },
    	    items : [
    	        {
    	        	xtype : "label",
    	        	cls : "ametys-public-page-text",
    	        	text : this.text
	    	    }, 
	    	    this.getDescription(),
	    	    {
	    	    	hidden: Ext.isEmpty(this.image),
	    	    	xtype: 'image',
	    	    	cls : "ametys-public-page-img",
	    	    	src: this.image,
	    	        width: 128,
	    	        height: 128,
	    	        margin: '20 0 0 0'
	    	    },
	    	    {
	    	    	hidden: Ext.isEmpty(this.redirectText),
    	        	xtype : "label",
    	        	cls : "ametys-public-page-redirect-text",
    	        	text : this.redirectText
	    	    }, 
	    	    {
	      	      xtype : "button",
	      	      scale : "large",
	      	      margin: '20 0 0 0',
	      	      cls : "ametys-public-page-btn",
	      	      iconAlign : "right",
	      	      iconCls : "x-fa fa-angle-right",
	      	      text : this.btnText,
	      	      handler: function (btn) {
	      	    	  window.location.href = this.redirectUrl;
	      	      },
	      	      scope: this
	      	    }
    	    ]
    	}]
    	
    	this.callParent(arguments);
    },
    
    getDescription: function ()
    {
    	return {
	    	hidden: Ext.isEmpty(this.description),
	    	xtype : "label",
	    	cls : "ametys-public-page-desc",
	    	html : '<div>' + this.description + '</div>'
	    };
    }
});

