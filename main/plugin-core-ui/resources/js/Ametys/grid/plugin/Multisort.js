/*
 *  Copyright 2014 Anyware Services
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
 * This plugin injects a multisort toolbar on top on the grid.
 * 
 * A column is adding to the sort by clicking on its header. The last clicked column is always the predominant sorter.
 * The sort fields can be reordered by drag and drop.
 * 
 * By default, the sort toolbar accepts a maximun of 3 sort fields.
 * 
 * Click on a the arrow of a sorter field, change the sort direction.
 */
Ext.define('Ametys.grid.plugin.Multisort', 
{
	extend: 'Ext.AbstractPlugin',
	alias: 'plugin.multisort',
	
	requires: [
	            'Ext.data.*',
	            'Ext.grid.*',
	            'Ext.util.*',
	            'Ext.toolbar.*',
	            'Ext.ux.ToolbarDroppable',
	            'Ext.ux.BoxReorderer'
               ],
	
    /**
   	 * @cfg {Number} maxNumberOfSortFields=3 the maximum number of accepted sorted fields
   	 */
   	maxNumberOfSortFields: 3,
   	
   	/** 
   	 * @private
   	 * @property {Ext.button.Button} _toggleButton The toggle button displayed in the columns header
   	 */
    
    /**
     * @private
     * @property {Number} closeItemButtonWidth The width used by buttons to close items
     */
    closeItemButtonWidth: null,
           	
    setCmp: function(gridConfig) 
    {
    	var me = this;
        
        this.callParent(arguments);
        
        var reorderer = Ext.create('Ext.ux.BoxReorderer', {
            listeners: {
                scope: this,
                'drop': function(r, c, container) { //sort on drop
                	this.doSort();
                }
            }
        });
        
        var droppable = Ext.create('Ext.ux.ToolbarDroppable', {
        	
        	destroy: function()
        	{
        		this.dropTarget.destroy();
        		this.dropTarget = null;
        	},
        	
    		// creates the new toolbar item from the header click event if possible
            createItem: function(column) 
            {
            	var columnCt = column.ownerCt,
            		grid = me.getCmp(),
            		toolbar = grid.getDockedItems('toolbar[dock="top"]')[0],
            		changePrimaryCriteria = false;
            	
            	var sortConfig = {
            		text: column.text,	
            		property: column.dataIndex,
                    direction: "ASC"
            	}
            	
            	this._insertSortItem(sortConfig);
            },
            
            /**
             * Inserts a sort item in multisort toolbar
             * @param {Object} sortConfig The sort configuration
             * @param {String} sortConfig.text The text
             * @param {String} sortConfig.property The sort property
             * @param {String} sortConfig.direction The sort direction
             * @private
             */
            _insertSortItem: function (sortConfig)
            {
            	var grid = me.getCmp(),
	        		toolbar = grid.getDockedItems('toolbar[dock="top"]')[0],
	        		changePrimaryCriteria = false;
            	
            	var sorterContainer =  me.createSorterConfig({
                    text: sortConfig.text,
                    sortData: {
                        property: sortConfig.property,
                        direction: sortConfig.direction
                    }
                });
            	
            	for (var i = 1; i < toolbar.items.length; i++)
                {
                	var currentSorterContainer = toolbar.items.get(i),
                	    sortButton = currentSorterContainer.items.get(0);
                	
                	if (sortButton.sortData.property == sortConfig.property)
            		{
                		// the sorter wasn't the primary criteria
            			if (i != 1)
        				{
            				changePrimaryCriteria = true;
        				}
                		
            			me._suspendDoSort = true;
                		me.changeSortDirection(sortButton, changePrimaryCriteria); 
                		me._suspendDoSort = false;
                		
                		toolbar.insert(1, currentSorterContainer);
                		return;
            		}
            	}

                toolbar.insert(1, sorterContainer);
                
                // limited to me.maxNumberOfSortFields search criterion
                if (toolbar.items.length > me.maxNumberOfSortFields + 1)
                {
                	me.destroyContainer(toolbar.items.get(me.maxNumberOfSortFields).getId());
            	}
            },
            
            /**
             * Initialize the multisort toolbar with the configured columns
             * @private
             */
            _initializeSortItems: function (grid)
            {
            	function getColumnText (grid, dataIndex) 
				{
					 var columns = grid.headerCt.getGridColumns();
					 for (var i = 0; i < columns.length; i++) 
					 {
						 if (columns[i].dataIndex == dataIndex) 
						 {
							 return columns[i].text;
						 }  
					 }
				}
				
				// Draw sort item in multisort toolbar from store's sorters
				var sorters = grid.getStore().getSorters();
				for (var i=sorters.length - 1; i >= 0; i--)
				{
					var columnTxt = getColumnText(grid, sorters.items[i].getProperty());
					if (columnTxt)
					{
						var sortConfig = {
    		            		text: columnTxt,	
    		            		property: sorters.items[i].getProperty(),
    		                    direction: sorters.items[i].getDirection()
    		            	};
    					
    					this._insertSortItem(sortConfig);
					}
				}
            },
            
            /**
            * Custom canDrop implementation which returns true if a column can be added to the toolbar
            * @param {Ext.dd.DragSource} dragSource The drag source
            * @param {Object} event The event
            * @param {Object} data Arbitrary data from the drag source. For a HeaderContainer, it will
            * contain a header property which is the Header being dragged.
            * @return {Boolean} True if the drop is allowed
            */
            canDrop: function(dragSource, event, data) {
            	var sorters = me.getSorters(),
                    header  = data.header,
                    length = sorters.length,
                    entryIndex = this.calculateEntryIndex(event),
                    targetItem = this.toolbar.getComponent(entryIndex),
                    i;
                
                // Group columns have no dataIndex and therefore cannot be sorted
                // If target isn't reorderable it could not be replaced
                if (!header.dataIndex || (targetItem && targetItem.reorderable === false)) {
                    return false;
                }
                
                for (i = 0; i < length; i++) {
                    if (sorters[i].property == header.dataIndex) {
                        return false;
                    }
                }
                return true;
            }
        });
        
        // add the toolbar with the 2 plugins
        gridConfig.dockedItems = Ext.Array.from(gridConfig.dockedItems);
        gridConfig.dockedItems.push({
        	ui: 'ametys-grid-multisort',
        	xtype: 'toolbar',
            itemId: 'grid-multisort',
        	hidden: true,
            border: gridConfig.border,
        	items  : [
	            {
	                xtype: 'tbtext',
	                text: "<i18n:text i18n:key='PLUGINS_CORE_UI_MULTISORT_TOOLBAR_TEXT'/>",
	                reorderable: false
            	}
        	],
            plugins: [reorderer, droppable]
        });
        
        gridConfig.listeners = gridConfig.listeners || {};
        gridConfig.listeners['columnschanged'] = {
        		scope: this,
        		fn: function(headerCt, eOpts) {
    				Ext.Array.each(headerCt.columnManager.getColumns(), function(column) {column.sort = Ext.bind(me.doSort, me, [droppable, column], false)});
        		}
        };
        
        gridConfig.listeners['reconfigure'] = {
        		scope: this,
        		fn: function(grid, store, columns) {
        			me._suspendDoSort = true;
    				droppable._initializeSortItems(grid);
    				me._suspendDoSort = false;
        		}
        };
        
        // Hide/show button
        gridConfig.listeners['viewready'] = {
        		scope: this,
        		single: true,
        		fn: function(grid, eOpts) {
        			var cmpId = grid.headerCt.getId() + '-' + Ext.id();

                    function onScrollMove(x, y) 
                    {
                        if (this.rendered) 
                        {
                            Ext.get(cmpId).dom.style.right = -x + "px";
                        }
                    }
        			grid.view.onScrollMove = grid.view.onScrollMove ? Ext.Function.createInterceptor(grid.view.onScrollMove, onScrollMove, grid) : onScrollMove;
        			
        			grid.headerCt.getEl().insertFirst({
					   id: cmpId,
				       tag: 'div', 
					   cls: "a-grid-multisort-toolbar-toggle-container"
				    });
        		
        			this._toggleButton = new Ext.button.Button({
        				renderTo: cmpId,
                        
                        iconCls: "a-grid-multisort-toolbar-toggle-img",
        				tooltip: "<i18n:text i18n:key='PLUGINS_CORE_UI_MULTISORT_SHOW_TOOLBAR_BUTTON_TOOLTIP'/>",
                        
	        			ui: 'ametys-grid-multisort-toggle',
                        
                        width: '100%',
                        height: '100%',
                        
                        enableToggle: true,
        				toggleHandler: function(btn, state) {
							me.getCmp().getDockedItems('toolbar[dock="top"]')[0].setVisible(state);
    						btn.setTooltip(state ? "<i18n:text i18n:key='PLUGINS_CORE_UI_MULTISORT_HIDE_TOOLBAR_BUTTON_TOOLTIP'/>" : "<i18n:text i18n:key='PLUGINS_CORE_UI_MULTISORT_SHOW_TOOLBAR_BUTTON_TOOLTIP'/>");
	        			}
        			});

        			// on resize event to get the proper height of the header container
    				grid.headerCt.on('resize', function(headerContainer) 
					{
    					Ext.get(cmpId).setHeight(headerContainer.el.getHeight(true));
					    Ext.get(cmpId).setWidth(Ext.getScrollbarSize().width);
				    });
    				
    				// initialize the multisort toolbar
    				droppable._initializeSortItems(grid);
        		}
        }
    },
    
    destroy: function()
    {
    	if (this._toggleButton)
    	{
    		this._toggleButton.destroy();
        	this._toggleButton = null;
    	}
    	
    	this.callParent(arguments);
    },
               
    /**
     * Returns an array of sortData from the sorter buttons
     * @return {Array} Ordered sort data from each of the sorter buttons
     */
    getSorters: function () 
    {
    	var sorters = [],
        	toolbar = this.getCmp().getDockedItems('toolbar[dock="top"]')[0];
        
        for (var i = 0; i < toolbar.items.length; i++)
    	{
        	if (toolbar.items.get(i).isXType('container'))
        	{
        		var sortButton = toolbar.items.get(i).items.get(0);
        		sorters.push(sortButton.sortData);
        	}
    	}	
    	
        return sorters;
    },
    
    /**
     * Convenience function for creating Toolbar Buttons that are tied to sorters
     * @param {Object} config Optional config object
     * @param {String} config.text the label of the element to be created
     * @param {Object} config.sortData the sort data
     * @param {String} config.sortData.property the label of the header
     * @param {String} config.sortData. direction equals 'ASC' || 'DESC'
     * @return {Object} The configuration of the Container with the 2 buttons and the label
     */
     createSorterConfig: function (config) 
     {
        var me = this,
        	containerId = Ext.id(),
        	config = config || {};
    	
        var deleteButtonConfig = ({
    		xtype: 'button',
            ui: 'ametys-grid-multisort-toolbar-item-remove',
            iconCls: 'a-grid-multisort-toolbar-item-remove-img',
            width: this.closeItemButtonWidth,
            
        	listeners: {
        		click: function(button, e) {
        			me.destroyContainer(containerId);
        		}
        	}

        });
        
        var sortButtonConfig = ({
            xtype: 'button',
            ui: 'ametys-grid-multisort-toolbar-item-sort',
            iconCls: 'a-grid-multisort-toolbar-item-sort-img',
            enableToggle: true,
            
            reorderable: true,
            sortData: config.sortData,
            pressed: config && config.sortData && config.sortData.direction == "DESC",
            
            listeners: {
                click: function(button, e) {
                    me.changeSortDirection(button, false);
                }
            }
        });
        
        var textConfig = Ext.create('Ext.Component', {
        	html : config.text,
        	cls: 'a-grid-multisort-toolbar-item-text'
        });

        return new Ext.container.Container({
        		id: containerId,
        		layout: {
        	        type: 'hbox',
    	        	defaultMargins: {
    	        		right: 3
    	        	}
        	    },
        	    cls: 'a-grid-multisort-toolbar-items-container',
        	    items: [sortButtonConfig, textConfig, deleteButtonConfig]
        	});
    },
    
    /**
     * Callback handler used when a sorter button is clicked or reordered
     * @param {Ext.Button} button The button that was clicked
     * @param {Boolean} changePrimaryCriteria true if the primary sorting criteria was changed, false otherwise
     */
    changeSortDirection: function (button, changePrimaryCriteria) 
    {
    	var sortData = button.sortData;
    	
        if (sortData)
        {
        	// when changing the primary criteria, the sorting order is ascending by default
        	if (changePrimaryCriteria)
    		{
    			sortData.direction = "ASC";
    		}
        	else
    		{
        		sortData.direction = Ext.String.toggle(sortData.direction, "ASC", "DESC");
    		}
        		
            button.toggle(sortData.direction != "ASC");
        	
    	    this.getCmp().getStore().clearFilter();
    	    
    	    if (!this._suspendDoSort)
    	    {
    	    	this.doSort();
    	    }
        }
    },
    
    /**
     * Sorts the grid and optionally the droppable toolbar createItem method if a grid header was clicked
     * @param {Ext.ux.ToolbarDroppable} droppable the droppable toolbar
     * @param {Ext.grid.column.Column} column the column whose header was clicked
     */
    doSort: function (droppable, column) 
    {
    	// header click
    	if (droppable)
		{
    		droppable.createItem(column);
		}
		this.getCmp().getStore().sort(this.getSorters());
    },
    
    /**
     * Destroys the container with the id containerId
     * @param {String} containerId the id of the container to destroy
     */
    destroyContainer: function(containerId)
    {
    	Ext.getCmp(containerId).destroy();
    	this.doSort();
    }
});
