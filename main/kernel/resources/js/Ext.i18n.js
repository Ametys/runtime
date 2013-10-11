/*
 *  Copyright 2013 Anyware Services
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

(function ()
{
	// Override SortType to add support for accented characters
	Ext.define("Ext.locale.data.SortTypes", {
        override: "Ext.data.SortTypes",
        
        /**
         * @member Ext.data.SortTypes
         * @method asNonAccentedUCString 
         * @since Ametys Runtime 3.7
         * @ametys
         * Case insensitive string (which takes accents into account)
         * @param {Object} s The value being converted
         * @return {String} The comparison value
         */
    	asNonAccentedUCString: function (s)
    	{
    		s = s.toLowerCase();
    		
    		s = s.replace(new RegExp(/[àáâãäå]/g),"a");
    		s = s.replace(new RegExp(/æ/g),"ae");
    		s = s.replace(new RegExp(/ç/g),"c");
    		s = s.replace(new RegExp(/[èéêë]/g),"e");
    		s = s.replace(new RegExp(/[ìíîï]/g),"i");
    		s = s.replace(new RegExp(/ñ/g),"n");
    		s = s.replace(new RegExp(/[òóôõö]/g),"o");
    		s = s.replace(new RegExp(/œ/g),"oe");
    		s = s.replace(new RegExp(/[ùúûü]/g),"u");
    		s = s.replace(new RegExp(/[ýÿ]/g),"y");

    		return Ext.data.SortTypes.asUCString(s);
    	}
    });
})();

/*
 * Supports for ametysDescription on fields and fields containers
 */
/**
 * @member Ext.form.field.Base
 * @ametys
 * @since Ametys Runtime 3.7
 * @cfg {String} ametysDescription A help image is added with the given description as a tooltip
 */
/**
 * @member Ext.form.FieldContainer
 * @ametys
 * @since Ametys Runtime 3.7
 * @cfg {String} ametysDescription A help image is added with the given description as a tooltip
 */
