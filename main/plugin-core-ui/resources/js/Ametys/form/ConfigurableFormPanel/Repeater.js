/*
 *  Copyright 2016 Anyware Services
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
 * This class provides a repeater container
 * @private
 */
Ext.define('Ametys.form.ConfigurableFormPanel.Repeater',
{
    extend: 'Ext.panel.Panel',
    alias: 'widget.cms.repeater',
    
    statics: {
        /**
         * @property {Number} PADDING The padding of repeater
         * @private
         * @readonly 
         */
    	PADDING: 5,
    	
        /**
         * @property {Number} NESTED_OFFSET The left offset when nesting repeaters
         * @private
         * @readonly 
         */
        NESTED_OFFSET: 20,
        /**
         * @property {RegExp} HEADER_VARIABLES Regular expression used to extract used metadatas from the header template.
         * @private
         * @readonly 
         */
        HEADER_VARIABLES: /\{([^\}]+)\}/gi
    },
    
    /**
     * @cfg {String} label The repeater label.
     */
    /**
     * @cfg {Ametys.form.ConfigurableFormPanel} form The parent form panel
     */
    /**
     * @cfg {String} addLabel The add button label.
     */
    /**
     * @cfg {String} delLabel The delete button label.
     */
    /**
     * @cfg {String} headerLabel The item panel header label template.
     */
    /**
     * @cfg {Boolean} readOnly True to disallow add, delete and move actions
     */
    /**
     * @cfg {Number} minSize The repeater min size.
     */
    /**
     * @cfg {Number} maxSize The repeater max size.
     */
    /**
     * @cfg {HTMLElement} compositionNode The repeater composition as XML node.
     */
    /**
     * @cfg {Object} composition The repeater composition as JSON object.
     */
    /**
     * @cfg {Object/Object[]} fieldCheckers The field checkers of this repeater as a JSON object
     */
    /**
     * @cfg {String} prefix The metadata prefix (to create sub elements)
     */
    /**
     * @cfg {Number} nestingLevel The nesting level of the repeater.
     */

    /**
     * @cfg {String} invalidCls The CSS class to use when marking the repeater invalid.
     */
    invalidCls: 'a-repeater-invalid',
    
    /**
     * @cfg {String} defaultPathSeparator='.' The default separator for fields
     */
    defaultPathSeparator: '.',
    
    /**
     * @cfg {String/String[]/Ext.XTemplate} activeErrorsTpl The template used to format the Array of error messages.
     * It renders each message as an item in an unordered list.
     */
    activeErrorsTpl: [
                          '<tpl if="errors && errors.length">',
                              '<ul class="{listCls}">',
                                  '<tpl for="errors"><li>{.}</li></tpl>',
                              '</ul>',
                          '</tpl>'
                      ],
                      
    
    /**
     * @property {Number} _lastInsertItemPosition The last inserted item was at this position
     * @private
     */                      
                      
	/**
     * @property {Boolean} isRepeater
     * Flag denoting that this component is a Repeater. Always true.
     */
	isRepeater : true,
    
    constructor: function(config)
    {
        config = config || {};
        config.header = config.header || {};
        config.header.titlePosition = config.header.titlePosition || 1; 
        
        if (config.defaultPathSeparator)
        {
            this.defaultPathSeparator = config.defaultPathSeparator;
        }

        this.callParent(arguments);  
    },
    
    initComponent: function()
    {
        Ext.applyIf(this, {
            layout: {
                type: 'repeater-accordion',
                multi: true
            }
        });

        Ext.apply(this, {
            ui: 'light',
            border: false,
            shadow: false,
            isRepeater: true,
            
            cls: 'a-repeater a-repeater-level-' + this.nestingLevel + (this.nestingLevel % 2 == 0 ? ' even' : ' odd'),
            
            margin: this.nestingLevel > 1 ? ('0 0 5 ' + Ametys.form.ConfigurableFormPanel.Repeater.NESTED_OFFSET) : '0 0 5 0',
            
            header: {
            	title: this.label + ' (0)',
                style: "border-width: 1px !important"
            },
            
            items: [{
                hidden: true,
                items: [{
                    xtype: 'numberfield',
                    name: '_' + this.prefix + this.name + '.size',
                    value: 0
                }]
            }],
            
            tools: this.readOnly ? null : [
                this._addFirst(this.addLabel)
            ]
        });
        
        if (this.headerLabel)
        {
            // Compile the header template and extract the metadata names.
            this._headerTpl = new Ext.Template(this.headerLabel, {compiled: true});
            this._headerFields = [];
            while ((result = Ametys.form.ConfigurableFormPanel.Repeater.HEADER_VARIABLES.exec(this.headerLabel)) != null)
            {
                this._headerFields.push(result[1]);
            }
        }
        
        // Monitor when the form is ready, to update panel headers accordingly.
        if (this.form)
        {
            this.form.on('formready', this._onFormReady, this);
            this.form.on('repeaterEntryReady', this._onRepeaterEntryReady, this);
        }
        
        /**
         * @event validitychange
         * Fires when an entry was added or deleted
         * @param {Ametys.form.ConfigurableFormPanel.Repeater} this
         * @param {Boolean} isValid Whether or not the repeater is now valid
         */
        
        this.callParent(arguments);
    },
    
    /**
     * Clear all the repeater items
     */
    reset: function()
    {
    	var items = this.getItems();
		for (var i = items.getCount() - 1; i >= 0; i--)
    	{
 	    	this.removeItem(items.getAt(i));
    	}
    },
    
    /**
     * Get the miminum size ie. the miminum amount of entries
     * @return {Number} The miminum size of the repeater
     */
    getMinSize: function ()
    {
    	return this.minSize;
    },
    
    /**
     * Add a new repeater item.
     * @param {Object} options The options.
     * @param {Number} options.position The index at which the Component will be inserted into the Container's items collection. The position is 0-based. Can be null to add at the end.
     * @param {Number} options.previousPosition The panel previous position.
     * @param {Boolean} [options.collapsed=true] Whether to render the panel collapsed or not.
     * @param {Boolean} [options.animate=true] `true` to animate the panel, `false` otherwise.
     * @return The panel corresponding to the new instance
     */
    createRepeaterItemPanel: function (options)
    {
        var opt = options || {};
        
        var pos = Ext.isNumber(opt.position) ? opt.position : this.getItemCount();
        this._lastInsertItemPosition = pos + 1;
    	
    	var item = Ext.create('Ext.Panel', {
    		title: this.label + ' (' + (pos+1) + ')',
    		minTitle: this.label,
	    	layout: {
	    		type: 'anchor'
	    	},
	    	anchor: '100%',

    		bodyPadding: Ametys.form.ConfigurableFormPanel.Repeater.PADDING + ' ' + Ametys.form.ConfigurableFormPanel.Repeater.PADDING + ' 0 ' + Ametys.form.ConfigurableFormPanel.Repeater.PADDING,
    		
    		index: pos,   // Index in the item panels list, 0-based.
    		
            ui: 'light',
    		titleCollapse: true,
    		hideCollapseTool: false,
    		collapsible: true,
            header: {
                titlePosition: 1
            },
            border: true,
    		
            collapsed: opt.collapsed !== false,  // Render the items collapsed by default
    		
    	    cls: 'a-repeater-item a-repeater-item-level-' + this.nestingLevel + (this.nestingLevel % 2 == 0 ? ' even' : ' odd'),
    	    
    	    // The tools
    	    tools: this.readOnly ? null : [
                // collapse tool (automatically added)
                this._upTool(),
                this._downTool(),
                this._addTool(this.addLabel),
                this._deleteTool(this.delLabel)
    	    ],
    	    
    	    items: [{
    	    	xtype: 'numberfield',
    	    	name: '_' + this.prefix + this.name + this.defaultPathSeparator + (pos + 1) + '.previous-position',
    	    	value: Ext.isNumber(opt.previousPosition) ? opt.previousPosition + 1 : -1,
    	    	hidden: true
    	    }],
    	    
    	    listeners: {
    	        add: {fn: this._onAddComponent, scope: this}
    	    }
    	});
    	
    	if (Ext.isNumber(opt.position))
	    {
    	    var items = this.getItems();
    	    
    	    // Shift the items after the insert position.
    	    for (var i = items.getCount() - 1; i >= opt.position; i--)
	    	{
    	    	var itemPanel = items.getAt(i);
    	    	
                this._increaseIndexOfFields(itemPanel.index + 1);
                itemPanel.index = itemPanel.index + 1;
                this._updatePanelHeader(itemPanel);
	    	}
    	    
    	    // Position is 0 -> insert at position #1 to insert after the size field.
    	    this.insert(opt.position + 1, item);
	    }
    	else
	    {
    	    this.add(item);
	    }
    	
    	this.incrementItemCount();
    	this.getHeader() instanceof Ext.panel.Header ? this.getHeader().hide() : this.getHeader().hidden = true;
    	return item;
    },
    
    /**
     * Draw the repeater fields
     * @param {Ext.Panel} panel The item panel.
     * @param {Object} options Options.
     */
    drawRepeaterItemFields: function(panel, options)
    {
        var opt = options || {};
        
        var index = panel.index + 1;
        
        // Transmit offset + 20 (margin) + 5 (padding) + 1 (border).
        var offset = this.offset 
        			+ Ametys.form.ConfigurableFormPanel.Repeater.PADDING 
        			+ 1 + (this.nestingLevel > 1 ? Ametys.form.ConfigurableFormPanel.Repeater.NESTED_OFFSET : 0);
        var roffset = this.roffset 
					+ Ametys.form.ConfigurableFormPanel.Repeater.PADDING 
					+ 1;
        // Draw the fields.
        
        if (this.compositionNode)
        {
        	Ext.defer(this.form._configureXML, 0, this.form, [this.compositionNode, this.prefix + this.name + this.defaultPathSeparator + index + this.defaultPathSeparator, panel, offset, roffset]);
        }
        else
        {
        	Ext.defer(this.form._configureJSON, 0, this.form, [this.composition, this.prefix + this.name + this.defaultPathSeparator + index + this.defaultPathSeparator, panel, offset, roffset]);
        }
        
        if (this.fieldCheckers)
    	{
        	this.form._fieldCheckersManager.addFieldCheckers(this.items.get(index), this.fieldCheckers, this.prefix + this.name + this.defaultPathSeparator + index + this.defaultPathSeparator, offset, roffset);
    	}

        // Default to true
//        panel.expand(opt.animate !== false);
    },
    
    /**
     * Add a new repeater item.
     * @param {Object} options The item panel.
     * @param {Boolean} scrollTo=false When true, the form will scroll to this new element
     * @return {Ext.panel.Panel} The newly created repeater item panel.
     */
    addRepeaterItem: function(options, scrollTo) // function(position)
    {
		// Suspend layout update.
    	this.form.suspendLayouts();
        var options = options || {};
        
        if (options.fireRepeaterEntryReadyEvent)
        {
            this.form.notifyAddRepeaterEntry(true);
        }
        
        // Create the item panel.
        var itemPanel = this.createRepeaterItemPanel(options);
        
        // Draw the item fields.
        this.drawRepeaterItemFields(itemPanel, options);
        
        this.getHeader() instanceof Ext.panel.Header ? this.getHeader().hide() : this.getHeader().hidden = true;
        
        // Update the tools visibility.
        if (itemPanel.rendered)
        {
            this._updateToolsVisibility();
            if (scrollTo)
            {
            	this._scrollToNewPanel(itemPanel);
            }
        }
        else
        {
            itemPanel.on('render', this._updateToolsVisibility, this);
            if (scrollTo)
            {
            	itemPanel.on('boxready', this._scrollToNewPanel, this);
            }
        }
        
        // Resume layout update and force to recalculate the layout.
        this.form.resumeLayouts(true);
        
        if (options.fireRepeaterEntryReadyEvent)
        {
            this.form.notifyAddRepeaterEntry(false);
        	this.form.fireEvent('repeaterEntryReady', this);
        }
        
        return itemPanel;
    },
    
    /**
     * @private
     * Scroll the form to make the new panel visible
     * @param {Ext.panel.Panel} panel The new panel to scroll to
     */
    _scrollToNewPanel: function(panel)
    {
    	var newHeight = panel.getHeight();
    	var newTop = panel.getPosition()[1]; // get the bottom of the previous
    	
    	var visibleTop = this.form.getPosition()[1];
    	var visibleSize = this.form.getHeight();

    	var newPos = Math.min((newTop + newHeight) - (visibleTop + visibleSize) + 10, newTop-visibleTop);
    	if (newPos > 0)
    	{
    		this.form.scrollBy(0, newPos);
    	}
    },
    
    /**
     * Remove a repeater entry.
     * @param {Ext.panel.Panel} itemPanel The item panel to remove.
     */
    removeItem: function(itemPanel)
    {
        // Position of the panel to delete
        var position = itemPanel.index;
        
        // Remove the repeater item.
        this.remove(itemPanel);
        
        this._removeFields(position + 1);
        
        // Get the next panel in repeater (the panel has been already removed => position + 1).
        var itemPanel = this.items.getAt(position + 1);
        while (itemPanel != null)
        {
            // Update the repeater fields index.
            this._decreaseIndexOfFields(itemPanel.index + 1);
            itemPanel.index = itemPanel.index - 1;
            this._updatePanelHeader(itemPanel);
            
            itemPanel = itemPanel.nextSibling();
        }
        
        // Decrease the repeater size.
        this.decrementItemCount();
        this._updateToolsVisibility();
        
        // Show the repeater header if there is no more entry.
        if (this.getItemCount() < 1)
        {
            this.rendered ? this.getHeader().show() : this.getHeader().hidden = false;
        }
    },
    
    /**
     * Get the repeater items.
     * @return {Ext.util.MixedCollection} the repeater items.
     */
    getItems: function()
    {
        // Filter the hidden panels.
        return this.items.filterBy(function(panel, key) {
            return !panel.isHidden();
        });
    },
    
    /**
     * Get the repeater item count.
     * @return {Number} the repeater item count.
     */
    getItemCount: function()
    {
        // Remove 1 for the hidden count field.
        return this.items.getCount() - 1;
    },
    
    /**
     * Get the repeater's label
     * @return {String} the label
     */
    getLabel: function ()
    {
    	return this.label;
    },
    
    /**
     * Returns whether or not the repeater is currently valid
     * @return True if the repeater is valid, else false
     */
    isValid: function ()
    {
    	var errors = this.getErrors(),
    		isValid = Ext.isEmpty(errors);
    	
    	if (isValid)
        {
    		this.clearInvalid();
        }
    	else
    	{
    		this.markInvalid(errors);
    	}
    	
    	return isValid;
    },
    
    /**
     * Returns whether or not the repeater is currently valid, 
     * and fires the {@link #validitychange} event if the repeater validity has changed since the last validation.
     *
     * @return {Boolean} True if the repeater is valid, else false
     */
    validate : function()
    {
        var me = this,
            isValid = me.isValid();
        
        if (isValid !== me.wasValid) {
            me.wasValid = isValid;
            me.fireEvent('validitychange', me, isValid);
        }
        
        return isValid;
    },

    
    /**
     * Runs repeater's validations and returns an array of any errors
     * @return {String[]} All validation errors for this field
     */
    getErrors: function ()
    {
    	var errors = [];
    	
    	if (this.minSize != null && this.getItemCount() < this.minSize)
        {
    		errors.push("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_INSERT_INVALID_MINSIZE}}: " + this.getItemCount() + '/' +  this.minSize);
        }
    	
    	if (this.maxSize != null && this.getItemCount() > this.maxSize)
        {
    		errors.push("{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_INSERT_INVALID_MAXSIZE}}: " + this.getItemCount() + '/' +  this.maxSize);
        }
    	
    	return errors;
    },
    
    /**
     * Display one or more error messages associated with this repeater
     * @param {String/String[]} errors The validation message(s) to display.
     */
    markInvalid: function (errors)
    {
    	errors = Ext.Array.from(errors);
    	
    	var me = this,
    		hasError = !Ext.isEmpty(errors);
    	
    	if (me.rendered && !me.isDestroyed) 
    	{
    		var tpl = me.getTpl('activeErrorsTpl');
    		
    		var activeError = tpl.apply({
                errors: errors,
                listCls: Ext.plainListCls + ' a-repeater-invalid-tooltip'
            });
    		
            if (me.getItemCount() > 0)
            {
            	me.getItems().each(function(panel, index, length) {
                	
                	// Remove old errors if exists
                	if (panel.tools.error)
              		{
                		panel.getHeader().remove(panel.tools.error);
              		}
                	
                	if (hasError)
                	{
                		me.clearErrorMessages(panel.getHeader() || panel.header);
                		
                		panel.addTool({type: 'error', qtip: activeError});
                	}
                });
            }
            else
            {
            	me.clearErrorMessages(me.getHeader() || me.header);
           		me.addTool({type: 'error', qtip: activeError});
            }
        }
    	
    	this.el[hasError ? 'addCls' : 'removeCls'](this.invalidCls);
    },
    
    /**
     * @private 
     * Clear any error messages
     * @param {Ext.panel.Header} header The panel's header
     */
    clearErrorMessages : function (header)
    {
    	var tools = header.tools || [];
    	
    	var errorTools = header.items.filter('type', 'error');
    	if (errorTools != null)
    	{
    		errorTools.each (function (item) {
    			Ext.Array.remove(tools, item);
    			header.remove(item);
    		});
    	}
    },
    
    /**
     * Clear any invalid styles/messages for this repeater.
     */
    clearInvalid: function ()
    {
    	var me = this;
    	me.el['removeCls'](me.invalidCls);

    	if (me.getItemCount() > 0)
        {
    		me.getItems().each(function(panel, index, length) {
	       		 if (panel.tools.error)
	       		 {
	       			 panel.getHeader().remove(panel.tools.error);
	       		 }
	       	});
        }
    	else
    	{
    		if (me.getHeader().tools.error)
    		{
    			me.getHeader().remove(me.getHeader().tools.error);
    		}
    	}
    },
    
    /**
     * Expand all the repeater items.
     */
    expandAll: function()
    {
        var me = this;
        me.getItems().each(function(panel, index, length) {
            if (panel.rendered)
            {
                panel.expand();
            }
            else
            {
                panel.collapsed = false;
            }
        });
    },
    
    /**
     * Collapse all the repeater items.
     */
    collapseAll: function()
    {
        var me = this;
        me.getItems().each(function(panel, index, length) {
            if (panel.rendered && panel.isVisible(true))
            {
                panel.collapse();
            }
            else
            {
                panel.collapsed = true;
            }
        });
    },
    
    /**
     * @private
     * Get the field that holds the size of the repeater
     * @return {Ext.form.field.Field} The field
     */
    getSizeField: function()
    {
        return this.items.first().items.first();
    },
    
    /**
     * @private
     * Increment the value of the internal field holding the repeater size
     */
    incrementItemCount: function()
    {
        var sizeField = this.getSizeField();
        sizeField.setValue(sizeField.getValue() + 1);
    },
    
    /**
     * @private
     * Decrement the value of the internal field holding the repeater size
     */
    decrementItemCount: function()
    {
        var sizeField = this.getSizeField();
        sizeField.setValue(Math.max(sizeField.getValue() - 1, 0));
    },
    
    /**
     * Set an item's previous position field.
     * @param {Number} position the item's position in the repeater, 0-based.
     * @param {Number} previousPosition the item's previous position.
     */
    setItemPreviousPosition: function(position, previousPosition)
    {
        // Get the corresponding item.
        var item = this.getItems().getAt(position);
        // The item panel's first element is the previous position field.
        item.items.first().setValue(previousPosition + 1);
    },
    
    /**
     * Mark an item as new by modifying its previous position field.
     * @param {Number} position the item's position in the repeater, 0-based.
     */
    markItemAsNew: function(position)
    {
        // Get the corresponding item.
        var item = this.getItems().getAt(position);
        // The item panel's first element is the previous position field.
        item.items.first().setValue(-1);
    },
    
    /**
     * Hide or show the tools
     * @private
     */
    _updateToolsVisibility: function()
    {
    	if (!this.readOnly)
    	{
    		var me = this;
            
            // Iterate on repeater item panels.
            me.getItems().each(function(panel, index, length) {
                function setVisible(name, value) 
                {
    	            // The panel is rendered: tools is an object, items can be accessed by their name.
    	        	// The panel is not rendered yet: tools is a configuration array.
                    if (panel.tools[name])
                    {
                        panel.tools[name].setVisible(value);
                    }
                    else
                    {
                        Ext.Array.findBy(panel.tools, function(f) { return f.type == 'moveup'; }).hidden = !value;
                    }
                }
                
                setVisible("moveup", index > 0);
                setVisible("movedown", index < (length-1));
                setVisible("plus", me.maxSize == null || length < me.maxSize);
                setVisible("minus", me.minSize == null || length > me.minSize);
            });
    	}
    },
    
    /**
     * Decrease the index of repeater fields 
     * @param {Number} index The start index
     * @private
     */
    _decreaseIndexOfFields: function (index)
    {
        this._shiftIndexOfFields(index, -1);
    },
    
    /**
     * Increase the index of repeater fields 
     * @param {Number} index The start index
     * @private
     */
    _increaseIndexOfFields: function (index)
    {
        this._shiftIndexOfFields(index, 1);
    },
    
    /**
     * Shift the index of repeater fields 
     * @param {Number} index The start index
     * @param {Number} offset The offset to shift
     * @private
     */
    _shiftIndexOfFields: function (index, offset)
    {
        var me = this;
        var fieldsToRename = {};
    	
        // Shift standard fields.
        var prefix = me.prefix + me.name + me.defaultPathSeparator + index;
        var fieldNames = me.form.getFieldNames();
    	for (var i = 0; i < fieldNames.length; i++)
    	{
    		var fieldName = fieldNames[i];
    		if (fieldName.indexOf(prefix + me.defaultPathSeparator) == 0)
    		{
    			var field = me.form.getField(fieldName);
    			var newName = me.prefix + me.name + me.defaultPathSeparator + (index + offset) + me.defaultPathSeparator + fieldName.substring(prefix.length + 1);
    			me._setFieldName(field, newName);
    			
    			fieldsToRename[fieldName] = newName;
    		}
    	}
    	
    	// Shift hidden fields (which name starts with an underscore).
    	prefix = '_' + me.prefix + me.name + me.defaultPathSeparator + index;
    	me.form.getForm().getFields().each(function(formField) {
    	    if (formField.name && formField.name.indexOf(prefix + me.defaultPathSeparator) == 0)
	        {
    	        var newName = '_' + me.prefix + me.name + me.defaultPathSeparator + (index + offset) + me.defaultPathSeparator + formField.name.substring(prefix.length + 1);
    	        me._setFieldName(formField, newName);
	        }
	    });
    	
    	for (var oldName in fieldsToRename)
		{
    		me.form._onRenameField(oldName, fieldsToRename[oldName]);
		}
    },
    
    /**
     * Switch index of two repeater fields
     * @param {Number} index1 Index of first field
     * @param {Number} index2 Index of second field
     * @private
     */
    _switchIndexOfFields: function(index1, index2)
    {
        var me = this;
        var fieldsToRename = [];
    	
        // Switch standard fields.
        var prefix1 = me.prefix + me.name + me.defaultPathSeparator + index1;
        var prefix2 = me.prefix + me.name + me.defaultPathSeparator + index2;
        var fieldNames = me.form.getFieldNames();
    	for (var i = 0; i < fieldNames.length; i++)
    	{
    		var fieldName = fieldNames[i];
    		if (fieldName.indexOf(prefix1 + me.defaultPathSeparator) == 0)
    		{
    			var field = me.form.getField(fieldName);
    			var newName = me.prefix + me.name + me.defaultPathSeparator + index2 + me.defaultPathSeparator + fieldName.substring(prefix1.length + 1);
    			
    			fieldsToRename.push({index: i, field: field, newName: newName});
    		}
    		else if (fieldName.indexOf(prefix2 + me.defaultPathSeparator) == 0)
    		{
    			var field = me.form.getField(fieldName);
    			var newName = me.prefix + me.name + me.defaultPathSeparator + index1 + me.defaultPathSeparator + fieldName.substring(prefix2.length + 1);
    			
    			fieldsToRename.push({index: i, field: field, newName: newName});
    		}
    	}
    	
        // Switch hidden fields (which name starts with an underscore).
        prefix1 = '_' + me.prefix + me.name + me.defaultPathSeparator + index1;
        prefix2 = '_' + me.prefix + me.name + me.defaultPathSeparator + index2;
        me.form.getForm().getFields().each(function(formField) {
            if (formField.name && formField.name.indexOf(prefix1 + me.defaultPathSeparator) == 0)
            {
                var newName = '_' + me.prefix + me.name + me.defaultPathSeparator + index2 + me.defaultPathSeparator + formField.name.substring(prefix1.length + 1);
                me._setFieldName(formField, newName);
            }
            else if (formField.name && formField.name.indexOf(prefix2 + me.defaultPathSeparator) == 0)
            {
                var newName = '_' + me.prefix + me.name + me.defaultPathSeparator + index1 + me.defaultPathSeparator + formField.name.substring(prefix2.length + 1);
                me._setFieldName(formField, newName);
            }
        });
    	
    	for (var i = 0; i < fieldsToRename.length; i++)
		{
    		me._setFieldName(fieldsToRename[i].field, fieldsToRename[i].newName);
    		me.form._onRenameField(fieldsToRename[i].index, fieldsToRename[i].newName);
		}
    },
    
    /**
     * Remove the references to field names in the form.
     * @param {Number} index The index of the removed form item.
     * @private
     */
    _removeFields: function(index)
    {
        var fieldNames = this.form.getFieldNames();
        var fieldsToRemove = [];
        
        var prefix = this.prefix + this.name + this.defaultPathSeparator + index + this.defaultPathSeparator;
        for (var i = 0; i < fieldNames.length; i++)
        {
            if (fieldNames[i].indexOf(prefix) == 0)
            {
            	fieldsToRemove.push(fieldNames[i]);
            }
        }
        
        for (var i = 0; i < fieldsToRemove.length; i++)
    	{
        	this.form._onRemoveField(fieldsToRemove[i]);
    	}
    },
    
    /**
     * @private
     * Change a field name. Used when position of a repeater line has changed.
     * @param {Ext.form.field.Field} field The field to rename
     * @param {String} newName The new name of the field
     */
    _setFieldName: function(field, newName)
    {
    	field.name = newName;
    	
		if (typeof field.setName == 'function')
		{
			field.setName(newName);
		}
		
		var input = Ext.get(field.getInputId());
		if (input != null)
		{
			input.dom.name = newName;
		}
    },
    
    /**
     * Called when the owner form is ready, all its fields initialized and valued.
     * @param {Ametys.form.ConfigurableFormPanel} form The owner form.
     * @private
     */
    _onFormReady: function(form)
    {
        this._updateAllItemHeaders();
    },
    
    /**
     * Called when the an repeater entry has been added and is ready (all its fields initialized)
     * @param {Ametys.form.ConfigurableFormPanel.Repeater} repeater The repeater containing the entry.
     * @private
     */
    _onRepeaterEntryReady: function(repeater)
    {
        // updates only 
        if (repeater === this)
        {
            this._updateAllItemHeaders();
        }
    },
    
    /**
     * Called when a component is added to an item panel.
     * @param {Ext.panel.Panel} panel The container panel.
     * @param {Ext.Component} component The added component.
     * @param {Number} index The component index.
     * @private
     */
    _onAddComponent: function(panel, component, index)
    {
        // When a specific header label is specified, monitor when field values change.
        if (component.isFormField && this.headerLabel)
        {
            // Monitor only metadata the template is based on.
            var shortName = component.shortName;
            if (shortName && this._headerFields.indexOf(shortName) >= 0)
            {
                // When the field loses focus, update the header of its entry.
                component.on('change', Ext.bind(this._updatePanelHeader, this, [panel]), this);
            }
        }
    },
    
    /**
     * Update all item panel headers from their fields.
     * @private
     */
    _updateAllItemHeaders: function()
    {
        var me = this;
        me.getItems().each(function(panel, index, length) {
            me._updatePanelHeader(panel);
        });
    },
    
    /**
     * Update a panel header from its fields.
     * @param {Ext.panel.Panel} panel The panel which header to update.
     * @private
     */
    _updatePanelHeader: function(panel)
    {
        if (this.headerLabel)
        {
            var subFields = panel.query('> *[shortName]');
            var emptyValue = true;
            
            // Iterate over all the fields.
            var values = {};
            for (var i = 0; i < subFields.length; i++)
            {
                // Process only the fields which are used in the header template.
                var shortName = subFields[i].shortName;
                if (this._headerFields.indexOf(shortName) >= 0)
                {
                    // Get the value and test if it's empty.
                    var value = subFields[i].getReadableValue();
                    values[shortName] = value;
                    if (value != null && value != '')
                    {
                        emptyValue = false;
                    }
                }
            }
            
            var addTitle = this._headerTpl.apply(values);
            
            // Compute and set the new panel header/title.
            var newTitle = panel.minTitle + ' (' + (panel.index+1) + ')';
            if (!emptyValue && addTitle != '')
            {
                newTitle = newTitle + '<span class="header-repeater-item-value"> - ' + addTitle + '</span>';
            }
            
            panel.setTitle(newTitle);
        }
    },
    
    // ----------------------------------------------------
    /**
     * Creates the general 'add' tool with the given label
     * @private
     */
    _addFirst: function (label)
    {
        return {
            type: 'plus',
            qtip: label, 
            handler: this._add,
            scope: this
        };
    },
    
    /**
     * Creates the 'add' tool with the given label
     * @private
     */
    _addTool: function(label)
    {
    	return {
    		type: 'plus',
    		qtip: label, 
    		handler: this._insert,
    		scope: this
    	};
    },
    
    /**
     * Creates the 'delete' tool with the given label
     * @private
     */
    _deleteTool: function(label)
    {
    	return {
    	    type: 'minus',
    		qtip: label, 
    		handler: function(event, toolEl, header, tool) {
    		    // header.ownerCt returns the item panel.
    		    this._delete(header.ownerCt);
    		},
            scope: this
    	};
    },
    
    /**
     * Creates the 'move down' tool with the given label
     * @private
     */
    _downTool: function()
    {
    	return {
    	    type: 'movedown',
    		qtip: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_MOVE_DOWN}}", 
    		handler: this._down,
            scope: this
    	};
    },
    
    /**
     * Creates the 'move up' tool with the given label
     * @private
     */
    _upTool: function()
    {
    	return {
    	    type: 'moveup',
    		qtip: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_MOVE_UP}}", 
    		handler: this._up,
    		scope: this
    	};
    },
    
    // ----------------------------------------------------
    // Tool actions
    /**
     * Add a new repeater instance at the end of the list.
     * @param {Ext.event.Event} event The click event.
     * @param {Ext.Element} toolEl The tool Element.
     * @param {Ext.panel.Header} header The host panel header.
     * @param {Ext.panel.Tool} tool The tool object
     */
    _add: function(event, toolEl, header, tool)
    {
        if (this.maxSize != null && this.getItemCount() >= this.maxSize)
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_INSERT}}",
                msg: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_INSERT_ERROR_MAXSIZE}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
            return;
        }
        
        this.addRepeaterItem({collapsed: false, fireRepeaterEntryReadyEvent: true});
        this.validate();
    },
    
    /**
     * Insert a new repeater instance after the given panel.
     * @param {Ext.event.Event} event The click event.
     * @param {Ext.Element} toolEl The tool Element.
     * @param {Ext.panel.Header} header The host panel header.
     * @param {Ext.panel.Tool} tool The tool object
     */
    _insert: function(event, toolEl, header, tool)
    {
        if (this.maxSize != null && this.getItemCount() >= this.maxSize)
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_INSERT}}",
                msg: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_INSERT_ERROR_MAXSIZE}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
            return;
        }
        
        var panel = header.ownerCt;
        this.addRepeaterItem({position: panel.index + 1, collapsed: false, fireRepeaterEntryReadyEvent: true}, true);
        this.validate();
    },
    
    
    /**
     * Move down the given panel in its repeater
     * @param {Ext.event.Event} event The click event.
     * @param {Ext.Element} toolEl The tool Element.
     * @param {Ext.panel.Header} header The host panel header.
     * @param {Ext.panel.Tool} tool The tool object
     */
    _down: function(event, toolEl, header, tool)
    {
    	var itemPanel = header.ownerCt;
    	var index = itemPanel.index + 1;
    	
    	if (index >= this.getItemCount())
		{
    		return;
		}
    	
    	var items = this.getItems();
    	var itemPanel2 = items.getAt(itemPanel.index + 1);
    	
    	this.move(index, index + 1);
    	
    	// Switch all fields. Position fields will be updated as well.
    	this._switchIndexOfFields(index, index + 1);
        
    	var tmpIndex = itemPanel.index;
    	itemPanel.index = itemPanel2.index;
    	itemPanel2.index = tmpIndex;

        // Update title (that depends on position, but also on fields)
        this._updatePanelHeader(itemPanel);
        this._updatePanelHeader(itemPanel2);
    	
    	itemPanel.expand();
    	
        // Update tools
        this._updateToolsVisibility();
        
    },
    
    /**
     * Move up the given panel in its repeater
     * @param {Ext.event.Event} event The click event.
     * @param {Ext.Element} toolEl The tool Element.
     * @param {Ext.panel.Header} header The host panel header.
     * @param {Ext.panel.Tool} tool The tool object
     */
    _up: function(event, toolEl, header, tool)
    {
    	var itemPanel = header.ownerCt;
    	var index = itemPanel.index + 1;
    	
    	if (index <= 0)
		{
    		return;
		}
    	
    	var items = this.getItems();
    	var itemPanel2 = items.getAt(itemPanel.index - 1);
    	
    	this.move(index, index - 1);
    	
        // Switch all fields. Position fields will be updated as well.
    	this._switchIndexOfFields(index, index - 1);
    	
    	var tmpIndex = itemPanel.index;
    	itemPanel.index = itemPanel2.index;
    	itemPanel2.index = tmpIndex;
    	
        // Update title (that depends on position, but also on fields)
        this._updatePanelHeader(itemPanel);
        this._updatePanelHeader(itemPanel2);
    	
    	itemPanel.expand();
    	
        // Update tools
        this._updateToolsVisibility();
    },
    
    /**
     * @private
     * Removes a repeater entry.
     * @param {Ext.Panel} itemPanel The panel to delete
     */
    _delete: function(itemPanel)
    {
        if (this.minSize != null && this.getItemCount() <= this.minSize)
        {
            Ametys.Msg.show({
                title: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_DELETE}}",
                msg: "{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_DELETE_ERROR_MINSIZE}}",
                buttons: Ext.Msg.OK,
                icon: Ext.MessageBox.ERROR
            });
            return;
        }
        
        // Confirm deletion
        Ametys.Msg.confirm(
			"{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_DELETE}}",
			"{{i18n PLUGINS_CORE_UI_CONFIGURABLE_FORM_REPEATER_DELETE_CONFIRM}}",
			function (answer)
			{
				if (answer == 'yes')
				{
					// Remove the entry.
			        this.removeItem(itemPanel);
			        this.validate();
				}
			},
			this
		);
    }
    
});

/**
 * Repeater-specific accordion layout, which allows to collapse all items.
 * @private
 */
Ext.define('Ametys.cms.form.layout.Repeater', {
    extend: 'Ext.layout.container.Accordion',
    alias: ['layout.repeater-accordion'],
    
    /**
     * Overridden to prevent automatically expanding an item when another is collapsed.
     */
    onComponentCollapse: function(comp)
    {
        // Do nothing.
    }
    
    /*onContentChange: function () {
    	this.owner.updateLayout({isRoot: true});
    	return true;
    }*/ 
    
});
