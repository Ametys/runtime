/*
 * Copyright (c) 2008 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */

// Ametys Namespace
Ext.namespace('org.ametys');

/**
 * org.ametys.JsonTreeLoader
 *
 * @class org.ametys.JsonTreeLoader
 * @extends Ext.tree.TreeLoader
 * @constructor
 * @param {Object} config Configuration options
 */
org.ametys.JsonTreeLoader = function(config) 
{
	org.ametys.JsonTreeLoader.superclass.constructor.call(this, config);
}

Ext.extend(org.ametys.JsonTreeLoader, Ext.tree.TreeLoader, {
    processResponse : function(response, node, callback)
    {
        var json = response.responseText;
        try {
            var o = eval("("+json+")");
            
            o=eval('o.'+this.root);
            
            node.beginUpdate();
 
            for (var i = 0, len = o.length; i < len; i++)
            {
            
                var n = this.createNode(o[i]);
                
                if (n)
                {
                    node.appendChild(n);
                }
            }
            node.endUpdate();
            
            if(typeof callback == "function")
            {
                callback(this, node);
            }
        }
        catch(e)
        {
            this.handleFailure(response);
        }
    },
    
    createNode : function(attr)
    {
        	
        if(this.baseAttrs)
        {
            Ext.applyIf(attr, this.baseAttrs);
        }
        if(this.applyLoader !== false)
        {
            attr.loader = this;
        }
        if(typeof attr.uiProvider == 'string')
        {
           attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
        }
        
        //If this.nodeText is defined, use it for node text
        var nodeText = this.jsonNodeText;
        if (nodeText != '')
        {
        	attr.text = eval('attr.' + nodeText);
        }
        var nodeId = this.jsonNodeId;
        if (nodeId != '')
        {
        	attr.id = eval('attr.' + nodeId);
        }
        
        if (attr.icon == null)
        {
        	attr.icon = this.defaultIcon;
        }
       
        //If the attribute @leaf is present in JSON response, use it for leaf attribute
        if (attr.leaf)
        {
        	attr.leaf = true;
        }
        
        //Icone définie par défaut ?
        /*if (this.defaultIcon)
        {
        	attr.icon = this.defaultIcon;
        }
        */
        
        return new Ext.tree.AsyncTreeNode(attr);
                      //  new Ext.tree.TreeNode(attr) :
                      //  new Ext.tree.AsyncTreeNode(attr));
    },
    
    
    initComponent : function()
    {
    	org.ametys.JsonTreeLoader.superclass.initComponent.call(this);
    }    
});