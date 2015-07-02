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
	Ext.override(Ext.String, {
        /**
         * @member Ext.String
         * @method deemphasize 
         * @since Ametys Runtime 3.9
         * @ametys
         * Convert a string value into a non accented string
         * @param {Object} s The value being converted
         * @return {String} The deemphasize value
         */
        deemphasize: function (s)
    	{
    		s = s.replace(new RegExp(/[ÀÁÂÃÄÅ]/g),"A");
    		s = s.replace(new RegExp(/[àáâãäå]/g),"a");
    		s = s.replace(new RegExp(/Æ/g),"AE");
    		s = s.replace(new RegExp(/æ/g),"ae");
    		s = s.replace(new RegExp(/Ç/g),"C");
    		s = s.replace(new RegExp(/ç/g),"c");
    		s = s.replace(new RegExp(/[ÈÉÊË]/g),"E");
    		s = s.replace(new RegExp(/[èéêë]/g),"e");
    		s = s.replace(new RegExp(/[ÌÍÎÏ]/g),"I");
    		s = s.replace(new RegExp(/[ìíîï]/g),"i");
    		s = s.replace(new RegExp(/Ñ/g),"N");
    		s = s.replace(new RegExp(/ñ/g),"n");
    		s = s.replace(new RegExp(/[ÒÓÔÕÖ]/g),"O");
    		s = s.replace(new RegExp(/[òóôõö]/g),"o");
    		s = s.replace(new RegExp(/Œ/g),"OE");
    		s = s.replace(new RegExp(/œ/g),"oe");
    		s = s.replace(new RegExp(/[ÙÚÛÜ]/g),"U");
    		s = s.replace(new RegExp(/[ùúûü]/g),"u");
    		s = s.replace(new RegExp(/[ÝŸ]/g),"y");
    		s = s.replace(new RegExp(/[ýÿ]/g),"y");

    		return s;
    	}
	});
})();
(function ()
{
	// Override SortType to add support for accented characters
    Ext.override(Ext.data.SortTypes, {
        /**
         * @member Ext.data.SortTypes
         * @method asNonAccentedUCString 
         * @since Ametys Runtime 3.9
         * @ametys
         * Case insensitive string (which takes accents into account)
         * @param {Object} s The value being converted
         * @return {String} The comparison value
         */
    	asNonAccentedUCString: function (s)
    	{
    		s = Ext.String.deemphasize(s.toLowerCase());
    		
    		return Ext.data.SortTypes.asUCString(s);
    	}
    });
})();

/*
 * Supports for ametysDescription ametysComment on fields and fields containers.
 */
/**
 * @member Ext.form.field.Base
 * @ametys
 * @since Ametys Runtime 3.9
 * @cfg {String} ametysDescription A help image is added with the given description as a tooltip
 */
/**
 * @member Ext.form.field.Base
 * @ametys
 * @since Ametys Runtime 3.9
 * @cfg {String} ametysComment A comment image is added with the given comment as a tooltip
 */
/**
 * @member Ext.form.FieldContainer
 * @ametys
 * @since Ametys Runtime 3.9
 * @cfg {String} ametysDescription A help image is added with the given description as a tooltip
 */
/**
 * @member Ext.form.FieldContainer
 * @ametys
 * @since Ametys Runtime 3.9
 * @cfg {String} ametysComment A comment image is added with the given comment as a tooltip
 */
