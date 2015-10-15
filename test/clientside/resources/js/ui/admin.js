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
var tabAdmin = {
	title: 'Administrateur',
 	items: 
	[
		{
            title: 'Configuration',
            items: 
            [
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/config/config_32.png',
                    text: 'Configuration'
                }
            ]
		},
		{
	        title: 'Système',
            items: 
            [
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/system/announcement_off_32.png',
                    text: 'Accès'
                },
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/jvmstatus/jvmstatus_32.png',
                    text: 'Général'
                },
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/jvmstatus/properties_32.png',
                    text: 'Propriétés'
                },
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/jvmstatus/monitoring_32.png',
                    text: 'Monitoring'
                }
            ]
		},
		{
			title: 'Journaux',
            items: 
            [
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/logs/logs_32.png',
                    text: 'Visualisation'
                },
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/logs/loglevel_32.png',
                    text: 'Configuration'
                }
            ]
		},
        {
            title: 'Plugins & Workspaces',
            items: 
            [
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/plugins/plugins_32.png',
                    text: 'Plugins par fichier'
                },
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/plugins/plugins_ep_32.png',
                    text: 'Plugins par point d\'extension'
                },
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/admin/resources/img/plugins/workspaces_32.png',
                    text: 'Workspaces'
                }
            ]
        },
        {
            title: 'Développeur',
            items: 
            [
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/core-ui/resources/img/messages/messages_32.png',
                    handler: function (){messageTracker ();},
                    text: 'Suivi des messages du bus'
                },
                {
                    xtype: 'ametys.ribbon-button',
                    icon: '/ametys/plugins/core-ui/resources/img/requests/requests_32.png',
                    handler: function (){requestTracker ();},
                    text: 'Suivi des requêtes serveur'
                }
            ]
        }
	]	
};

messageTracker = function()
{
    var store = Ext.create("Ext.data.ArrayStore",{
        sorters: [{property: 'fireDate', direction:'DESC'}],
        fields: [
             {name: 'id'},
             {name: 'creationDate', type: 'date', dateFormat: 'Y-m-d'},
             {name: 'fireDate', type: 'date', dateFormat: 'Y-m-d'},
             {name: 'type'},
             {name: 'target'},
             {name: 'callstack'}],
        data: [["1", new Date(), new Date(), "<span style='font-weight: bold'>toolFocused</span><br/>" + Ext.JSON.prettyEncode({"creation":null}), "<span style='font-weight: bold'>tool</span><br/>" + Ext.JSON.prettyEncode({"id":"ui-tool-messagestracker", "tool":"Object Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool@uitool-messagestracker"}), ""],
               ["2", new Date(), new Date(), "<span style='font-weight: bold'>selectionChanged</span><br/>" + Ext.JSON.prettyEncode({}), "<span style='color: #7f7f7f; font-style: italic;'>&lt;pas de cible&gt;</span>", ""],
               ["3", new Date(), new Date(), "<span style='font-weight: bold'>selectionChanging</span><br/>" + Ext.JSON.prettyEncode({"targets":[]}), "<span style='color: #7f7f7f; font-style: italic;'>&lt;pas de cible&gt;</span>", ""],
               ["4", new Date(), new Date(), "<span style='font-weight: bold'>toolFocused</span><br/>" + Ext.JSON.prettyEncode({"creation":null}), "<span style='font-weight: bold'>tool</span><br/>" + Ext.JSON.prettyEncode({"id":"ui-tool-messagestracker", "tool":"Object Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool@uitool-messagestracker"}), ""],
               ["5", new Date(), new Date(), "<span style='font-weight: bold'>selectionChanged</span><br/>" + Ext.JSON.prettyEncode({}), "<span style='color: #7f7f7f; font-style: italic;'>&lt;pas de cible&gt;</span>", ""],
               ["6", new Date(), new Date(), "<span style='font-weight: bold'>selectionChanging</span><br/>" + Ext.JSON.prettyEncode({"targets":[]}), "<span style='color: #7f7f7f; font-style: italic;'>&lt;pas de cible&gt;</span>", ""]]
    });
    
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'Suivi des messages de bus',
        smallIcon: '/plugins/core-ui/resources/img/messages/messages_16.png',
        mediumIcon: '/plugins/core-ui/resources/img/messages/messages_32.png',
        largeIcon: '/plugins/core-ui/resources/img/messages/messages_48.png',
        type: Math.round(Math.random() * 6) * 10,
        layout: 'fit',
        items:[
            Ext.create("Ext.grid.Panel", {
                cls: "uitool-messagestracker",
                store: store,
                scrollable: true,
                columns: [
                    {stateId: 'grid-id', header: "N°", width: 40, sortable: true, dataIndex: 'id', hideable: false},
                    {stateId: 'grid-creationdate', header: "Date de création", width: 120, sortable: true, renderer: Ext.util.Format.dateRenderer("d/m/y H:i:s"), dataIndex: 'creationDate'},
                    {stateId: 'grid-firedate', header: "Date d'envoi", width: 120, sortable: true, renderer: Ext.util.Format.dateRenderer("d/m/y H:i:s"), dataIndex: 'fireDate'},
                    {stateId: 'grid-type', header: "Type", width: 200, sortable: true, dataIndex: 'type'},
                    {stateId: 'grid-target', header: "Cible", flex: 1, sortable: true, dataIndex: 'target'},
                    {stateId: 'grid-callstack', header: "Pile d'appel de création", flex: 0.5, sortable: true, hidden: true, dataIndex: 'callstack'}
                ]
            })
        ]
    });

    layout.addTool(tool, "b");
    layout.focusTool(tool);
}

