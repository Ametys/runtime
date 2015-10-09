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
var monindex = 1;

function openTool1()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°1 - ' + monindex++,
        description: 'This is my tool numero uno',
        smallIcon: '/test/resources/img/editpaste_16.gif',
        mediumIcon: '/test/resources/img/editpaste_32.gif',
        largeIcon: '/test/resources/img/editpaste_48.gif',
        type: Math.round(Math.random() * 6) * 10,
        html: 'This is tool one<br/>'
            + '<ul>'
            +       '<li>Contextual tab 1 <a href="javascript:tabContextual1.showContextualTab()">show</a> <a href="javascript:tabContextual1.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 2 <a href="javascript:tabContextual2.showContextualTab()">show</a> <a href="javascript:tabContextual2.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 2bis <a href="javascript:tabContextual2bis.showContextualTab()">show</a> <a href="javascript:tabContextual2bis.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 3 <a href="javascript:tabContextual3.showContextualTab()">show</a> <a href="javascript:tabContextual3.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 4 <a href="javascript:tabContextual4.showContextualTab()">show</a> <a href="javascript:tabContextual4.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 5 <a href="javascript:tabContextual5.showContextualTab()">show</a> <a href="javascript:tabContextual5.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 6 <a href="javascript:tabContextual6.showContextualTab()">show</a> <a href="javascript:tabContextual6.hideContextualTab()">hide</a></li>'
            +       '<li>Contextual tab 7 <a href="javascript:tabContextual7.showContextualTab()">show</a> <a href="javascript:tabContextual7.hideContextualTab()">hide</a></li>'
            + '</ul>',
        closable: true
    });

    layout.addTool(tool, "cl");
    layout.focusTool(tool);
}

function openTool2()
{
    store = Ext.create('Ext.data.Store', {
                        fields: ['name', 'gender', { name: 'age', type: 'int' }],
                        data: [
                            { name: 'Joe', gender: 'male', age: 36 },
                            { name: 'Anna', gender: 'female', age: 29 },
                            { name: 'Frederick', gender: 'male', age: 74 }
                        ]
                    });
                    
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°2 - ' + monindex++,
        description: 'This is my tool numero duo',
        smallIcon: '/test/resources/img/editpaste_16.gif',
        mediumIcon: '/test/resources/img/editpaste_32.gif',
        largeIcon: '/test/resources/img/editpaste_48.gif',
        type: Math.round(Math.random() * 6) * 10,
        dockedItems: [ createOodPanel() ],
        layout: 'fit',
        items: [

            {
                xtype: 'gridpanel',
                bbar: [{
                    xtype: 'pagingtoolbar',
                    store: store,
                    displayInfo: true,
                    displayMsg : 'Displaying topics {0} - {1} of {2}'
                }],
                store: store,
                columns: [
                    {
                        xtype: 'gridcolumn',
                        dataIndex: 'name',
                        text: 'Name',
                        flex: 1
                    },
                    {
                        xtype: 'gridcolumn',
                        dataIndex: 'gender',
                        text: 'Gender'
                    },
                    {
                        xtype: 'numbercolumn',
                        dataIndex: 'age',
                        text: 'Age'
                    }
                ]
            }
        
        ]
    });
    
    layout.addTool(tool, "l");
    layout.focusTool(tool);
}

function openTool3()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°3 with a qui long name that is very long in fact - ' + monindex++,
        description: 'This is my tool numero trouo',
        smallIcon: '/test/resources/img/editpaste_16.gif',
        mediumIcon: '/test/resources/img/editpaste_32.gif',
        largeIcon: '/test/resources/img/editpaste_48.gif',
        type: Math.round(Math.random() * 6) * 10,
        scrollable: true,
        closable: true,
        layout: 'absolute',
        items: tools3Items
    });
    
    layout.addTool(tool, "cr");
    layout.focusTool(tool);
}