(function ()
{
	/*
	 * Support of warning message and ametys description on fields
	 */
	
    
	var renderFn = {
	    /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * Get the readable value of a Field. The default implementation returns the same as #getValue.
         * Override this method to return an understandable value for more complex field (such as combobox, file input, ...). 
         * @template
         */
        getReadableValue: function ()
        {
        	return this.getValue();
        }
	};
	
	Ext.define("Ametys.form.field.Base", Ext.apply(Ext.clone(renderFn), { override: 'Ext.form.field.Base'}));
	Ext.define("Ametys.form.FieldContainer", Ext.apply(Ext.clone(renderFn), { override: 'Ext.form.FieldContainer'}));
	
	
    var ametysLabelable =  {
        afterOutterBodyEl: [  '<tpl if="renderWarning">',
                            '<div id="{id}-warningWrapEl" data-ref="warningWrapEl" class="ametys-warning" style="display: none"><div style="width: 20px;"></div></div>',
                        '</tpl>',
                        '<tpl if="ametysDescription">',
                            '<div id="{id}-descWrapEl" data-ref="descWrapEl" class="ametys-description" data-qtip="{ametysDescription}"><div style="width: 20px;"></div></div>',
                        '</tpl>',
                        '<tpl if="showAmetysComments == true">',
                            '<div id="{id}-commentWrapEl" data-ref="commentWrapEl" class="ametys-comments ametys-comments-empty" data-qtip=""><div style="width: 20px;"></div></div>',
                        '</tpl>'
        ],    
        
        initConfig : function(config)
        {
            config = config || {};
            config.childEls = config.childEls || [];
            config.childEls.push("warningWrapEl");
            config.childEls.push("descWrapEl");
            config.childEls.push("commentWrapEl");
            
            this.callParent(arguments);  
        },
        
        /**
         * @private
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * On render listener
         */
	    _onRenderLabelable: function()
	    {
	    	var sideErrorCell = Ext.get(this.getId() + "-sideErrorCell");
	    	if (sideErrorCell)
	    	{
	    		sideErrorCell.addCls(this.errorMsgCls + "-wrapper")
	    	}
	    },
        
        getInsertionRenderData: function(data, names)
        {
            data.ametysDescription = this.ametysDescription;
            data.showAmetysComments = this.showAmetysComments;
            data.renderWarning = true;
            
            return this.callParent(arguments);
        },
    	
        initLabelable: function ()
        {
        	this.callParent(arguments);
        	
        	this.on("render", this._onRenderLabelable, this);
        },
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * @event warningchange
         * Fires when the active warning message is changed via {@link #setActiveWarning}.
         * @param {Ext.form.Labelable} this
         * @param {String} warning The active warning message
         */
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * @event commentsupdated
         * Fires when the comments are updated via {@link #addComment} or {@link #addComments} or {@link #removeComments}.
         * @param {Ext.form.Labelable} this
         * @param {Object[]} comments The comments
         */

        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * @cfg {String/String[]/Ext.XTemplate} commentsTpl
         * The template used to format the Array of comments. It renders each message as an item in an unordered list.
         */
        commentsTpl: [
            '<tpl if="comments && comments.length &gt; 0">',
            '<ul class="{listCls} ametys-tooltip-comment"><tpl for="comments"><li>',
            // Author and date formatting
            '<div class="author-date">',
            Ext.String.format("<i18n:translate><i18n:text i18n:key='PLUGINS_CORE_UI_AMETYS_COMMENTS_TPL_AUTHOR_DATE'/><i18n:param>{0}</i18n:param><i18n:param>{1}</i18n:param></i18n:translate>", '{author}', '{date:date(Ext.Date.patterns.FriendlyDateTime )}'),
            '</div><div class="comment-text">{text:nl2br()}</div>',
            '</li></tpl></ul>',
            "<tpl else><i18n:text i18n:key='PLUGINS_CORE_UI_AMETYS_COMMENTS_TPL_EMPTY'/></tpl>"
        ],
        
    	/**
    	 * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * Get the fields comments. 
         * @return {Object[]} The comment array. See {@link #addComment}
         */
    	getComments: function()
    	{
    		return this.ametysComments || [];
    	},
    	
    	/**
    	 * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * Add a comment. 
         * @param {Object} comment The comment object to set
         * @param {String} comment.text The text of the comment
         * @param {String} comment.author The author of the comment
         * @param {Date} [comment.date] The date of the comment, if null, will be set to the current date.
         */
    	addComment: function(comment)
    	{
    		this.addComments([comment]);
    	},
    	
    	/**
    	 * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * Add comments 
         * @param {Object[]} comments The comment array to set. See {@link #addComment}
         */
    	addComments: function(comments)
    	{
    		comments = comments || [];
    		this.ametysComments = this.ametysComments || [];
    		
    		Ext.Array.forEach(comments, function(comment) {
    			comment.date = comment.date || new Date();
        		this.ametysComments.push(comment);
    		}, this);
    		
    		this.fireEvent('commentsupdated', this, this.ametysComments);
    		
    		this.renderComments();
    	},
    	
    	/**
    	 * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * Remove the field comments. 
         */
    	removeComments: function()
    	{
    		delete this.ametysComments;
    		this.renderComments();
    	},
    	
    	/**
    	 * @member Ext.form.Labelable
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * 
         * Updates the rendered DOM to match the current comments. This only updates the content and
         * attributes, you'll have to call doComponentLayout to actually update the display.
         */
        renderComments: function() 
        {
            var me = this,
                comments = me.getComments(),
                hasComment = comments.length > 0;

            if (me.showAmetysComments && me.rendered && !me.isDestroyed) 
            {
                if (this.commentWrapEl) 
                {
                	this.commentWrapEl[hasComment ? 'removeCls' : 'addCls']('ametys-comments-empty');
                	this.commentWrapEl.child('div', true).setAttribute("data-qtip", Ext.XTemplate.getTpl(this, 'commentsTpl').apply({
                		comments: comments,
                		listCls: Ext.plainListCls
                	}));
                }
            }
        },
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * @cfg {String} warningCls
         * The CSS class to use when marking the component has warning.
         */
        warningCls : Ext.baseCSSPrefix + 'form-warning',
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
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
        
	    /**
	     * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
         * 
	     * Gets an Array of any active warning messages currently applied to the field. 
	     * @return {String} The active warning message on the component; if there are no warning, null is returned.
	     */
    	getActiveWarning: function ()
    	{
    		return this.activeWarn;
    	},
    	
    	/**
    	 * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
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
         * @since Ametys Runtime 3.9
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
         * @since Ametys Runtime 3.9
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
         * @since Ametys Runtime 3.9
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
         * @since Ametys Runtime 3.9
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
    	 * @member Ext.form.Labelable
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * 
         * Updates the rendered DOM to match the current activeWarn. This only updates the content and
         * attributes, you'll have to call doComponentLayout to actually update the display.
         */
        renderActiveWarning: function() 
        {
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

                if (this.warningWrapEl) 
                {
                	this.warningWrapEl.dom.style.display = hasWarn ? '' : 'none';
                	this.warningWrapEl.dom.setAttribute("data-warnqtip", activeWarn);
                }
            }
        },
        
        /**
         * @member Ext.form.Labelable
         * @ametys
         * @since Ametys Runtime 3.9
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
                if (this.warningWrapEl) 
                {
                	this.warningWrapEl.dom.style.display = 'none';
                }
            }
        }
    };

    Ext.Array.insert(Ext.form.Labelable.prototype.labelableRenderTpl, Ext.form.Labelable.prototype.labelableRenderTpl.length - 1, ametysLabelable.afterOutterBodyEl);
    
    Ext.override(Ext.form.Labelable, Ext.apply(Ext.clone(ametysLabelable), { 
		statics: {
            initTip: function() 
            {
                this.callParent(arguments);
                
                var tip = this.warnTip,
                    cfg, copy;
    
                if (tip) {
                    return;
                }
    
                cfg = {
                    id: 'ext-form-warn-tip',
                    //<debug>
                    // tell the spec runner to ignore this element when checking if the dom is clean
                    sticky: true,
                    //</debug>
                    ui: 'form-warning'
                };
    
                // On Touch devices, tapping the target shows the qtip
                if (Ext.supports.Touch) {
                    cfg.dismissDelay = 0;
                    cfg.anchor = 'top';
                    cfg.showDelay = 0;
                    cfg.listeners = {
                        beforeshow: function() {
                            this.minWidth = Ext.fly(this.anchorTarget).getWidth();
                        }
                    };
                }
                tip = this.warnTip = Ext.create('Ext.tip.QuickTip', cfg);
                copy = Ext.apply({}, tip.tagConfig);
                copy.attribute = 'warnqtip';
                tip.setTagConfig(copy);                
            },
            
            destroyTip: function() 
            {
                this.callParent(arguments);

                this.tip = Ext.destroy(this.tip);
            }
		}
	}));
    Ext.override(Ext.form.field.Base, Ext.clone(ametysLabelable));
    Ext.override(Ext.form.FieldContainer, Ext.clone(ametysLabelable));
	 
    Ext.define("Ametys.form.field.Field", {
        override: "Ext.form.field.Field",
		        
		/**
		 * @member Ext.form.field.Field
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * 
		 * Associate one or more warning messages with this field.
		 * @param {String/String[]} warns The warning message(s) for the field.
		 */
		markWarning: Ext.emptyFn,
		
		/**
		 * @member Ext.form.field.Field
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * 
		 * Clear any warning styles/messages for this field.
		 */
		clearWarning: Ext.emptyFn,
		
		/**
         * @member Ext.form.field.Field
         * @ametys
         * @since Ametys Runtime 3.9
         * 
         * Get the readable value of a Field. The default implementation returns the same as #getValue.
         * Override this method to return an understandable value for more complex field (such as combobox, file input, ...). 
         * @template
         */
        getReadableValue: function ()
        {
        	return this.getValue();
        }
    });
    
    var ametysFieldBase = {
		/**
		 * @member Ext.form.field.Base
		 * @ametys
		 * @since Ametys Runtime 3.9
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
		 * @since Ametys Runtime 3.9
		 * 
		 * Clear any warning styles/messages for this field.
		 */
		clearWarning: function() 
		{
			this.unsetActiveWarnings();
			this.updateLayout();
		},
		
		/**
	     * @member Ext.form.field.Base
		 * @ametys
		 * @since Ametys Runtime 3.9
		 *  
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
    
    Ext.define("Ametys.form.field.Base", Ext.apply(Ext.clone(ametysFieldBase), { 
        override: 'Ext.form.field.Base',
        
        ignoreChangeRe: /data\-errorqtip|data\-warnqtip|style\.|className/
    }));
    
    Ext.define("Ametys.layout.component.field.FieldContainer", {
        override: 'Ext.layout.component.field.FieldContainer',
    
        publishInnerWidth: function (ownerContext, width) {
            var owner = this.owner;
            
            if (owner.descWrapEl)
            {
                width -= owner.descWrapEl.getWidth();
            }
            
            if (owner.commentWrapEl)
            {
                width -= owner.commentWrapEl.getWidth();
            }
            
            if (owner.warnWrapEl && owner.hasActiveWarning()) 
            {
                width -= owner.warnWrapEl.getWidth();
            }
            
            this.callParent([ownerContext, width]);
        }
     });
    
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
        	 * @since Ametys Runtime 3.9
        	 * @cfg {Boolean} ametysShowMultipleHint=false true to show to multiple hint under the field. false by default
        	 */
        	if (this.ametysShowMultipleHint == true)
        	{
        		result += '<span class="ametys-field-hint">(' + "<i18n:text i18n:key='PLUGINS_CORE_UI_MULTIPLE_HINT'/>" + ')</span>'
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
        
        afterRender: function()
        {
            this.callParent(arguments);
            
            /**
             * @member Ext.form.field.File
             * @ametys
             * @since Ametys Runtime 3.9
             * @cfg {Boolean} ametysShowMaxUploadSizeHint false to hide to max size hint under the field. true by default
             */
            if (Ametys.MAX_UPLOAD_SIZE != undefined && Ametys.MAX_UPLOAD_SIZE != '' && this.ametysShowMaxUploadSizeHint !== false)
            {
                this.triggerWrap.dom.setAttribute('data-maxsizemsg', "(<i18n:text i18n:key='PLUGINS_CORE_UI_UPLOAD_HINT'/>" + Ext.util.Format.fileSize(Ametys.MAX_UPLOAD_SIZE) + ")");
            }
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
 * Support for readable value
 */
(function() 
{
    Ext.define("Ametys.form.field.ComboBox", {
        override: "Ext.form.field.ComboBox",
        
        getReadableValue: function ()
    	{
    		return this.getDisplayValue();
    	}
    });
})();

/*
 * Support for comments
 */
(function ()
		{
			Ext.define("Ametys.form.Basic", {
		        override: "Ext.form.Basic",
		        
		        /**
		         * @member Ext.form.Basic
		         * @method getComments 
		         * @since Ametys Runtime 3.9
		         * @ametys
		         * Retrieves the commenst of each field in the form as a set of key/comments pairs
		         * @param {Boolean} [asString=false] If true, will return the key/comment collection as a single URL-encoded param string.
		         * @param {Boolean} [excludeEmpty=false] If true, the field with no comments won't be included, otherwise empty field with have an empty array as value.
		         * @return {String/Object} The comments
		         */
		    	getComments: function (asString, excludeEmpty)
		    	{
		    		var comments  = {};
		           	var	fields  = this.getFields().items;

			        for (var i = 0; i < fields.length; i++) 
			        {
			            var field = fields[i];
			            
			            if (Ext.isFunction(field.getComments))
			            {
			            	var fieldComments = field.getComments();
			            	
			            	if (!(excludeEmpty && fieldComments.length == 0))
		            		{
			            		// Format date to string for each comment 
			            		Ext.Array.forEach(fieldComments, function(comment) {
			            			comment.date =  Ext.util.Format.date(comment.date, Ext.Date.patterns.ISO8601DateTime);
			            		});
			            		
			            		comments[field.getName()] = fieldComments;
		            		}
			            }
			        }

			        if (asString)
			        {
			        	comments = Ext.Object.toQueryString(comments);
			        }
			        
			        return comments;
				}
		    });
})();

(function ()
		{
			Ext.define("Ametys.form.Panel", {
		        override: "Ext.form.Panel",
		        
		        /**
		         * @member Ext.form.Panel
		         * @method getComments 
		         * @since Ametys Runtime 3.9
		         * @ametys
		         * Convenience function for fetching the current comment of each field in the form. This is the same as calling
		         * {@link Ext.form.Basic#getComments} this.getForm().getComments().
		         *
		         * @inheritdoc Ext.form.Basic#getComments
		         */
		    	getComments: function (asString, excludeEmpty)
		    	{
		    		return this.getForm().getComments(asString, excludeEmpty);
				}
		    });
})();

/*
 * Support for optional label on text field to indicate field is multiple
 */
(function() 
{
    Ext.define("Ametys.form.field.Number", {
        override: "Ext.form.field.Number",
        
        /**
         * @override Ext.form.field.Number
         * @member submitLocaleSeparator
         * @cfg submitLocaleSeparator=false 
         * @since Ametys Runtime 3.9
         * @ametys
         *
         * @inheritdoc Ext.form.field.Number#cfg-submitLocaleSeparator
         */
        submitLocaleSeparator: false,
        
        /**
         * @override Ext.form.field.Number
         * @member submitLocaleSeparator
         * @cfg baseChars='0123456789.' 
         * @since Ametys Runtime 3.9
         * @ametys
         *
         * @inheritdoc Ext.form.field.Number#cfg-baseChars
         */
        baseChars: '0123456789.'
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
     * @since Ametys Runtime 3.9
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
	        	var anim = Ext.fx.Manager.getActiveAnimation(element.id);
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
	        	    value[1].replace(/[-.0-9]*/, '')
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
    	 * @since Ametys Runtime 3.9
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
			
			function getLastNonEmptyTextNode(html)
			{
				var lastChild = html.lastChild;
                if (lastChild == null)
                {
                    return null;
                }

				while (lastChild.lastChild != null)
				{
					lastChild = lastChild.lastChild;
				}
				
				if (lastChild.nodeType == 3) // node text
				{
					var text = lastChild.nodeValue;
					if (Ext.isEmpty(text) || text == ellipsis)
					{
						// Remove empty text
						lastChild.parentNode.removeChild(lastChild);
						return getLastNonEmptyTextNode(html);
					}
					else
					{
						if (text.indexOf(ellipsis, text.length - 3) == -1)
						{
							lastChild.nodeValue = text + ellipsis;
						}
						return lastChild;
					}
				}
				else
				{
					// Remove empty tag
					lastChild.parentNode.removeChild(lastChild);
					return getLastNonEmptyTextNode(html);
				}
			}
			
			var html = document.createElement('div');
			html.innerHTML = text;
			
			if (this.getWidth(html.innerHTML) > maxWidth && maxWidth > 0)
			{
				var textNode = getLastNonEmptyTextNode(html);
				
				while (this.getWidth (html.innerHTML) > maxWidth)
				{
					textNode.nodeValue = textNode.nodeValue.substring (0, textNode.nodeValue.length - ellipsis.length - 1) + ellipsis;
					textNode = getLastNonEmptyTextNode(html);
				}
				
				return html.innerHTML;
			}
			else
			{
				return text;
			}
		},
		
		/**
		 * @member Ext.util.TextMetrics
		 * @ametys
		 * @since Ametys Runtime 3.9
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
		         * @since Ametys Runtime 3.9
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
					 * @since Ametys Runtime 3.9 
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
				 * @since Ametys Runtime 3.9 
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
					if (this.self._logger == null)
					{
						this.self._logger = Ametys.log.LoggerFactory.getLoggerFor(this.self.getName()) 
					}
					return this.self._logger;
				},
                
                /**
                 * @member Ext.Base
                 * @method addCallables
                 * @ametys
                 * @since Ametys Runtime 4.0 
                 * Add methods to this object that will call a server method using Ametys.data.ServerComm#callMethod.
                 * 
                 * The generated method should be documented using the following template
                 * 
                 * 
                 *          @ callable
                 *          @ member My.Object
                 *          @ method MethodName 
                 *          This calls the method 'MMM' of the server DAO 'XXX'.
                 *          @ param {Object[]} parameters The parameters to transmit to the server method
                 *          @ param {} parameters.myparam
                 *          ...
                 *          @ param {Function} callback The function to call when the java process is over. 
                 *          @ param {Object} callback.returnedValue The value return from the server. Null on error (please note that when an error occured, the callback may not be called depending on the value of errorMessage).
                 *          @ param {Object} callback.arguments Other arguments specified in option.arguments                 
                 *          @ param {Object} [options] Advanced options for the call.
                 *          @ param {Boolean/String/Object} [options.errorMessage=true] Display an error message. See Ametys.data.ServerCall#callMethod errorMessage.
                 *          @ param {Boolean/String/Object} [options.waitMessage] Display a waiting message. See Ametys.data.ServerCall#callMethod waitMessage.
                 *          @ param {Number} [options.priority] The message priority. See Ametys.data.ServerCall#callMethod for more information on the priority. PRIORITY_SYNCHRONOUS cannot be used here.
                 *          @ param {String} [options.cancelCode] Cancel similar unachieved read operations. See Ametys.data.ServerCall.callMethod cancelCode.
                 *          @ param {Object} [options.arguments] Additional arguments set in the callback.arguments parameter.                  
                 * 
                 * @param {Object/Object[]} configs The default values for Ametys.data.ServerComm#callMethod config argument.
                 * @param {String} [configs.localName=configs.methodName] This additionnal optionnal argument stands for the local method name.
                 */
                addCallables: function(configs)
                {
                    configs = Ext.Array.from(configs);
                    
                    Ext.Array.each(configs, function(config) {
                        this[config.localName || config.methodName] = function(parameters, callback, options) {
                            parameters = parameters || [];
                            options = options || {};
                            
                            if (options.errorMessage == null)
                            {
                                options.errorMessage = true;
                            }
                            
                            var methodConfig = {
                                parameters: parameters,
                                callback: {
                                    handler: callback,
                                    scope: this,
                                    arguments: options.arguments
                                },
                                waitMessage: options.waitMessage,
                                errorMessage: options.errorMessage,
                                cancelCode: options.cancelCode,
                                priority: options.priority                                
                            }
                            var finalConfig = Ext.applyIf(methodConfig, config);
                            Ametys.data.ServerComm.callMethod(finalConfig);
                        }
                    }, this);
                }                
			});
})();

(function() {
	Ext.define("Ametys.selection.RowModel", {
		override: "Ext.selection.RowModel",
		
		/**
		 * @member Ext.grid.plugin.CellEditing
		 * @ametys
		 * @since Ametys Runtime 3.9
	     * @cfg {Boolean} moveEditorOnEnter
	     * <tt>false</tt> to turn off moving the editor to the next row down when the enter key is pressed
	     * or the next row up when shift + enter keys are pressed.
	     */
		
		/**
		 * @private
		 * As onEditorTab but for ENTER key
		 */
		onEditorEnter: function(editingPlugin, e)
		{
	        if (this.moveEditorOnEnter == true) 
	        {
	            var me = this,
	            view = editingPlugin.context.view,
	            record = editingPlugin.getActiveRecord(),
	            header = editingPlugin.getActiveColumn(),
	            position = view.getPosition(record, header),
	            direction = e.shiftKey ? 'up' : 'down',
	            lastPos;

		        // We want to continue looping while:
		        // 1) We have a valid position
		        // 2) There is no editor at that position
		        // 3) There is an editor, but editing has been cancelled (veto event)
	
	            // Changing row require a timeout to avoid the modification of the new record instead of the old one ; and completing the editing in stead of opening it
	            window.setTimeout(function() {
	            	do {
	            		lastPos = position;
	            		position  = view.walkCells(position, direction, e, me.preventWrap);
	            		if (position === false || lastPos && lastPos.isEqual(position)) {
	            			// If we end up with the same result twice, it means that we weren't able to progress
	            			// via walkCells, for example if the remaining records are non-record rows, so gracefully
	            			// fall out here.
	            			return;
	            		}
	            		editingPlugin.context.grid.getSelectionModel().deselect(lastPos.row);
	            	} while (position && (!position.column.getEditor(record) || !editingPlugin.startEditByPosition(position)));
	            }, 1);
	        }
		},
		
	    onEditorTab: function(editingPlugin, e) {
	        var me = this,
	            view = editingPlugin.context.view,
	            record = editingPlugin.getActiveRecord(),
	            header = editingPlugin.getActiveColumn(),
	            position = view.getPosition(record, header),
	            direction = e.shiftKey ? 'left' : 'right',
	            lastPos;

	        // We want to continue looping while:
	        // 1) We have a valid position
	        // 2) There is no editor at that position
	        // 3) There is an editor, but editing has been cancelled (veto event)

	        do {
	            lastPos = position;
	            position  = view.walkCells(position, direction, e, me.preventWrap);
	            if (lastPos && lastPos.isEqual(position)) {
	                // If we end up with the same result twice, it means that we weren't able to progress
	                // via walkCells, for example if the remaining records are non-record rows, so gracefully
	                // fall out here.
	                return;
	            }
        		editingPlugin.context.grid.getSelectionModel().deselect(lastPos.row); 										// FIX we have to deselect last row CMS-5979
	        } while (position && (!position.column.getEditor(record) || !editingPlugin.startEditByPosition(position)));
	    }		
	});
	
	Ext.define("Ametys.grid.plugin.CellEditing", {
		override: "Ext.grid.plugin.CellEditing",
		
		/**
		 * @member Ext.grid.plugin.CellEditing
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * @cfg {Boolean} editAfterSelect=false When #cfg-triggerEvent is not specified or is 'cellclick' and #cfg-clicksToEdit is 1, this configuration allow to enter in edition only if the record was focused first. As a rename under files manager, you will have to first click on the row to select it and click again to edit it (but not doubleclick).
		 */

		/**
		 * @member Ext.grid.plugin.CellEditing
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * @cfg {Boolean} silentlyIgnoreInvalid=true When leaving edition of a field with keys (ENTER or TAB), the edition will be canceled if true, and the edition will not be quit when false. Bluring the field will continue cancelling the edition if the field is invalid.
		 */

		/**
		 * @member Ext.grid.plugin.CellEditing
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * @private
		 * @property {String} armed Used when #cfg-editAfterSelect is true. The record id that was "armed"... so that was selected in a preceding operation: this is to distinguish a click to select and a second click to edit.
		 */
		
		initEditTriggers: function()
		{
			this.callParent(arguments);
			
			 if (this.editAfterSelect && (this.triggerEvent == null || this.triggerEvent == 'cellclick') && this.clicksToEdit === 1)
			 {
				 this.mon(this.view, 'celldblclick', this.onCellDblClick, this);
			 }
		},
		
		onCellClick: function(view, cell, colIdx, record, row, rowIdx, e)
		{
			// In some specific case, such as trigger widgets, changing the selection does not close the current edition
			this.getActiveEditor() && this.getActiveEditor().completeEdit();
			
			if (!this.editAfterSelect || (this.triggerEvent != null && this.triggerEvent != 'cellclick') || this.clicksToEdit !== 1)
			{
				this.callParent(arguments);
			}
			else if (this.armed == record.getId() && this.oncellclicktimeout == null)
			{
				this.oncellclicktimeout = Ext.defer(this.onCellClickAndNotDoubleClick, 300, this, arguments);
			}
			this.armed = record.getId();
		},
		
		/**
		 * @member Ext.grid.plugin.CellEditing
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * @private
		 * This method is call asynchronously by #onCellClick, when a single click was done and not a double click
		 */
		onCellClickAndNotDoubleClick: function()
		{
			this.oncellclicktimeout = null;
			Ext.defer(this.superclass.onCellClick, 0, this, arguments);
		},
		
		/**
		 * @member Ext.grid.plugin.CellEditing
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * @private
		 * Listener on cell double click, only when cell editing is set to a single click on the cell AND #cfg-editAfterSelect is true.
		 * This listener is here to cancel a starting editing when finally this is not a simple click
		 */
		onCellDblClick: function()
		{
			if (this.oncellclicktimeout != null)
			{
				window.clearTimeout(this.oncellclicktimeout);
				this.oncellclicktimeout = null;
			}
		},
		
	    onSpecialKey: function(ed, field, e) {
	        var sm = ed.up('tablepanel').getSelectionModel();
	        if (sm.moveEditorOnEnter && e.getKey() === e.ENTER) 
	        {
	            e.stopEvent();

	            var column = this.getActiveColumn();
	            var record = this.getActiveRecord();
	            
	            if (ed) {
	                // Allow the field to act on tabs before onEditorTab, which ends
	                // up calling completeEdit. This is useful for picker type fields.
	                ed.onEditorTab(e);
	            }

	            if (sm.onEditorEnter) {
	                sm.onEditorEnter(ed.editingPlugin, e);
	            }
	        }
	        else
	        {
	        	this.callParent(arguments);
	        }
	    }
	});
	
	Ext.define("Ametys.grid.CellEditor", {
		override: "Ext.grid.CellEditor",
		
		startEdit: function()
		{
			// TODO this.editingPlugin.armed = this.editingPlugin.getActiveRecord().getId();
		
			this.callParent(arguments);
		},
		
		onSpecialKey : function(field, event) {
			if (this.editingPlugin.silentlyIgnoreInvalid == false && !field.isValid() && (event.getKey() == event.ENTER || event.getKey() == event.TAB))
			{
				event.stopEvent();
				return;
			}
			
			this.callParent(arguments);
		},
		
		alignment: "tl-tl"
	});
})();

(function()
{
	Ext.define("Ametys.JSON", {
		override: 'Ext.JSON',
		
		/**
		 * @member Ext.JSON
		 * @ametys
		 * @since Ametys Runtime 3.9
		 * Converts an object to a readable json string as a HTML fragment.
		 * @param {Object} value The value to encode.
		 * @param {Number} [offset=0] The offset to indent the text
		 * @param {Function} [renderer] A renderer function to pass when a custom output is wanted. Can call Ext.JSON#prettyEncode for recursion.
         * @param {Object} renderer.value The value to encode
         * @param {Number} renderer.offset The offset to indent the text (to use only when generating new lines).
         * @param {String/Boolean} renderer.return The rendered value or `false` to get the standard rendering for this value.
         * @return {String} The rendered value, as a HTML fragment.
		 */
		prettyEncode: function (value, offset, renderer)
		{
            function openArrayOrObject(separator, value)
            {
                function closedText(values) 
                {
                    return "... " + values + " value" + (values != 1 ? "s" : "") + " ...";
                }
                return '<div class="json-array' + (offset <= 1 ? '' : ' json-closed') + '">' 
                        + '<span class="json-char" onclick="Ext.get(this.parentNode).toggleCls(\'json-closed\')" oncontextmenu="Ext.get(this.parentNode).removeCls(\'json-closed\'); Ext.get(this.parentNode).select(\'div.json-closed\').removeCls(\'json-closed\'); return false;">' 
                                + separator 
                        + '</span>' 
                        + '<span class="json-closed" style="display: none">' + closedText(Ext.isArray(value) ? value.length : Ext.Object.getSize(value)) + '</span>' 
                        + '<span>';
            }
            function closeArrayOrObject(separator)
            {
                return "</span>" + separator + "</div>";
            }
            
			offset = offset || 0;
            
			var s = "";
			var result;
			if (Ext.isFunction(renderer) && (result = renderer(value, offset)))
		    {
			    return result;
		    }
			else if (value != null && value.$className)
			{
				return this.prettyEncode("Object " + value.$className + (typeof(value.getId) == 'function' ? ('@' + value.getId()) : ''), renderer);
			}
			else if (typeof(value) == "function")
			{
				return "null";
			}
			else if (Ext.isArray(value))
			{
                if (value.length == 0)
                {
                    s += '<div class="json-array">[ ]</div>';
                }
                else
                {
    				s += openArrayOrObject("[", value);
    				
    				var hasOne = false;
    				for (var id = 0; id < value.length; id++)
    				{
    					if (hasOne)
    					{
    						s += ",";
    					}
    					hasOne = true;
    					s += "<br/>"
    					for (var i = 0; i < offset + 1; i++)
    					{
    						s += "&#160;&#160;&#160;&#160;";
    					}
    					s += this.prettyEncode(value[id], offset + 1, renderer);
    				}
    				
    				if (hasOne)
    				{
    					s += "<br/>";
    					for (var i = 0; i < offset; i++)
    					{
    						s += "&#160;&#160;&#160;&#160;";
    					}
    				}
    				else
    				{
    					s += " ";
    				}
    				s += closeArrayOrObject("]");
                }
			}
			else if (Ext.isObject(value))
			{
                if (Ext.Object.isEmpty(value))
                {
                    s += '<div class="json-object">{ }</div>';
                }
                else
                {
                    s += openArrayOrObject("{", value);
    				
    				var hasOne = false;
    				for (var id in value)
    				{
    					if (hasOne)
    					{
    						s += ",";
    					}
    					hasOne = true;
    					s += "<br/>"
    					for (var i = 0; i < offset + 1; i++)
    					{
    						s += "&#160;&#160;&#160;&#160;";
    					}
    					s += "<strong>" + Ext.JSON.encodeValue(id) + "</strong>: ";
    					s += this.prettyEncode(value[id], offset + 2, renderer);
    				}
    
    				if (hasOne)
    				{
    					s += "<br/>";
    					for (var i = 0; i < offset; i++)
    					{
    						s += "&#160;&#160;&#160;&#160;";
    					}
    				}
    				else
    				{
    					s += " ";
    				}
    				s += closeArrayOrObject("}");
                }
			}
			else
			{
				s += Ext.JSON.encodeValue(value);
			}
			
			return s;
		}
	});
})();

(function()
{
	Ext.define("Ametys.ux.IFrame", {
		override: 'Ext.ux.IFrame',
		
		loadMask: "<i18n:text i18n:key='PLUGINS_CORE_UI_IFRAME_LOADING'/>"
	});
})();

(function()
{
    Ext.define("Ametys.form.field.Tag", {
        override: 'Ext.form.field.Tag',
        
        /** 
         * @member Ext.form.field.Tag
         * @since Ametys Runtime 3.9
         * @ametys
         * @cfg {Boolean} labelHTML=false If true the labelTpl will not be encoded 
         * */
        labelHTML: false,
        
        getMultiSelectItemMarkup: function()
        {
            var value = this.callParent(arguments);
            
            if (this.labelHTML)
            {
                var me = this;
                this.multiSelectItemTpl.getItemLabel = function(values) { return me.labelTpl.apply(values); };
                return this.multiSelectItemTpl.apply(this.valueCollection.getRange());
            }
            else
            {
                return value;
            }
        }
    });    
})();