requestTracker = function()
{
    var store = Ext.create("Ext.data.ArrayStore",{
        sorters: [{property: 'id', direction:'DESC'}],
        fields: [
            {name: 'id'},
            {name: 'type'},
            {name: 'date', type: 'date', dateFormat: 'Y-m-d'},
            {name: 'duration'},
            {name: 'return'},
            {name: 'size'}
        ],
        data: [["1", "Asynchrone", new Date(), "0.146", "3", "Ok"],
               ["2", "Asynchrone", new Date(), "0.153", "2", "Ok"],
               ["3", "Asynchrone", new Date(), "0.204", "2", "Ok"]
            ]
    });
    
    var grid = Ext.create("Ext.grid.Panel", { 
        minHeight: 50,
        flex: 0.3,
        store: store,
        scrollable: true,
        border: true,
        columns: [
            {stateId: 'grid-id', header: "N°", width: 55, sortable: true, dataIndex: 'id', hideable: false},
            {stateId: 'grid-type', header: "Type", width: 85, sortable: true, dataIndex: 'type'},
            {stateId: 'grid-date', header: "Date", width: 130, sortable: true, renderer: Ext.util.Format.dateRenderer("d/m/y H:i:s"), dataIndex: 'date'},
            {stateId: 'grid-duration', header: "Durée", width: 65, sortable: true, dataIndex: 'duration'},
            {stateId: 'grid-size', header: "Taille", width: 60, sortable: true, dataIndex: 'size'},
            {stateId: 'grid-return', header: "Retour", width: 70, sortable: true, dataIndex: 'return'}
        ]
    });

    var msgStore = Ext.create("Ext.data.ArrayStore",{
        sorters: [{property: 'id', direction:'DESC'}],
        fields: [
            {name: 'id'},
            {name: 'readableCallType'},
            {name: 'readableCallValue'},
            {name: 'type'}
        ],
        data: [["0", "Url", "plugins/core/userprefs/save/xml", "xml"],
               ["1", "Url", "plugins/core-ui/system-announcement/view.xml", "xml"],
               ["2", "Url", "plugins/core-ui/system-startup.xml", "xml"]
            ]
    });
    
    var msgGrid = Ext.create("Ext.grid.Panel", { 
        minHeight: 50,
        flex: 0.7,
        border: true,
        split: true,
        store: msgStore,
        scrollable: true,
        columns: [
            {stateId: 'msgrid-id', header: "N°", width: 55, sortable: true, dataIndex: 'id', hideable: false},
            {stateId: 'msgrid-readabletype', header: "Type", width: 70, sortable: true, dataIndex: 'readableCallType'},
            {stateId: 'msgrid-readablevalue', header: "Appel", width: 250, sortable: true, dataIndex: 'readableCallValue'},
            {stateId: 'msgrid-type', header: "Retour", width: 75, sortable: true, dataIndex: 'type'}
        ],
        
        listeners: {'selectionchange': Ext.bind(this._onSelectMessage, this) }
    });
            
    var leftPanel = Ext.create("Ext.Panel", {
        split: true,
        layout: { 
            type: 'vbox',
            align: 'stretch'
        },
        minWidth: 100,
        flex: 0.4,
        items: [ grid, msgGrid ]
    });
            
    var _messageTpl = new Ext.Template(
            "<b>Url</b> : ",
            "{url}<br/>",
            "<b>Paramètres</b> : ",
            "<code class='request-tracker'>{parameters}</code><br/><br/>",
            "<b>Réponse</b> : ",
            "<code class='request-tracker'>{response}</code>"
    );

    var rightPanel = Ext.create("Ext.Component", {
        layout: 'fit',
        scrollable: true,
        minWidth: 100,
        split: true,
        border: true,
        flex: 0.6,
        cls: 'message-details',
        defaultHtml: "",
        html: _messageTpl.applyTemplate({url: "userprefs/save.xml", parameters: Ext.JSON.prettyEncode({workspace:"ametys", prefContext:"/admin", "submit":"true"}), response: xmlstringToHTML("<response id=\"2\" code=\"200\"><xml><userprefs prefCOntext=\"admin\"><tab-policy>all</tab-policy></userprefs></xml></response>")})
    });
    
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'Suivi des requêtes serveur',
        smallIcon: '/plugins/core-ui/resources/img/requests/requests_16.png',
        mediumIcon: '/plugins/core-ui/resources/img/requests/requests_32.png',
        largeIcon: '/plugins/core-ui/resources/img/requests/requests_48.png',
        type: Math.round(Math.random() * 6) * 10,
        layout: 'fit',
        items:[
            Ext.create("Ext.container.Container", {
                layout: { 
                    type: 'hbox',
                    align: 'stretch'
                },
                cls: 'uitool-requesttracker',
                items: [ leftPanel, rightPanel ]
            })
        ]
    });

    layout.addTool(tool, "b");
    layout.focusTool(tool);
}

