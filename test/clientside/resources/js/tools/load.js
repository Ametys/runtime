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

function loadStyle(url, media)
{
    var head = document.getElementsByTagName("head")[0];
    var link = document.createElement("link");
    link.rel = "stylesheet";
    link.href = url;
    link.type = "text/css";
    if (media)
    {
        link.media = media;
    }
    head.appendChild(link);
}

function loadScript(url, onLoad, onError)
{
    var me = this;
    function internalOnError (msg)
    {
        var message = "Echec lors du chargement du fichier" + url;
        me.getLogger().error(message);
        
        if (Ext.isFunction (onError))
        {
            onError.call (null, msg);
        }
    }
    
    function internalOnLoad (msg)
    {
        if (me.getLogger().isInfoEnabled())
        {
            var message = "Chargement du fichier" + url;
            me.getLogger().info(message);
        }
        
        if (Ext.isFunction (onLoad))
        {
            onLoad.call ();
        }
    }
    
    // Disable cache to avoid URL ends with '?_dc=1379928434854'
    Ext.Loader.setConfig ({
        disableCaching : false
    });
    
    Ext.Loader.scripts.push({
        url: url,   
        onLoad: internalOnLoad,
        onError: internalOnError
    });
    
    Ext.Loader.setConfig ({
        disableCaching : true
    });
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

a = Ext.BLANK_IMAGE_URL;
ametys_opts = { 'context-path': '/ametys', 'language-code': 'fr'};

var isFashion = getParameterByName("fashion");

if (isFashion == "false")
{
    loadStyle("/packages/local/theme-ametys-base/build/resources/theme-ametys-base-all-debug.css");
}

loadStyle("/ext/build/packages/ux/classic/neptune/resources/ux-all-debug.css");

var scripts = [];

if (isFashion == "false")
{
    scripts.push("/packages/local/theme-ametys-base/build/theme-ametys-base-debug.js");
}
else
{
    scripts.push("/ext/build/classic/theme-neptune/theme-neptune-debug.js");
    scripts.push("/packages/local/theme-ametys-base/overrides/Ametys/ui/fluent/ribbon/Ribbon/ContextualTabGroup.js");
    scripts.push("/packages/local/theme-ametys-base/overrides/Ametys/ui/fluent/ribbon/GroupScale.js");
    scripts.push("/packages/local/theme-ametys-base/overrides/Ametys/ui/fluent/ribbon/TabPanel.js");
    scripts.push("/packages/local/theme-ametys-base/overrides/Ametys/ui/tool/layout/ZonedTabsToolsLayout/ZoneTabsToolsPanel.js");
    scripts.push("/~cmd/extensions/sencha-fashion/fashion/fashion.js");
    scripts.push("/~cmd/extensions/sencha-fashion/sass-compiler.js");
}

scripts.push("/ext/build/packages/ux/classic/ux-debug.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ext.fixes.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ext.enhancements.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/log/Logger.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/log/Logger/Entry.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/log/LoggerFactory.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/log/ErrorDialog.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/window/DialogBox.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/window/MessageBox.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/AbstractField.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/AbstractFieldsWrapper.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/DateTime.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/ChangePassword.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/ReferencedNumberField.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/RichText.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/Code.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/TextArea.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/Password.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/field/StringTime.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel/Repeater.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel/ParameterChecker.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/ConfigurableFormPanel/ParameterCheckersDAO.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/data/ServerCaller.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/data/ServerComm.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/data/ServerCommProxy.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/data/ServerComm/TimeoutDialog.js");
            
            
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/tip/Tooltip.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/misc/Badge.js");
            
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Title.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/ContextualTabGroupContainer.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/ContextualTabGroup.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/MessageContainer.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/SearchMenu.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Notificator.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Notificator/Notification.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Ribbon/Notificator/Toast.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/TabPanel.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Panel.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/Group.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/GroupScale.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/GroupScalePart.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/RibbonButtonMixin.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/Button.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/SplitButton.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/Toolbar.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/fluent/ribbon/controls/gallery/MenuPanel.js");

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/WidgetManager.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/widget/DefaultWidgets.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/form/widget/GeoCode.js");
            

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/tool/ToolPanel.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/tool/ToolsLayout.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout/ZoneTabsToolsPanel.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ui/tool/layout/ZonedTabsToolsLayout/ZonedTabsDD.js");
            
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/message/Message.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/message/MessageBus.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/message/MessageTarget.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/message/MessageTargetHelper.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/message/MessageTargetFactory.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/message/factory/DefaultMessageTargetFactory.js");

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/RelationManager.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/RelationHandler.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/RelationPoint.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/Relation.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysDropZone.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysViewDragZone.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysTreeViewDragDrop.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysTreeViewDragZone.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysTreeViewDropZone.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysGridViewDragDrop.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/relation/dd/AmetysGridViewDropZone.js");

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/RibbonManager.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/RibbonElementController.js");

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/RibbonTabController.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/tab/TabController.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/tab/ContentTabController.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/tab/EditionTabController.js");

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/RibbonUIController.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/CommonController.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/FieldController.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/ButtonController.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/ribbon/element/ui/button/OpenToolButtonController.js");
        
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/tool/ToolsManager.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/tool/ToolFactory.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/tool/factory/BasicToolFactory.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/tool/factory/UniqueToolFactory.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/tool/ToolMessageTargetFactory.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/tool/Tool.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/tool/SelectionTool.js");

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/userprefs/UserPrefsDAO.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/userprefs/UserPrefsDAOStateProvider.js");

scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/plugins/coreui/system/messagetracker/MessageTrackerTool.js");
scripts.push("/ametys/plugins/core-ui/resources/js/Ametys/plugins/coreui/system/messagetracker/MessageTrackerTool/MessageEntry.js");

alert(scripts.length);
var i = 0;

(function loadScript()
{
    alert(scripts[i]);
    loadScript(scripts[i++], loadScript);
})();



ribbonItems = []; 

scripts.push("resources/js/tools/test.js");
scripts.push("resources/js/tools/developer.js");
scripts.push("resources/js/tools/messagetargetfactories.js");

function afterLoad()
{
    Ext.BLANK_IMAGE_URL = a;
    ametys_opts = { 'context-path': '', 'language-code': 'fr'};
    
    Ametys.userprefs.UserPrefsDAO.setDefaultPrefContext("/test");
    Ametys.userprefs.UserPrefsDAO.preload({});
    Ametys.userprefs.UserPrefsDAO.saveValues = function(params, callback, prefContext, priority, cancelCode) { callback(true, null); ;}
    var upp = Ext.create('Ametys.userprefs.UserPrefsDAOStateProvider', { preference: 'workspace' })
    Ext.state.Manager.setProvider(upp);
}