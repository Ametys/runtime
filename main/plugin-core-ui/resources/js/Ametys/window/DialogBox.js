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
		 *  @cfg {Boolean} selectDefaultFocus=false Select the defaultFocus element in addition to focus it 
		 */
		selectDefaultFocus: false,
		
		/**
		 * @cfg {Boolean} freezeHeight=false Set to 'true' to freeze the dialog height after first rendering. This option is useful for wizard to avoid that the dialog box changes its size during navigation. When using this option, do not set #cfg-height, but consider using #cfg-minHeight and #cfg-maxHeight.
		 */
		freezeHeight: false,
		
		constructor: function(config)
		{
			this.callParent(arguments);
			this.on('beforeshow', this._onBeforeShow, this);
			this.on('resize', this._onResize, this);
		},
		
		focus: function()
		{
			if (arguments.length > 0)
			{
				arguments[0] = arguments[0] == null ? this.selectDefaultFocus : arguments[0]; 
				this.callParent(arguments);
			}
			else
			{
				this.callParent([this.selectDefaultFocus]);
			}
		},
		
        /**
         * @private
         * Listener on before show
         */
		_onBeforeShow: function ()
		{
            this.center(); // This is the fix for RUNTIME-1823
            
			if (this.freezeHeight)
			{
				// Let the content decides of the dialog box's height
				this.setHeight(null);
			}
		},

        /**
         * @private
         * Listener on resize
         * @param {Ametys.window.DialogBox} window The window
         * @param {Number} width The new width
         * @param {Number} height The new height
         */
		_onResize: function (window, width, height)
		{
			var oldHeight = this.getHeight();
			
			if (this.freezeHeight)
			{
				// Freeze the dialog's height
				this.setHeight(this.getHeight());
			}
            
    		this.center(); // auto center on resize (we do not test for changing size, since at startup height does not change and we do want to re-center non-fixed-height boxes)
		}
	}
);