function xmlstringToHTML(xmlstring)
{
    function escape(htmlstring)
    {
        return htmlstring.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/\r\n/g, '<br/>').replace(/&amp;#160;/g, '&#160;');
    }
    function escapeOpening(tagstring)
    {
        return "<span class='tag'>" + tagstring.replace(/( [^=]+)(=)(\"[^"]*\")/g, "<span class='attr-name'>$1</span><span class='attr-eq'>$2</span><span class='attr-value'>$3</span>") + "</span>";
    }
    function escapeText(textstring, pad)
    {
        var json = Ext.JSON.decode(textstring, true)
        if (json != null)
        {
            var ppad = parseInt(pad/ 2) + 1;
            textstring = Ext.JSON.prettyEncode(json, ppad);
            if (textstring.indexOf("<br/>") != -1)
            {
                var padding = "";
                for (var i = 0; i < ppad; i++) 
                {
                    padding += '&#160;&#160;&#160;&#160;';
                }
                textstring = "<br/>" + padding + textstring + "<br/>";
            }
        }
        
        return "<span class='text'>" + textstring + "</span>";
    }
    function escapeClosing(tagstring)
    {
        return "<span class='tag'>" + tagstring + "</span>";
    }
    
    var formatted = '';
    xml = xmlstring.replace(/(>)(<)(\/*)/g, '$1\r\n$2$3');
    var pad = 0;
    Ext.each(xml.split('\r\n'), function(node, index) 
        {
            var indent = 0;
            if (node.match( /.+<\/\w[^>]*>$/ )) 
            {
                // full tag <test>foo</test>
                node = escape(node);
                
                var i = node.indexOf('>');
                var j = node.indexOf('&lt;', i+1);
                node = escapeOpening(node.substring(0, i+1)) 
                        + escapeText(node.substring(i+1, j), pad) 
                        + escapeClosing(node.substring(j));
                indent = 0;
            } 
            else if (node.match( /^<\/\w/ )) 
            {
                // just a closing tag </test>
                node = escape(node);
                node = escapeClosing(node);
                if (pad != 0) 
                {
                    pad -= 1;
                }
            } 
            else if (node.match( /^<\w[^>]*[^\/]>.*$/ )) 
            {
                // just an opening tag <test attr="1">
                node = escape(node);
                node = escapeOpening(node);
                indent = 1;
            } 
            else 
            {
                // autoclosing tags <test/>
                node = escape(node);
                node = escapeOpening(node);
                indent = 0;
            }
     
            var padding = '';
            for (var i = 0; i < pad; i++) 
            {
                padding += '&#160;&#160;';
            }
     
            formatted += padding + node + '<br/>';
            pad += indent;
        }
    );
 
    return formatted;
}
        