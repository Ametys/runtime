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

Ext.define('Ametys.error.ErrorPage', {
    extend: 'Ext.panel.Panel',
    
    text: 'Erreur',
    description: '',
    details: '',

    cls : 'ametys-error-page',
    
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
    	    xtype : "container",
    	    width : 500,
    	    cls : "ametys-error-page-inner-container",
    	    layout : {
    	      type : "vbox",
    	      align : "center",
    	      pack : "center"
    	    },
    	    items : [{
    	      xtype : "label",
    	      cls : "ametys-error-page-text",
    	      text : this.text
    	    }, {
    	      xtype : "label",
    	      cls : "ametys-error-page-desc",
    	      html : '<div>' + this.description + '</div>'
    	    },
    	    ]
    	  }, 
    	  {
      		  xtype : "panel",
      		  itemId: 'details',
    	      cls : "ametys-error-page-details",
    	      hidden: Ext.isEmpty(this.details),
    	      width: '70%',
    	      
    	      title: "<i18n:text i18n:key='KERNEL_ERROR_DETAILS' i18n:catalogue='kernel'/>",
      		  collapsible: true,
    		  titleCollapse: true,
    		  animCollapse: true,
    		  titleAlign: 'right',
    		  collapsed: true,
      	      
      	      bodyPadding: '10 20',
      	      autoScroll: true,
      	      maxHeight: 400,
      	      border: false,
      	      html : '<div><pre><code>' + (Ext.isEmpty(this.message) ? '' : this.message + '<br/><br/>') + this.details + '</code></pre><div>'
      	  }]
    	
    	this.callParent(arguments);
    }
    
});