function openTool4()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°4 - ' + monindex++,
        description: 'This is my tool numero quatro',
        smallIcon: '/test/resources/img/editpaste_16.gif',
        mediumIcon: '/test/resources/img/editpaste_32.gif',
        largeIcon: '/test/resources/img/editpaste_48.gif',
        type: Math.round(Math.random() * 6) * 10,
        items: [ 
            {
                xtype: 'button',
                text: 'Dialog',
                handler: function() {
                                Ext.create('Ametys.window.DialogBox', {
                                    title: "Boite de dialogue...",
                                    icon: Ametys.CONTEXT_PATH + '/test/resources/img/editpaste_48.gif',
                                    
                                    width: 500,
                                    scrollable: true,
                                    layout: 'form',
                                    
                                    items: [{
                                                xtype: 'form',
                                                border: false,
                                                defaults: {
                                                    cls: 'ametys',
                                                    labelAlign: 'top',
                                                    labelSeparator: '',
                                                    labelWidth: 130
                                                },
                                                items: [{
                                                            xtype: 'component',
                                                            cls: 'text',
                                                            html: "Ceci est une boite de dialogue avec un texte d'introduction un petit peu long, qui peut même nécessiter plusieurs lignes."
                                                        }, 
                                                        {
                                                            xtype: 'textfield',
                                                            fieldLabel : "Saisissez quelque chose de vert",
                                                            name: 'url',
                                                            itemId: 'url',
                                                            width: 450,
                                                            allowBlank: false,
                                                            msgTarget: 'side'
                                                        },
                                                        {
                                                            xtype: 'component',
                                                            cls: 'text',
                                                            html: "Ceci est un petit message de fin de page"
                                                        }
                                                ]
                                            }
                                    ],
                                    
                                    defaultFocus: 'url',
                                    closeAction: 'hide',
                                    buttons : [{
                                        text :"Ok"
                                    }, {
                                        text :"Annuler"
                                    }]
                                }).show();
                            
                }
            }
        ]
    });
    
    layout.addTool(tool, "r");
    layout.focusTool(tool);
}



function openTool5()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°5 - ' + monindex++,
        description: 'This is my tool numero cinquo',
        smallIcon: '/test/resources/img/editpaste_16.gif',
        mediumIcon: '/test/resources/img/editpaste_32.gif',
        largeIcon: '/test/resources/img/editpaste_48.gif',
        type: Math.round(Math.random() * 6) * 10,
        html: "<b>je suis le contenu du Tool 5</b><br/>Pensez à me drag'n'droper dans une autre zone et à me ramener ici ensuite pour tester"
    });
    
    layout.addTool(tool, "t");
    layout.focusTool(tool);
}



function openTool6()
{
    var tool = Ext.create("Ametys.ui.tool.ToolPanel", {
        title: 'My Tool n°6 - ' + monindex++,
        description: 'This is my tool numero sexto',
        smallIcon: '/test/resources/img/editpaste_16.gif',
        mediumIcon: '/test/resources/img/editpaste_32.gif',
        largeIcon: '/test/resources/img/editpaste_48.gif',
        type: Math.round(Math.random() * 6) * 10,
        html: "<b>je suis le contenu du Tool 6</b>"
    });
    
    layout.addTool(tool, "b");
    layout.focusTool(tool);
}


function createOodPanel()
{
    var button = Ext.create("Ext.Button", {
        dock: 'top',

        ui: 'tool-outofdate',
        textAlign: 'left',
        text:"Data are not up to date. Click here to refresh this tool.",
        tooltip: {
            title: "Out of date",
            image: Ametys.CONTEXT_PATH + "/kernelresources/img/Ametys/theme/gray/uitool/reload_32.png",
            imageWidth: 32,
            imageHeight: 32,
            text: "Data currently displayed are not up to date.<br/><br/>By clicking on the yellow bar you will refresh the view.<br/><br/><span style='font-style: italic'>The refresh is not automatic for performances purpose.</span>",
            inribbon: false
        },
        handler: Ext.bind(this.refresh, this, [true], false)
    });
    
    return button;
}