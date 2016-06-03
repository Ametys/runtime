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
 
var factory, control;

/*
 * OUTILS
 */
 
// messages de bus
factory = Ext.create("Ametys.tool.factory.UniqueToolFactory", Ext.apply({"toolClass":"Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool","title":"Suivi des messages du bus","description":"Voir les messages du bus","help":"org.ametys.runtime.messagetracker","icon-small":"/plugins/core-ui/resources/img/messages/messages_16.png","icon-medium":"/plugins/core-ui/resources/img/messages/messages_32.png","icon-large":"/plugins/core-ui/resources/img/messages/messages_48.png","default-location":"b"}, {id: "uitool-messagestracker", pluginName: "core-ui"}));
Ametys.tool.ToolsManager.addFactory(factory);

/*
 * BOUTONS
 */

// messages de bus
control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"primary-menu-item-id":"org.ametys.runtime.userinterface.MessageTrackerControl","opentool-id":"uitool-messagestracker","label":"Suivi des messages du bus","description":"Voir les messages du bus","help":"org.ametys.runtime.messagetracker","icon-small":"/plugins/core-ui/resources/img/messages/messages_16.png","icon-medium":"/plugins/core-ui/resources/img/messages/messages_32.png","icon-large":"/plugins/core-ui/resources/img/messages/messages_48.png","menu-items":["org.ametys.runtime.userinterface.MessageTrackerControl","org.ametys.runtime.userinterface.MessageTrackerControl.clear"]}, {id: "org.ametys.runtime.userinterface.MessageTrackerControlMenu", pluginName: "core-ui"}));
Ametys.ribbon.RibbonManager.registerUI(control);

control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-messagestracker","label":"Suivi des messages du bus","description":"Voir les messages du bus","help":"org.ametys.runtime.messagetracker","icon-small":"/plugins/core-ui/resources/img/messages/messages_16.png","icon-medium":"/plugins/core-ui/resources/img/messages/messages_32.png","icon-large":"/plugins/core-ui/resources/img/messages/messages_48.png"}, {id: "org.ametys.runtime.userinterface.MessageTrackerControl", pluginName: "core-ui"}));
Ametys.ribbon.RibbonManager.registerUI(control);

control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.coreui.system.messagetracker.MessageTrackerTool.removeAll","tool-id":"uitool-messagestracker","tool-enable-on-status":"active","tool-description-inactive":"Ce bouton est désactivé car la fenêtre de suivi des messages du bus n'est actuellement pas ouverte","label":"Effacer les messages affichés","description":"Cliquez sur ce bouton pour effacer les messages du bus actuellement visible dans la vue de suivi.","icon-small":"/plugins/core-ui/resources/img/messages/delete_16.gif","icon-medium":"/plugins/core-ui/resources/img/messages/delete_16.gif","icon-large":"/plugins/core-ui/resources/img/messages/delete_16.gif"}, {id: "org.ametys.runtime.userinterface.MessageTrackerControl.clear", pluginName: "core-ui"}));
Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"primary-menu-item-id":"org.ametys.runtime.userinterface.RequestTrackerControl","opentool-id":"uitool-requeststracker","label":"Suivi des requêtes serveur","default-description":"Voir les messages envoyés au serveur","help":"org.ametys.runtime.requesttracker","icon-small":"/plugins/core-ui/resources/img/requests/requests_16.png","icon-medium":"/plugins/core-ui/resources/img/requests/requests_32.png","icon-large":"/plugins/core-ui/resources/img/requests/requests_48.png","menu-items":["org.ametys.runtime.userinterface.RequestTrackerControl","org.ametys.runtime.userinterface.RequestTrackerControl.clear"]}, {id: "org.ametys.runtime.userinterface.RequestTrackerControlMenu", pluginName: "core-ui"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-requeststracker","label":"Suivi des requêtes serveur","default-description":"Voir les messages envoyés au serveur","help":"org.ametys.runtime.requesttracker","icon-small":"/plugins/core-ui/resources/img/requests/requests_16.png","icon-medium":"/plugins/core-ui/resources/img/requests/requests_32.png","icon-large":"/plugins/core-ui/resources/img/requests/requests_48.png"}, {id: "org.ametys.runtime.userinterface.RequestTrackerControl", pluginName: "core-ui"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.coreui.system.requesttracker.RequestTrackerTool.removeAll","tool-id":"uitool-requeststracker","tool-enable-on-status":"active","tool-description-inactive":"Ce bouton est désactivé car la fenêtre de suivi des requêtes du bus n'est actuellement pas ouverte","label":"Effacer les requêtes affichées","description":"Cliquez sur ce bouton pour effacer les messages de requêtes serveur actuellement visible dans la vue de suivi.","description-footer":"Voir l'aide pour plus de détails","icon-small":"/plugins/core-ui/resources/img/requests/delete_16.gif","icon-medium":"/plugins/core-ui/resources/img/requests/delete_16.gif","icon-large":"/plugins/core-ui/resources/img/requests/delete_16.gif"}, {id: "org.ametys.runtime.userinterface.RequestTrackerControl.clear", pluginName: "core-ui"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

/*
 * RIBBON
 */
 /** Tab 3 */
var tab_3 = Ext.create("Ametys.ui.fluent.ribbon.Panel", {title: "Développeur",items: []});

var fgp_3_1_small = [];
var fgp_3_1_medium = [];
var fgp_3_1_large = [];
fgp_3_1_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.runtime.userinterface.MessageTrackerControlMenu").addUI("large"));
fgp_3_1_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.runtime.userinterface.RequestTrackerControlMenu").addUI("large"));

    var fgp_3_1 = {title: 'Outils',
        priority: 0,
        smallItems: fgp_3_1_small,
        items: fgp_3_1_medium,
        largeItems: fgp_3_1_large
    };

    // Dialog box launcher
    
    
    tab_3.add(fgp_3_1);

ribbonItems.push(tab_3);
