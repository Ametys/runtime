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
 * Extensions for search tools
 * @private
 */
Ext.define('Ametys.plugins.cms.search.solr.EditSolrQueryExtension', {
	singleton: true,
	
	/**
	 * Add the search extensions for queries
	 */
	addSearchExtension: function ()
	{
		// Valid declarations
		1) "{{i18n test1}}"
		2) "{{i18n TEST_KEY}}"
		3) "{{i18n plugin.test:TEST_KEY}}"
		4) "\\{{{i18n TEST_KEY}}"

		// Invalid declarations
		5) "{{i18n test4 } }"
		6) "{{i18n {{ }}"
		7) "{{i18n }}"
		8) "{{i18n2 TEST_KEY}}"
		9) "{{i18n TEST_KEY}"
		10) "\{{i18n TEST_KEY}}"
		
		Ametys.plugins.cms.search.SearchToolExtensions.registerAdditionalButton({
			icon: Ametys.getPluginResourcesPrefix('cms') + '/img/search/solr_16.png',
			// text: "{{i18n PLUGINS_CMS_EDIT_SOLR_QUERY_SHORT}}",
			handler: this._openSolrTool,
			scope: this,
			tooltip: {
				title: "{{i18n PLUGINS_CMS_EDIT_SOLR_QUERY}}",
				text: "{{i18n PLUGINS_CMS_EDIT_SOLR_QUERY_TOOLTIP}}",
				image: Ametys.getPluginResourcesPrefix('cms') + '/img/search/solr_48.png',
				inribbon: false
			}
		}, 'l');
	},
	
	/**
	 * @private
	 * This action opens the Solr tool search
	 * @param {Ext.button.Button} button The button calling this function
	 */
	_openSolrTool: function(button)
	{
	    var toolId = button.toolId || button.parentMenu.ownerButton.toolId;
	    var tool = Ametys.tool.ToolsManager.getTool(toolId);
	    
	    if (tool.getCurrentSearchParameters)
		{
	    	var params = tool.getCurrentSearchParameters();
	    	params.contextualParameters = tool.getSearchContextualParameters();
	    	
		    // Get the solr query.
	        Ametys.data.ServerComm.send({
	            plugin: 'cms',
	            url: 'solr-query.json',
	            parameters: params,
	            priority: Ametys.data.ServerComm.PRIORITY_MAJOR,
	            waitMessage: true,
	            errorMessage: {
	                msg: "{{i18n PLUGINS_CMS_UITOOL_SOLR_SEARCH_ERROR_GET_QUERY}}",
	                category: this.self.getName()
	            },
	            responseType: 'text',
	            callback: {
	                handler: this._openSolrToolCb,
	                scope: this,
	                arguments: {
	                    tool: tool
	                }
	            }
	        });
		}
	},
	
	/**
	 * @private
     * Open the Solr query tool.
     * @param {Object} response the server's response
     * @param {Object} response.contentTypes the content types
     * @param {Object} response.columns the columns
     * @param {Object} response.facets the facets
     * @param {Object} params the callback arguments
     * @param {Ametys.tool.Tool} params.tool the search tool
     */
	_openSolrToolCb: function(response, params)
	{
	    var result = Ext.JSON.decode(Ext.dom.Query.selectValue('', response));
	    
	    var toolValues = {
            query: result.query
	    };
	    
	    if (Ext.isArray(result.contentTypes) && result.contentTypes.length > 0)
	    {
	        toolValues.contentTypes = result.contentTypes;
	    }
	    if (Ext.isArray(result.columns) && result.columns.length > 0)
        {
	        toolValues.columns = result.columns.join(', ').replace(/\//g, '.');
        }
        if (Ext.isArray(result.facets) && result.facets.length > 0)
	    {
	        toolValues.facets = result.facets;
	    }
	    
	    Ametys.tool.ToolsManager.openTool('uitool-solrsearch', {
	        id: 'search-ui.solr',
	        expanded: true,
	        values: toolValues
	    });
	}
	
});

Ametys.plugins.cms.search.solr.EditSolrQueryExtension.addSearchExtension();

// unfinished declaration
{{i18n UNFINISHED