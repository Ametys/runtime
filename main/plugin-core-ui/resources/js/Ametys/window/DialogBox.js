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
 * A window dedicated for dialog box.
 * Hanlding 'return' key to validate the dialog box.
 * Initialized as modal, non-resizable and with a inside padding of 5 pixels.
 * 
 * <pre><code>
 * 	Ext.create("Ametys.window.DialogBox", {
 * 				title: 'Error 17',
 * 				icon: 'error_16.gif',
 * 
 * 				layout: 'form',
 * 				items : [
 * 					new Ext.Component({ html: 'test' }),
 * 					new Ext.form.field.Text(),
 * 					new Ext.form.field.TextArea()
 * 				],
 * 
 * 				width: 300,
 * 				height: 200,
 * 
 * 				buttons: [ 
 * 					{ 
 * 						text:'OK', 
 * 						handler: function() { 
 * 							this.up('dialog').hide(); 
 * 						}
 * 					}
 * 				]
 * 	}).show();
 * </code></pre>
 */
Ext.define(
	"Ametys.window.DialogBox", 
	{
		extend: 'Ext.window.Window',
		alias: 'widget.dialog',
        
		bodyPadding: '5',
		modal: true,
		resizable :false,
		cls: 'ametys-dialogbox',
		
		/**
		 * @cfg {Boolean} freezeHeight=false Set to 'true' to freeze the dialog height after first rendering. This option is useful for wizard to avoid that the dialog box changes its size during navigation. When using this option, do not set #cfg-height, but consider using #cfg-minHeight and #cfg-maxHeight.
		 */
		freezeHeight: false,
		
		constructor: function(config)
		{
			this.callParent(arguments);
			this.on('beforeshow', this._onBeforeShow, this);
			this.on('afterrender', this._onRenderListener, this);
			this.on('resize', this._onResize, this);
		},
		
		_onBeforeShow: function ()
		{
            this.center(); // This is the fix for RUNTIME-1823
            
			if (this.freezeHeight)
			{
				// Let the content decides of the dialog box's height
				this.setHeight(null);
			}
		},

		_onResize: function (window, width, height)
		{
			var oldHeight = this.getHeight();
			
			if (this.freezeHeight)
			{
				// Freeze the dialog's height
				this.setHeight(this.getHeight());
			}
            
    		this.center(); // auto center on resize (we do not test for changing size, since at startup height does not change and we do want to re-center non-fixed-height boxes)
		},
	
		/**
		 * @private
		 * Listener on after render to register the key ENTER to close the dialog
		 */
		_onRenderListener : function()
		{
		    var km = this.getKeyMap();
		    km.on(Ext.event.Event.ENTER, this.onEnter, this);
		    km.disable();
		},
		
	    /**
	     * @cfg {Function} onEnter
	     * Allows override of the built-in processing for the enter key. Default action is to call {@link #validateAction} when cursor is in an input, a div or a span.
	     * The attribute 'donotsubmitonenter' can be specified to avoid this behavior.
	     */

		onEnter : function(key, event)
		{
			// only works for input (plz avoid textarea) or div (when the focus is on the dialog itself) or span (when focus is on a tree node)
			// but we want to avoid all others cases (buttons for exemple - if not a button with focus may double click)
			if (/input|div|span/i.test(event.target.tagName) && event.target.getAttribute('donotsubmitonenter') == null)
			{
				event.stopEvent();
				this.validateAction();
			}			
		},
		
		/**
		 * The handler to validate the dialog box. Called by {@link #onEnter} for example
         * @template
		 */
		validateAction: function()
		{
			var buttons = this.getDockedItems('toolbar[dock="bottom"] button'); 
			if (buttons && buttons.length >= 1)
			{
				buttons[0].btnEl.dom.click();
			}
		}
	}
);
