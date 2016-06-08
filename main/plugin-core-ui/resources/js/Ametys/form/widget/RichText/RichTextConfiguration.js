/*
 *  Copyright 2015 Anyware Services
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
 * Singleton to manage the 'edition.richtext' widget for the FormEditionPanel.
 * Allow to add CSS files, supported tags and common listeners.
 */
Ext.define('Ametys.form.widget.RichText.RichTextConfiguration', {
	extend: 'Ext.util.Observable',
	
	singleton: true,
	
	/**
	 * @property {String[]} _useCSSFile List of css files to load in the inline editor
	 * @private
	 */
	_useCSSFile: [],
	/**
	 * @property {Object} _tags Handled tags
	 * @private
	 */
	_tags: {},
	/**
	 * @property {Function[]} _validators List of registered validator #addValidator used in #validates
	 * @private
	 */
	_validators: [],
	
	/**
     * @event setcontent
     * Fires when the editor received new content. This allows to convert storing tags to internal tags. Use object.content to get/set the full html. See Ametys.form.field.RichText#event-setcontent for parameters.
     */
    /**
     * @event getcontent
     * Fires when the editor received content. This allows to convert internal tags to storing tags. Use object.content to get/set the full html. See Ametys.form.field.RichText#event-getcontent for parameters.
     */
    /**
     * @event keypress
     * Fires when the editor has a key press. See Ametys.form.field.RichText#event-keypress for parameters.
     */
    /**
     * @event keydown
     * Fires when the editor has a key down. See Ametys.form.field.RichText#event-keydown for parameters.
     */
    /**
     * @event keyup
     * Fires when the editor has a key up. See Ametys.form.field.RichText#event-keydown for parameters.
     */
    /**
     * @event visualaid
     * Fires when the editor pre process the serialization. See Ametys.form.field.RichText#event-visualaid for parameters.
     */
    /**
     * @event preprocess
     * Fires when the editor pre process the serialization. See Ametys.form.field.RichText#event-preprocess for parameters.
     */
    /**
     * @event htmlnodeselected
     * Fires when a HTML node is selected in editor. See Ametys.form.field.RichText#event-htmlnodeselected for parameters.
     */
	
    /**
     * Add a custom validation function to be called during inline editor validation ({@link Ext.form.field.Field#getErrors}).
     * This function will be passed the following parameters:
     *
     * @param {Function} validator The new validator to add. This function will have the following signature:
     * @param {Object} validator.value The current field value
     * @param {Boolean/String} validator.return
     * - True if the value is valid
     * - An error message if the value is invalid
     */
	addValidator: function(validator)
	{
		this._validators.push(validator);
	},
	
    /**
     * Validates the value among the existing validators for the inline editor
     * @param {String} value The current field value
     * @return {Boolean/String} validator.return
     *
     * - True if the value is valid
     * - An error message if the value is invalid
     */
	validates: function(value)
	{
		var returnValues = "";
		
		Ext.each(this._validators, function (validator) {
			var returnValue = validator.apply(null, [value]);
			if (returnValue !== true)
			{
				returnValues += returnValue + "\n";
			}
		});
		
		return returnValues.length == 0 ? true : returnValues.substring(0, returnValues.length - 1);
	},
	
	/**
	 * Add a CSS file to load in the inline editor
	 * @param {String} file The path of CSS file 
	 */
	addCSSFile: function (file)
	{
		this._useCSSFile.push(file);
	},
	
	/**
	 * Get all added css files as one string
	 * @return {String} The comma-separated list of added files
	 */
	getCSSFiles: function()
	{
		return this._useCSSFile.join(",")
	},
	
	/**
	 * Get all supported tags as a tinymce string. See valid_element tinymce configuration doc for the exact format.
	 * @return {String} The supported tags.
	 */
	getTags: function()
	{
		var validElements = "";
		for (var key in this._tags)
		{
			if (validElements != "")
			{
				validElements += ",";
			}
			validElements += this._tags[key].toString();
		}
		return validElements;
	},
    
    /**
     * Get all supported styles as a tinymce conf object. See valid_styles tinymce configuration doc for the exact format.
     * @return {Object} The supported properties for the style attribute.
     */
    getStyles: function()
    {
        var validStyles = {};
        
        for (var key in this._tags)
        {
            if (this._tags[key].attributes["style"])
            {
                validStyles[key] = this._tags[key].attributes["style"].values.join(",");
            }
        }        
        
        return validStyles;
    },
    
    /**
     * Get all supported classes as a tinymce conf object. See valid_classes tinymce configuration doc for the exact format.
     * @return {Object} The supported properties for the style attribute.
     */
    getClasses: function()
    {
        var validClasses = {};
        
        for (var key in this._tags)
        {
            validClasses[key] = {};
            
            if (this._tags[key].attributes["class"])
            {
                validClasses[key] = this._tags[key].attributes["class"].values.join(" ");
            }
        }        
        
        return validClasses;
    },    
	
	/**
	 * This method retrieve a tag to handle.
	 * Just by calling this method, the tag will be handled by the editor.
	 * Once you have a tag it a map
	 * - emptyTag Can be empty, +, - or #. see tinymce valid_elements documentation
	 * - getAttributes Call this method to handle an attribute. This will return a map where key is the attribute name and value are
	 *          - values An array of possible values
	 *          - defaultValue The default value. Can be null if there is no
	 * - replaceTags An array of tags that will be replaced by this one. This value can be dynamically removed if some one register this other tag as handled.
	 * 
	 * e.g. var aTag = Ametys.form.widget.RichText.RichTextConfiguration.handleTag("a"); // from now &lt;a&gt; will be accepted
	 *      aTag.emptyTag = "+"; // The &lt;a&gt; tag will be forced opened
	 *      var classAttr = aTag.handleAttribute("class"); // The class attribute is now accepted on the &lt;a&gt; tag
	 *      classAttr.defaultValue = "myclass"; // The class attribute will now always exists on &lt;a&gt; tag with the value "myclass"
	 *      classAttr.handleValue("myclass");
	 *      classAttr.handleValue("myclass2"); // The class attribute will now accept 2 values myclass or myclass2
     *      classAttr.handleValue(["myclass3", "myclass"]); // The class attribute will now accept 3 values myclass or myclass2 or myclass3
     *      
     * Please note, that the "style" and "classes" attributes are holded separately: 
     * For "style", each value is a valid property. E.g. the following line wille handle the "p" tag, with a style attribute, which contains the 'text-align' property.
     *      Ametys.form.widget.RichText.RichTextConfiguration.handleTag("p").handleAttribute("style").handleValue("text-align");
     *      and not a style attribute that can be equals to "text-align".
     * For "class", each value is a valid class.
     *      Ametys.form.widget.RichText.RichTextConfiguration.handleTag("p").handleAttribute("class").handleValue("a");
     *      means that the class attribute can contains "a", but do not need to be equals to "a". 
	 */
	handleTag: function(tagName)
	{
		if (this._tags[tagName] == null)
		{
			var me = this;
			this._tags[tagName] = {
					tagName: tagName,
					emptyTag: "",
					attributes: {},
					handleAttribute: function(attributeName)
					{
						if (this.attributes[attributeName] == null)
						{
							this.attributes[attributeName] = {
								attributeName: attributeName,
								values: [],
								defaultValue: null,
                                handleValue: function(value) {
                                    value = Ext.Array.from(value);
                                    this.values = Ext.Array.merge(this.values, value)
                                    return this;
                                },
								toString: function() {
									var a = this.defaultValue != null ? this.attributeName + "=" + this.defaultValue : "";

									var b = ""
                                    if (attributeName != "style" && attributeName != "class")
                                    {
    									for (var key in this.values)
    									{
    										if (typeof this.values[key] == "string")
    										{
    											if (b != "") 
    											{
    												b += "?";
    											}
    																			
    											b += this.values[key];
    										}
    									}
    									if (b != "")
    									{
    										b = this.attributeName + "<" + b;
    									}
                                    }
								
									if (a == "" && b == "")
									{
										return this.attributeName;
									}
									else 
									{
										return a + ((a != "" && b != "") ? "|" : "") + b;
									}
								}
							};
						}
						return this.attributes[attributeName];
					},
					replaceTags: [],
					toString: function() {
						var base = this.emptyTag + tagName;
						
						var attributes = "";
						for (var key in this.attributes)
						{
							if (attributes != "")
							{
								attributes += "|"; 
							}
							attributes += this.attributes[key].toString(); 
						}
						
						var attrs = (attributes == "" ? "" : "[" + attributes + "]");
						
						if (this.replaceTags.length == 0)
						{
							return base + attrs;
						}
						else
						{
							var finals = "";
							for (var i = 0; i < this.replaceTags.length; i++)
							{
								// The second part of the condition allow to remove multiple instance of the same string
								if (me._tags[this.replaceTags[i]] == null && this.replaceTags.indexOf(this.replaceTags[i]) == i)
								{
									finals += base + "/" + this.replaceTags[i] + attrs + ",";
								}
							}
							return finals.substring(0, finals.length - 1); 
						}
						
					}
			};
		}
		return this._tags[tagName];
	}
});

(function ()
{
    // Register basic tags
	Ametys.form.widget.RichText.RichTextConfiguration.handleTag("p").emptyTag = "#";
    Ametys.form.widget.RichText.RichTextConfiguration.handleTag("br");
    // Register SPAN for bookmark purposes
    Ametys.form.widget.RichText.RichTextConfiguration.handleTag("span").emptyTag = "!";
    Ametys.form.widget.RichText.RichTextConfiguration.handleTag("span").handleAttribute("id");
	
	Ametys.form.widget.RichText.RichTextConfiguration.addCSSFile(Ametys.getPluginResourcesPrefix('core-ui') + "/css/Ametys/form/RichTextConfiguration-Inline.css");
})();
