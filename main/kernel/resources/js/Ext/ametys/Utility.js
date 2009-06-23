// Add the additional 'advanced' VTypes
Ext.apply(Ext.form.VTypes, {
    daterange : function(val, field) {
        var date = field.parseDate(val);

        if(!date){
            return;
        }
        if (field.startDateField && (!this.dateRangeMax || (date.getTime() != this.dateRangeMax.getTime()))) {
            var start = Ext.getCmp(field.startDateField);
            start.setMaxValue(date);
            start.validate();
            this.dateRangeMax = date;
        } 
        else if (field.endDateField && (!this.dateRangeMin || (date.getTime() != this.dateRangeMin.getTime()))) {
            var end = Ext.getCmp(field.endDateField);
            end.setMinValue(date);
            end.validate();
            this.dateRangeMin = date;
        }
        /*
		 * Always return true since we're only using this vtype to set the
		 * min/max allowed values (these are tested for after the vtype test)
		 */
        return true;
    },

    password : function(val, field) {
        if (field.initialPassField) {
            var pwd = Ext.getCmp(field.initialPassField);
            return (val == pwd.getValue());
        }
        return true;
    },

    passwordText : 'Passwords do not match'
});

function Utils()
{
}

Utils.buildParams = function (form, submitId) 
{
	var result = {};
	// Indicate to the server that we're in ajax mode
	result['cocoon-ajax'] = 'true';
	
    // Iterate on all form controls
    for (var i = 0; i < form.elements.length; i++) 
    {
        input = form.elements[i];
        
        if (input.name == null || input.name == "" || input.disabled) {
            continue;
        }
        
        if (typeof(input.type) == "undefined") {
            // Skip fieldset
            continue;
        }
        if (input.type == "submit" || input.type == "image") {
            // Skip buttons
            continue;
        }
        if ((input.type == "checkbox" || input.type == "radio") && !input.checked) {
            // Skip unchecked checkboxes and radio buttons
            continue;
        }
        if (input.type == "file") {
            // Can't send files in Ajax mode. Fall back to full page
            return null;
        }
        if (input.tagName.toLowerCase() == "select" && input.multiple) {
            var name = input.name;
            var options = input.options;
            for (var zz = 0; zz < options.length; zz++) {
                if (options[zz].selected) {
                	result['name'] = options[zz].value;
                }
            }
            // don't use the default fallback
            continue;
        }

        // text, passwod, textarea, hidden, single select
        result[input.name] = input.value
    }
    return result;
} 