(function ()
{
	/*
	 * Support of warning message and ametys description on fields
	 */
	/**
     * Use a custom QuickTip instance separate from the main QuickTips singleton, so that we
     * can give it a custom frame style. Responds to errorqtip rather than the qtip property.
     * @static
     */
    
	var renderFn = {
	    onRender: function ()
	    {
	    	this.callParent(arguments); 
			
			var td = this.el.query(".ametys-warning")[0];
			if (td != null)
			{
				td.parentNode.appendChild(td); // move it as last
			}
			
			var td = this.el.query(".ametys-description")[0];
			if (td != null)
			{
				td.parentNode.appendChild(td); // move it as last
			}
	    }
	};
	
	Ext.define("Ametys.form.field.Base", Ext.apply(Ext.clone(renderFn), { override: 'Ext.form.field.Base'}));
	Ext.define("Ametys.form.FieldContainer", Ext.apply(Ext.clone(renderFn), { override: 'Ext.form.FieldContainer'}));
	
	
    var ametysLabelable =  {
        afterSubTpl: [ '</td><td class="ametys-warning" data-warnqtip="" style="display: none">',
                       /*'<td role="presentation" id="{id}-sideWarnCell" style="display:none" width="20px">',
                       		'<div role="presentation" id="{id}-warnEl" style="display:none"></div>',
                       '</td>',*/
	                    '<tpl if="ametysDescription">',
                    	'</td>',
                    	'<td class="ametys-description" data-qtip="{ametysDescription}">',
                    	'</tpl>'
        ], 
        
        getLabelableRenderData: function () 
    	{
    		var data = this.callParent(arguments);
    		data.ametysDescription = this.ametysDescription;
    		
    		this.getInsertionRenderData(data, this.labelableInsertions);
    		
    		return data;
    	},
        
        /**
         * @member Ext.form.field.Field
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * @cfg {String} warningCls
         * The CSS class to use when marking the component has warning.
         */
        warningCls : Ext.baseCSSPrefix + 'form-warning',
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * @cfg {String/String[]/Ext.XTemplate} activeWarnsTpl
         * The template used to format the Array of warnings messages passed to {@link #setActiveWarnings} into a single HTML
         * string. It renders each message as an item in an unordered list.
         */
   		activeWarnsTpl: [
              '<tpl if="warns && warns.length">',
                  '<ul class="{listCls}"><tpl for="warns"><li role="warn">{.}</li></tpl></ul>',
              '</tpl>'
        ],
        
        
        initLabelable: function ()
        {
        	this.callParent(arguments);
        	
        	this.addEvents(
                /**
                 * @event warningchange
                 * Fires when the active warning message is changed via {@link #setActiveWarning}.
                 * @param {Ext.form.Labelable} this
                 * @param {String} warning The active warning message
                 */
                'warningchange'
            );
        },
                      
	    /**
	     * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
	     * Gets an Array of any active warning messages currently applied to the field. 
	     * @return {String[]} The active warning messages on the component; if there are no warning, an empty Array is
	     * returned.
	     */
    	getActiveWarning: function ()
    	{
    		return this.activeWarn;
    	},
    	
    	/**
    	 * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * Gets an Array of any active warning messages currently applied to the field. 
         * @return {String[]} The active warning messages on the component; if there are no warnings, an empty Array is
         * returned.
         */
    	getActiveWarnings: function() {
            return this.activeWarns || [];
        },
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * Tells whether the field currently has an active warning message. 
         * @return {Boolean}
         */
        hasActiveWarning: function() {
            return !!this.getActiveWarning();
        },
    	
    	/**
    	 * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * Sets the active warning message to the given string. 
         * This replaces the entire warning message contents with the given string. 
         * Also see {@link #setActiveWarnings} which accepts an Array of messages and formats them according to the
         * {@link #activeWarnsTpl}. 
         * @param {String} msg The warning message
         */
        setActiveWarning: function (msg) 
        {
        	this.setActiveWarnings(msg);
        },

        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * Set the active warning message to an Array of warning messages. The messages are formatted into a single message
         * string using the {@link #activeWarnsTpl}. Also see {@link #setActiveWarning} which allows setting the entire warning
         * contents with a single string. 
         * @param {String[]} warns The warning messages
         */
        setActiveWarnings: function (warns)
        {
        	warns = Ext.Array.from(warns);
            this.activeWarn = warns[0];
            this.activeWarns = warns;
            
            this.activeWarn = Ext.XTemplate.getTpl(this, 'activeWarnsTpl').apply({
            	warns: warns,
                listCls: Ext.plainListCls 
            });
            
            this.renderActiveWarning();
        },
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * Clears the active warning message(s). Note that this only clears the warning message element's text and attributes,
         * you'll have to call doComponentLayout to actually update the field's layout to match. If the field extends {@link
         * Ext.form.field.Base} you should call {@link Ext.form.field.Base#clearInvalid clearInvalid} instead.
         */
        unsetActiveWarnings: function ()
        {
        	delete this.activeWarn;
        	delete this.activeWarns;
        	this.renderActiveWarning();
        },
    	
    	/**
         * @private
         * Updates the rendered DOM to match the current activeWarn. This only updates the content and
         * attributes, you'll have to call doComponentLayout to actually update the display.
         */
        renderActiveWarning: function() 
        {
        	Ext.form.Labelable.initWarnTip();
        	
            var me = this,
                activeWarn = me.getActiveWarning(),
                hasWarn = !!activeWarn;

            if (activeWarn !== me.lastActiveWarn) {
                me.fireEvent('warningchange', me, activeWarn);
                me.lastActiveWarn = activeWarn;
            }
            
            if (me.rendered && !me.isDestroyed && !me.preventMark) 
            {
                // Add/remove invalid class
                me.el[hasWarn ? 'addCls' : 'removeCls'](me.warningCls);

                var warnEl = this.el.query(".ametys-warning")[0];
                if (warnEl) {
                	warnEl.style.display = hasWarn ? '' : 'none';
                	warnEl.setAttribute("data-warnqtip", activeWarn);
                }
            }
        },
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.7
         * 
         * Hide the active warning message(s). Note that this only hides the warning message(s). The active warning message(s) are not cleared. 
         * Then you could call #renderActiveWarning method to show the warning message(s).
         * If you want to delete the active warning message(s) you should call #clearInvalid instead.
         */
        hideActiveWarning: function ()
        {
        	var me = this,
        		activeWarn = me.getActiveWarning(),
        		hasWarn = !!activeWarn;
        	
        	if (hasWarn && me.rendered && !me.isDestroyed && !me.preventMark) 
            {
        		me.el.removeCls (me.warningCls);
        		var warnEl = this.el.query(".ametys-warning")[0];
                if (warnEl) 
                {
                	warnEl.style.display = 'none';
                }
            }
        }
    };
    
	Ext.define("Ametys.form.Labelable", Ext.apply(Ext.clone(ametysLabelable), { 
		override: 'Ext.form.Labelable',
		
		statics: {
			/**
	         * Use a custom QuickTip instance separate from the main QuickTips singleton, so that we
	         * can give it a custom frame style. Responds to warnqtip rather than the qtip property.
	         * @static
	         */
			initWarnTip: function() {
		        var warnTip = this.warnTip;
		        if (!warnTip) {
		        	warnTip = this.warnTip = Ext.create('Ext.tip.QuickTip', {
		                baseCls: Ext.baseCSSPrefix + 'tip-form-warning'
		            });
		        	warnTip.tagConfig = Ext.apply({}, {attribute: 'warnqtip'}, warnTip.tagConfig);
		        }
		    },
		}
	}));
	Ext.define("Ametys.form.field.Base", Ext.apply(Ext.clone(ametysLabelable), { override: 'Ext.form.field.Base'}));
	Ext.define("Ametys.form.FieldContainer", Ext.apply(Ext.clone(ametysLabelable), { override: 'Ext.form.FieldContainer'}));
	 
    Ext.define("Ametys.form.field.Field", {
        override: "Ext.form.field.Field",
		        
		/**
		 * @member Ext.form.field.Field
		 * @ametys
		 * @since Ametys Runtime 3.7
		 * 
		 * Associate one or more warning messages with this field.
		 * @param {String/String[]} warns The warning message(s) for the field.
		 */
		markWarning: Ext.emptyFn,
		
		/**
		 * @member Ext.form.field.Field
		 * @ametys
		 * @since Ametys Runtime 3.7
		 * 
		 * Clear any warning styles/messages for this field.
		 */
		clearWarning: Ext.emptyFn
    });
    
    var ametysFieldBase = {
		/**
		 * @member Ext.form.field.Base
		 * @ametys
		 * @since Ametys Runtime 3.7
		 * 
		 * Associate one or more warning messages with this field.
		 * @param {String/String[]} warns The warning message(s) for the field.
		 */
		markWarning: function (warns)
		{
		    this.setActiveWarnings(Ext.Array.from(warns));
		    
		    if (this.hasActiveError())
	    	{
		    	// Hide active warning message(s)
	    		this.hideActiveWarning();
	    	}
		    
		    this.updateLayout();
		},
		
		/**
		 * @member Ext.form.field.Base
		 * @ametys
		 * @since Ametys Runtime 3.7
		 * 
		 * Clear any warning styles/messages for this field.
		 */
		clearWarning: function() 
		{
			this.unsetActiveWarnings();
			this.updateLayout();
		},
		
		/**
	     * @private 
	     * Overrides the method from the Ext.form.Labelable mixin to also add the warningCls to the inputEl
	     */
	    renderActiveWarning: function() 
	    {
	        var me = this,
	            hasWarn = !!me.getActiveWarning();
	        
	        if (me.inputEl) {
	            // Add/remove invalid class
	            me.inputEl[hasWarn ? 'addCls' : 'removeCls'](me.warningCls + '-field');
	        }
	        
	        me.mixins.labelable.renderActiveWarning.call(me);
	    },
	    
	    markInvalid: function ()
	    {
	    	if (this.hasActiveWarning())
	    	{
	    		// Hide active warning message(s) if exist
	    		this.hideActiveWarning();
	    	}
	    	this.callParent(arguments);
	    },
	    
	    clearInvalid: function ()
	    {
	    	this.callParent(arguments);
	    	
	    	if (this.hasActiveWarning())
	    	{
	    		// Display active warning message(s) if exist
		    	this.renderActiveWarning();
	    	}
	    }
    };
    
    Ext.define("Ametys.form.field.Base", Ext.apply(Ext.clone(ametysFieldBase), { override: 'Ext.form.field.Base'}));
    
})();
		        
/*
 * Support for optional label on text field to indicate field is multiple
 */
(function() 
{
    Ext.define("Ametys.form.field.Text", {
        override: "Ext.form.field.Text",
        
        getSubTplMarkup: function() {
        	var result = this.callParent(arguments);
        	
        	/**
        	 * @member Ext.form.field.Text
        	 * @ametys
        	 * @since Ametys Runtime 3.7
        	 * @cfg {Boolean} ametysShowMultipleHint=false true to show to multiple hint under the field. false by default
        	 */
        	if (this.ametysShowMultipleHint == true)
        	{
        		result += '<br/>'
        		    + '<span class="ametys-field-hint">(' + "<i18n:text i18n:key='KERNEL_MULTIPLE_HINT'/>" + ')</span>'
        	}
        	
        	return result;
        }
    });
})();

/*
 * Support for optional label on files to indicate max allowed size 
 */
(function() 
{
    Ext.define("Ametys.form.field.File", {
        override: "Ext.form.field.File",
        
        getTriggerMarkup: function() {
        	var result = this.callParent(arguments);
        	
        	/**
        	 * @member Ext.form.field.File
        	 * @ametys
        	 * @since Ametys Runtime 3.7
        	 * @cfg {Boolean} ametysShowMaxUploadSizeHint false to hide to max size hint under the field. true by default
        	 */
        	if (Ametys.MAX_UPLOAD_SIZE != undefined && Ametys.MAX_UPLOAD_SIZE != '' && this.ametysShowMaxUploadSizeHint !== false)
        	{
        		result += '</tr><tr id="' + this.id + '-uploadsize" class="ametys-file-hint"><td colspan="2">'
        		    + "(<i18n:text i18n:key='KERNEL_UPLOAD_HINT'/>"
        		    + Ext.util.Format.fileSize(Ametys.MAX_UPLOAD_SIZE)
        			+ ')</td>';
        	}
        	
        	return result;
        },
        
        // Override onFileChange method to prevent file path such as 'C:\fakepath\6_b.jpg' (IE, Chrome)
        onFileChange: function (button, e, value) {
        	
            this.callParent(arguments);
           
            var v = this.inputEl.dom.value;
            
            if (v.lastIndexOf('/') > 0)
            {
            	v = v.substring(v.lastIndexOf('/') + 1);
            }
            else if (v.lastIndexOf('\\') > 0)
            {
            	v = v.substring(v.lastIndexOf('\\') + 1);
            }
            
            this.inputEl.dom.value = v;
            
        }
    });
})();

/*
 * Support for background animation 
 */
(function () 
{
    /**
     * @member Ext.dom.Element
     * @method animate 
     * @since Ametys Runtime 3.7
     * @ametys
     * Ametys additionally handles `background-position` to animate a background-image and `background-position-step` to step this animation.
     * Both args are array of numbers with unit.
     * To right align, use '100%'.
     * 
     * The following example will animate the background image of the element to the coordinates 0,0. 
     * The animation will be "normal" on the x axis but will only use 256 multiples on the y axis.
     * 
     *     el.animate({ 
     * 			to: { 
     * 				'background-position': ['0px', '0px'], 
     * 				'background-position-step': ['1px', '256px'] 
     * 			}, 
     * 			duration: 500, 
     * 			easing: 'linear' 
     *     });
     */
	Ext.define('Ametys.fx.target.Element', {
		override: 'Ext.fx.target.Element',
		
	    getElVal: function(element, attr, val) 
	    {
	        if (val == undefined && attr === 'background-position') 
	        {
	        	var bgPos = element.getStyle("background-position");
	    		/^([^ ]*) ([^ ]*)$/.exec(bgPos);
	    		val = [ RegExp.$1, RegExp.$2 ];
	    		
	    		return val;
	        }
	        return this.callParent(arguments);
	    },
	    
	    setElVal: function(element, attr, value)
	    {
	        if (attr === 'background-position') 
	        {
	        	var anim = element.getActiveAnimation();
	        	var to = anim.to['background-position'];
	        	var by = anim.to['background-position-step']
	        	if (by == null)
	        	{
	        		by = [1, 1];
	        	}

	        	var roundedVal = [
	        	 	Math.round((parseInt(value[0]) - parseInt(to[0])) / parseInt(by[0])) * parseInt(by[0]) + parseInt(to[0]),
	        	 	Math.round((parseInt(value[1]) - parseInt(to[1])) / parseInt(by[1])) * parseInt(by[1]) + parseInt(to[1])
	        	];
	        	var units = [
	        	    value[0].replace(/[-.0-9]*/, ''),
	        	    value[1].replace(/[-.0-9]*/, ''),
	        	];

	        	element.setStyle("background-position", roundedVal[0] + units[0] + ' ' + roundedVal[1] + units[1]);
	        } 
	        else 
	        {
	        	this.callParent(arguments);
	        }
	    }
	});
})();

/*
 * Add a truncate method on TextMetrics 
 */
(function () 
{
	Ext.define('Ametys.util.TextMetrics', {
		override: 'Ext.util.TextMetrics',
		
    	/**
    	 * @member Ext.util.TextMetrics
    	 * @ametys
    	 * @since Ametys Runtime 3.7
    	 * Make an ellipsis on the provided text if necessary.
    	 * @param {String} text The text to test
    	 * @param {Number} maxWidth The max authorized with for this text
    	 * @param {String} [ellipsis='...'] The ellipsis text.
    	 * @returns {String} The text (potentially ellipsed) that fills in maxWidth. Returns an empty text, if the initial text is fully truncated.
    	 */
		ellipseText: function(text, maxWidth, ellipsis) 
		{
			if (text == null || text == '')
			{
				return '';
			}
			
			ellipsis = ellipsis || '...';
			
			if (this.getWidth(text) > maxWidth && maxWidth > 0)
			{
				var truncatedText = text;
				while (this.getWidth(truncatedText + ellipsis) > maxWidth)
				{
					truncatedText = truncatedText.substring (0, truncatedText.length -1);

					if (truncatedText == '')
					{
						return '';
					}
				}
				
				return truncatedText + ellipsis;
			}
			else
			{
				return text;
			}
		},
		
		/**
		 * @member Ext.util.TextMetrics
		 * @ametys
		 * @since Ametys Runtime 3.7
		 * Delimits the text given a number of lines and a maximal width.
		 * @param {String} text The text to delimit.
		 * @param {Number} maxWidth The max authorized with for each line.
		 * @param {Number} nbLines The max number of lines authorized.
		 * @param {String} ellipsis The possible ellipsis at the end of the text. Default to '...'
		 * @param {String/Boolean} hyphen The word break character used to for word that exceed the max width. False to disable hyphenation.
		 * @returns {String} The text (potentially ellipsed) that fills in maxWidth. Returns an empty text, if the initial text is fully truncated.
		 */
		delimitText: function(text, maxWidth, nbLines, ellipsis, hyphen)
		{
			if (!text)
			{
				return '';
			}
			
			if (!maxWidth)
			{
				return text;
			}
			
			nbLines = nbLines || 1;
			if (nbLines <= 1)
			{
				return this.ellipseText(text, maxWidth, ellipsis);
			}
			
			if (hyphen !== false)
			{
				hyphen = hyphen || '-';
			}
			
			// Cut text in lines.
			var lines = [];
			var nextIndex = -1;
			var nextWord = '';
			var currentLine = '';
				
			do
			{
				nextIndex = text.indexOf(' ');
				
				if (nextIndex != -1)
				{
					nextWord = text.substr(0, nextIndex);
					text = text.substring(nextIndex + 1);
				}
				else
				{
					nextWord = text;
					text = '';
				}
				
				// Add next word to current line, or create a new line depending on max width threshold.
				if (currentLine != '')
				{
					// test width of currentline + nextword
					if (this.getWidth(currentLine + ' ' + nextWord) <= maxWidth)
					{
						currentLine += ' ' + nextWord;
						nextWord = '';
					}
					else
					{
						lines.push(currentLine);
						currentLine = '';
					}
				}
				
				// Next hyphenation management.
				while (nextWord != '')
				{
					// Ensure next word is not exceeding max width
					if (this.getWidth(nextWord) > maxWidth)
					{
						nextWordTruncated = this.ellipseText(nextWord, maxWidth, hyphen);
						
						// If possible, add a new line with the next word truncated
						// Otherwise, just add the next word into the current line,
						// it will be ellipsed as the last line.
						if (lines.length < nbLines - 1)
						{
							lines.push(nextWordTruncated);
							
							// Calculate the rest of next word (if hyphenation is not disabled)
							if (hyphen !== false && Ext.String.endsWith(nextWordTruncated, hyphen))
							{
								nextWord = nextWord.substring(nextWordTruncated.length - hyphen.length);
							}
							else
							{
								nextWord = '';
							}
							
							currentLine = '';
						}
						else
						{
							currentLine = nextWord;
							nextWord = '';
						}
					}
					else
					{
						currentLine = nextWord;
						nextWord = '';
					}
				}
				
			} while (text != '' && lines.length < nbLines - 1);
				
			// Do not forget to re-include the current line into the text
			// because it must be taken into account when ellipsing the last line.
			if (currentLine != '')
			{
				text = text != '' ? currentLine + ' ' + text : currentLine;
			}
			
			// Add the last line which might be ellipsed.
			if (text != '' && lines.length < nbLines)
			{
				var lastLine = this.ellipseText(text, maxWidth);
				lines.push(lastLine);
			}
			
			return lines.join('<br/>');
		}
	});
})();

/*
 * Overriding some ux classes
 */
(function()
{
	/**
	 * Override of the {@link Ext.ux.DataView.Draggable} to handle multiselection with Drag operation.
	 * Also embed other minor tweaks, see {@link Ext.ux.DataView.Draggable} for comparison.
	 */
	Ext.define('Ametys.ux.DataView.Draggable', {
		override: 'Ext.ux.DataView.Draggable',
	
		/**
		 * @private
		 * Allow to override itemSelector ghost config.
		 * @param {Ext.view.View} dataview  The Ext.view.View instance that this DragZone is attached to
		 * @param {Object} config The configuration
		 */
		init: function(dataview, config) {
			this.callParent(arguments);
	
			Ext.apply(this.ghostConfig, {
				itemSelector: config.ghostConfig && config.ghostConfig.itemSelector || this.ghostConfig.itemSelector
			});
		},
		
		/**
		 * Tweaked from {@link Ext.ux.DataView.Draggable} (method getDragData,
		 * see source) to correctly handle multiselection.
		 * See {@link Ext.view.DragZone#getDragData}
		 * @param e
		 * @private
		 */
		getDragData: function(e) {
			var draggable = this.dvDraggable,
				dataview  = this.dataview,
				selModel  = dataview.getSelectionModel(),
				target    = e.getTarget(draggable.itemSelector),
				selected, records, dragData;
			
			// Do not allow drag with ctrl or shift modifier.
			if (!selModel.getCount() || e.ctrlKey || e.shiftKey) return false;
		
			// Modifying logic of this if-block, to allow multiselection.
			if (target)
			{
				// Target record must be draggable.
				var targetRecord = dataview.getRecord(target);
				if (targetRecord.get('allowDrag') === false)
				{
					return false;
				}
				
				// If target is not already selected,
				// select it (and only it)
				if (!dataview.isSelected(target)) {
					selModel.select(dataview.getRecord(target));
				}
				
				// Filters the records that are allowed to be dragged, and
				// cancel the event if no record can be dragged.
				records = selModel.getSelection();
				records = Ext.Array.filter(records, function(record)
				{
					return record.get('allowDrag') !== false;
				});
				
				if (Ext.isEmpty(records))
				{
					return false;
				}
				
				selected = Ext.Array.map(records, function(record) {
					return dataview.getNode(record);
				});
				
				dragData = {
					copy: true,
					nodes: selected,
					records: records,
					item: true
				};
				
				if (selected.length == 1) {
					dragData.single = true;
					dragData.ddel = target;
				} else {
					dragData.multi = true;
					dragData.ddel = draggable.prepareGhost(records).dom;
				}
		
				return dragData;
			}
		
			return false;
		}
	});
})();

(function ()
		{
			// Override SortType to add support for accented characters
			Ext.define("Ametys.dom.Query", {
		        override: "Ext.dom.Query",
		        
		        /**
		         * @member Ext.dom.Query
		         * @method selectDirectElements 
		         * @since Ametys Runtime 3.7
		         * @ametys
		         * Select a direct child element by a given name
		         * @param {String} element=* Name of the elements to limit.
		         * @param {HTMLElement} [node=document] The start of the query.
		         * @return {HTMLElement[]} An array of DOM elements
		         */
		    	selectDirectElements: function (element, node)
		    	{
		    		var selector = element || '*';
		    		
					var childNodes = Ext.dom.Query.select('> ' + selector, node);
					var elements = [];
					for (var i = 0; i < childNodes.length; i++)
					{
						// Test if Node.ELEMENT_NODE
						if (childNodes[i].nodeType == 1)
						{
							elements.push(childNodes[i]);
						}
					}
					return elements;
				}
		    });
})();

(function ()
		{
			Ext.define("Ametys.Base", {
				override: 'Ext.Base',
				
				inheritableStatics: {
					/**
					 * @private
					 * @property {Ametys.log.Logger} _logger The logger instance
					 */
					_logger: null,
					
					/**
					 * @member Ext.Base
					 * @method getLogger
					 * @static
					 * @ametys
					 * @since Ametys Runtime 3.7 
					 * Get the logger of this class (using classname as category)
					 * 
					 * 		try
					 * 		{
					 *      	if (this.getLogger().isDebugEnabled())
					 *      	{
					 *      		this.getLogger().debug("Starting process")
					 *      	}
					 *      
					 *      	...
					 *
					 *      	if (this.getLogger().isDebugEnabled())
					 *      	{
					 *      		this.getLogger().debug("Ending process")
					 *      	}
					 *      }
					 *      catch (e)
					 *      {
					 *      		this.getLogger().error({message: "Ending process", details: e});
					 *      }
					 * 
					 * @return {Ametys.log.Logger} The logger
					 */
					getLogger: function()
					{
						if (this._logger == null)
						{
							this._logger = Ametys.log.LoggerFactory.getLoggerFor(this.getName()) 
						}
						return this._logger;
					}
				},
				
				/**
				 * @member Ext.Base
				 * @method getLogger
				 * @ametys
				 * @since Ametys Runtime 3.7 
				 * Get the logger of this class (using classname as category)
				 * 
				 * 		try
				 * 		{
				 *      	if (this.getLogger().isDebugEnabled())
				 *      	{
				 *      		this.getLogger().debug("Starting process")
				 *      	}
				 *      
				 *      	...
				 *
				 *      	if (this.getLogger().isDebugEnabled())
				 *      	{
				 *      		this.getLogger().debug("Ending process")
				 *      	}
				 *      }
				 *      catch (e)
				 *      {
				 *      		this.getLogger().error({message: "Ending process", details: e});
				 *      }
				 * 
				 * @return {Ametys.log.Logger} The logger
				 */
				getLogger: function()
				{
					return this.self.getLogger();
				},
				
				destroy: function()
				{
					this._logger = null;
					this.callParent(arguments);
				}
			});
})();