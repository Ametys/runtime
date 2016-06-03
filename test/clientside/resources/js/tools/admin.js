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
 
   var control;

/*
 * BOUTONS
 */

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.system.SystemActions.editMessage","selection-target-id":"^system-announcement-message","label":"Modifier","description":"Modifie l'annonce sélectionnée.","icon-small":"/plugins/admin/resources/img/system/edit_announcement_16.png","icon-medium":"/plugins/admin/resources/img/system/edit_announcement_32.png","icon-large":"/plugins/admin/resources/img/system/edit_announcement_48.png"}, {id: "org.ametys.plugins.admin.system.Edit", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.clearCache","selection-target-id":"^(site)$","label":"Vider le cache du site","description":"Vide le cache du site sélectionné","icon-small":"/plugins/web/resources/img/site/clear_site_16.png","icon-medium":"/plugins/web/resources/img/site/clear_site_32.png","icon-large":"/plugins/web/resources/img/site/clear_site_48.png"}, {id: "org.ametys.web.admin.site.ClearCache", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-user-rights","label":"Voir les droits","description":"Droits de l'utilisateur sélectionné","selection-target-id":"^user$","selection-description-empty":"Aucun utilisateur n'est actuellement sélectionné.<br/>Sélectionnez un utilisateur pour visualiser ses droits.","selection-description-nomatch":"Aucun utilisateur n'est actuellement sélectionné.<br/>Sélectionnez un utilisateur pour visualiser ses droits.","selection-description-multiselectionforbidden":"Plusieurs utilisateurs sont actuellement sélectionnés.<br/>Sélectionnez un utilisateur pour visualiser ses droits.","help":"org.ametys.core.users.rights","icon-small":"/plugins/core/resources/img/users/rights_16.png","icon-medium":"/plugins/core/resources/img/users/rights_32.png","icon-large":"/plugins/core/resources/img/users/rights_48.png"}, {id: "org.ametys.core.users.Rights", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.plugins.admin.logs.LogLevelController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.changeLogLevel","selection-target-id":"^(log-category)$","selection-enable-multiselection":"false","level":"INFO","label":"Passer en INFO","description":"Changer le niveau de log de la catégorie sélectionnée en \"info\".","icon-small":"/plugins/admin/resources/img/logs/level_info_16.png","icon-medium":"/plugins/admin/resources/img/logs/level_info_32.png","icon-large":"/plugins/admin/resources/img/logs/level_info_48.png"}, {id: "org.ametys.plugins.admin.logslevel.Info", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.skin.SkinActions.duplicate","selection-target-id":"^skin$","label":"Dupliquer","description":"Créer une nouvelle charte à partir de la charte sélectionnée","icon-small":"/plugins/web/resources/img/administrator/skins/copy_16.png","icon-medium":"/plugins/web/resources/img/administrator/skins/copy_32.png","icon-large":"/plugins/web/resources/img/administrator/skins/copy_48.png"}, {id: "org.ametys.web.admin.skin.duplicate", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.skin.SkinActions.applyModel","selection-target-id":"^skin$","selection-target-parameter":{"name":"model","value":"!null"},"label":"Réappliquer le modèle","description":"Réappliquer le modèle à la charte sélectionnée","icon-small":"/plugins/skinfactory/resources/img/model/apply_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/apply_32.png","icon-large":"/plugins/skinfactory/resources/img/model/apply_48.png"}, {id: "org.ametys.skinfactory.admin.skin.applymodel", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-config","label":"Configuration","description":"Réglage des paramètres de configuration","icon-small":"/plugins/admin/resources/img/config/config_16.png","icon-medium":"/plugins/admin/resources/img/config/config_32.png","icon-large":"/plugins/admin/resources/img/config/config_48.png"}, {id: "org.ametys.plugins.admin.config.Open", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.plugins.PluginsActions.select","selection-target-id":"^(plugin-by-file-node|plugin-by-extension-point-node)$","selection-target-parameter":{"name":"selectable","value":"true"},"label":"Sélectionner","description":"Sélectionne le point d'extension.","icon-small":"/plugins/admin/resources/img/plugins/select_16.png","icon-medium":"/plugins/admin/resources/img/plugins/select_32.png","icon-large":"/plugins/admin/resources/img/plugins/select_48.png"}, {id: "org.ametys.plugins.admin.plugins.Select", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.plugins.admin.plugins.SaveChangesController", Ext.apply({"action":"Ametys.plugins.admin.plugins.PluginsActions.saveChanges","disabled":"true","label":"Sauvegarder les modifications","description":"Applique les modifications réalisées.","icon-small":"/plugins/admin/resources/img/plugins/save_16.png","icon-medium":"/plugins/admin/resources/img/plugins/save_32.png","icon-large":"/plugins/admin/resources/img/plugins/save_48.png"}, {id: "org.ametys.plugins.admin.plugins.SaveChanges", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.plugins.coreui.profiles.controller.SelectRightsController", Ext.apply({"action":"Ametys.plugins.coreui.profiles.ProfilesTool.unselectAll","disabled":"true","selection-target-id":"^profile$","selection-subtarget-id":"^form$","label":"Désélectionner tout","description":"Désélectionne tous les droits","icon-small":"/plugins/core/resources/img/profiles/unselect_16.png","icon-medium":"/plugins/core/resources/img/profiles/unselect_32.png","icon-large":"/plugins/core/resources/img/profiles/unselect_48.png"}, {id: "org.ametys.core.profiles.UnselectAll", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-logs","label":"Visualisation","description":"Visualiser les journaux de l'application","icon-small":"/plugins/admin/resources/img/logs/logs_16.png","icon-medium":"/plugins/admin/resources/img/logs/logs_32.png","icon-large":"/plugins/admin/resources/img/logs/logs_48.png"}, {id: "org.ametys.plugins.admin.Logs", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.view","selection-target-id":"^logfile$","selection-enable-multiselection":"false","label":"Voir","description":"Voir le fichier sélectionné","icon-small":"/plugins/admin/resources/img/logs/view_16.png","icon-medium":"/plugins/admin/resources/img/logs/view_32.png","icon-large":"/plugins/admin/resources/img/logs/view_48.png"}, {id: "org.ametys.plugins.admin.logs.View", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.plugins.admin.logs.LogLevelController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.changeLogLevel","selection-target-id":"^(log-category)$","selection-enable-multiselection":"false","level":"DEBUG","label":"Passer en DEBUG","description":"Changer le niveau de log de la catégorie sélectionnée en \"debug\".","icon-small":"/plugins/admin/resources/img/logs/level_debug_16.png","icon-medium":"/plugins/admin/resources/img/logs/level_debug_32.png","icon-large":"/plugins/admin/resources/img/logs/level_debug_48.png"}, {id: "org.ametys.plugins.admin.logslevel.Debug", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.plugins.coreui.profiles.controller.ViewModeController", Ext.apply({"action":"Ametys.plugins.coreui.profiles.ProfilesTool.switchMode","enable-toggle":"true","tool-id":"uitool-profile","tool-enable-on-status":"focus","selection-target-id":"^profile$","label":"Mode édition","description":"Passe en mode d'édition des droits (ajout ou suppression) du profil sélectionné","icon-small":"/plugins/core/resources/img/profiles/edit_16.png","icon-medium":"/plugins/core/resources/img/profiles/edit_32.png","icon-large":"/plugins/core/resources/img/profiles/edit_48.png"}, {id: "org.ametys.core.profiles.SwitchMode", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.core.superuser.SuperUserActions.affectSuperUser","selection-target-id":"^(site)$","label":"Affecter un gestionnaire","description":"Affecte un gestionnaire","icon-small":"/plugins/core/resources/img/superuser/super_user_16.png","icon-medium":"/plugins/core/resources/img/superuser/super_user_32.png","icon-large":"/plugins/core/resources/img/superuser/super_user_48.png"}, {id: "org.ametys.web.admin.site.SuperUser", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.plugins.admin.logs.LogLevelController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.changeLogLevel","selection-target-id":"^(log-category)$","selection-enable-multiselection":"false","level":"FORCE","label":"Forcer l'héritage","description":"Forcer l'héritage de niveau de log de la catégorie sélectionnée.","icon-small":"/plugins/admin/resources/img/logs/force_16.png","icon-medium":"/plugins/admin/resources/img/logs/force_32.png","icon-large":"/plugins/admin/resources/img/logs/force_48.png"}, {id: "org.ametys.plugins.admin.logslevel.Force", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.model.SkinModelActions.filterSkins","selection-target-id":"^skin-model$","toggle-enabled":"true","label":"Voir les chartes du modèle","description":"Filter les chartes graphiques en fonction du modèle sélectionné","icon-small":"/plugins/skinfactory/resources/img/model/filter_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/filter_32.png","icon-large":"/plugins/skinfactory/resources/img/model/filter_48.png"}, {id: "org.ametys.skinfactory.admin.model.filterskins", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.plugins.PluginsActions.showDocumentation","selection-target-id":"^(plugin-by-file-node|plugin-by-extension-point-node)$","label":"Documentation","description":"Voir la documentation rattachée au noeud sélectionné.","icon-small":"/plugins/admin/resources/img/plugins/documentation_16.png","icon-medium":"/plugins/admin/resources/img/plugins/documentation_32.png","icon-large":"/plugins/admin/resources/img/plugins/documentation_48.png"}, {id: "org.ametys.plugins.admin.plugins.Documentation", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.plugins.coreui.profiles.controller.SelectRightsController", Ext.apply({"action":"Ametys.plugins.coreui.profiles.ProfilesTool.selectAll","disabled":"true","selection-target-id":"^profile$","selection-subtarget-id":"^form$","label":"Sélectionner tout","description":"Sélectionne tous les droits","icon-small":"/plugins/core/resources/img/profiles/select_16.png","icon-medium":"/plugins/core/resources/img/profiles/select_32.png","icon-large":"/plugins/core/resources/img/profiles/select_48.png"}, {id: "org.ametys.core.profiles.SelectAll", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-pluginsbyextensionpoint","label":"Plugins par point d'extension","description":"Ouvre un outil permettant de visualiser les plugins par point d'extension sous forme d'un arbre. Permet d'activer/désactiver des plugins.","icon-small":"/plugins/admin/resources/img/plugins/plugins_ep_16.png","icon-medium":"/plugins/admin/resources/img/plugins/plugins_ep_32.png","icon-large":"/plugins/admin/resources/img/plugins/plugins_ep_48.png"}, {id: "org.ametys.plugins.admin.PluginsByExtensionPoint", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.skin.SkinActions.unsaveConfig","selection-target-id":"^(skin|skin-temp)$","selection-subtarget-id":"^(skin-configuration)$","label":"Fermer sans sauver","description":"Annule les modifications","icon-small":"/plugins/web/resources/img/skin/config/unsave_16.png","icon-medium":"/plugins/web/resources/img/skin/config/unsave_32.png","icon-large":"/plugins/web/resources/img/skin/config/unsave_48.png"}, {id: "org.ametys.web.admin.skin.UnsaveConfig", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.plugins.coreui.configurableformpanel.TestsController", Ext.apply({"action":"Ametys.plugins.coreui.configurableformpanel.TestsController.check","mode":"missed","selection-target-id":"^form$","label":"Tests manqués","description":"Lancer tous les tests qui ne sont pas en statut \"Réussi\".","icon-small":"/plugins/core-ui/resources/img/Ametys/common/form/configurable/tests/testmissing_16.png","icon-medium":"/plugins/core-ui/resources/img/Ametys/common/form/configurable/tests/testmissing_32.png","icon-large":"/plugins/core-ui/resources/img/Ametys/common/form/configurable/tests/testmissing_48.png"}, {id: "org.ametys.plugins.core.configurableformpanel.CheckMissed", pluginName: "core-ui"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.skin.SkinActions.unlinkModel","selection-target-id":"^skin$","selection-target-parameter":{"name":"model","value":"!null"},"label":"Supprimer le lien avec le modèle","description":"Supprime le lien avec le modèle","icon-small":"/plugins/skinfactory/resources/img/model/unlink_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/unlink_32.png","icon-large":"/plugins/skinfactory/resources/img/model/unlink_48.png"}, {id: "org.ametys.skinfactory.admin.skin.unlinkmodel", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-pluginsbyfile","label":"Plugins par fichier","description":"Ouvre un outil permettant de visualiser les plugins par fichiers sous forme d'un arbre. Permet d'activer/désactiver des plugins.","icon-small":"/plugins/admin/resources/img/plugins/plugins_16.png","icon-medium":"/plugins/admin/resources/img/plugins/plugins_32.png","icon-large":"/plugins/admin/resources/img/plugins/plugins_48.png"}, {id: "org.ametys.plugins.admin.PluginsByFile", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.plugins.admin.plugins.SaveChangesController", Ext.apply({"action":"Ametys.plugins.admin.plugins.PluginsActions.cancelChanges","disabled":"true","label":"Annuler les modifications","description":"Annuler les modifications réalisées.","icon-small":"/plugins/admin/resources/img/plugins/unsave_16.png","icon-medium":"/plugins/admin/resources/img/plugins/unsave_32.png","icon-large":"/plugins/admin/resources/img/plugins/unsave_48.png"}, {id: "org.ametys.plugins.admin.plugins.CancelChanges", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.plugins.PluginsActions.deactivate","selection-target-id":"^(plugin-by-file-node|plugin-by-extension-point-node)$","selection-target-parameter":{"name":"^activeFeature$","value":"true"},"label":"Désactiver","description":"Permet de désactiver la feature sélectionnée.","icon-small":"/plugins/admin/resources/img/plugins/deactivate_16.png","icon-medium":"/plugins/admin/resources/img/plugins/deactivate_32.png","icon-large":"/plugins/admin/resources/img/plugins/deactivate_48.png"}, {id: "org.ametys.plugins.admin.plugins.Deactivate", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.add","selection-target-id":"^(root-sites|site)$","label":"Nouveau site","description":"Crée un nouveau site","icon-small":"/plugins/web/resources/img/site/add_site_16.png","icon-medium":"/plugins/web/resources/img/site/add_site_32.png","icon-large":"/plugins/web/resources/img/site/add_site_48.png"}, {id: "org.ametys.web.admin.site.Add", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-groups","label":"Groupes d'utilisateurs","description":"Liste des groupes et de leurs utilisateurs","help":"org.ametys.core.groups","icon-small":"/plugins/core/resources/img/groups/group_16.png","icon-medium":"/plugins/core/resources/img/groups/group_32.png","icon-large":"/plugins/core/resources/img/groups/group_50.png"}, {id: "org.ametys.core.Groups", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.download","selection-target-id":"^logfile$","label":"Télécharger","description":"Télécharger le ou les fichier(s) sélectionné(s)","icon-small":"/plugins/admin/resources/img/logs/download_16.png","icon-medium":"/plugins/admin/resources/img/logs/download_32.png","icon-large":"/plugins/admin/resources/img/logs/download_48.png"}, {id: "org.ametys.plugins.admin.logs.Download", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.deleteFile","selection-target-id":"^logfile$","label":"Supprimer","description":"Supprimer le(s) fichier(s) sélectionnés","icon-small":"/plugins/admin/resources/img/logs/delete_16.png","icon-medium":"/plugins/admin/resources/img/logs/delete_32.png","icon-large":"/plugins/admin/resources/img/logs/delete_48.png"}, {id: "org.ametys.plugins.admin.logs.Delete", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.skin.SkinActions.configure","selection-target-id":"^skin$","label":"Configurer","description":"Modifie la configuration de la charte graphique.","icon-small":"/plugins/web/resources/img/administrator/skins/configure_16.png","icon-medium":"/plugins/web/resources/img/administrator/skins/configure_32.png","icon-large":"/plugins/web/resources/img/administrator/skins/configure_48.png"}, {id: "org.ametys.web.admin.skin.configure", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-systemproperties","label":"Propriétés","description":"Cet outil vous permet de visualiser les propriétés système de lancement de la machine virtuelle java.","icon-small":"/plugins/admin/resources/img/jvmstatus/properties_16.png","icon-medium":"/plugins/admin/resources/img/jvmstatus/properties_32.png","icon-large":"/plugins/admin/resources/img/jvmstatus/properties_48.png"}, {id: "org.ametys.plugins.admin.SystemProperties", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.skin.SkinActions.export","selection-target-id":"^skin$","label":"Exporter","description":"Exporter la charte au format ZIP","icon-small":"/plugins/web/resources/img/administrator/skins/export_16.png","icon-medium":"/plugins/web/resources/img/administrator/skins/export_32.png","icon-large":"/plugins/web/resources/img/administrator/skins/export_48.png"}, {id: "org.ametys.web.admin.skin.export", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.system.SystemActions.addMessage","label":"Nouveau","description":"Crée une nouvelle annonce.","icon-small":"/plugins/admin/resources/img/system/add_announcement_16.png","icon-medium":"/plugins/admin/resources/img/system/add_announcement_32.png","icon-large":"/plugins/admin/resources/img/system/add_announcement_48.png"}, {id: "org.ametys.plugins.admin.system.Add", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.cms.indexing.ReindexContents.act","label":"Indexation totale","description":"Lancer l'indexation totale des contenus","icon-small":"/plugins/cms/resources/img/indexing/reindex_16.png","icon-medium":"/plugins/cms/resources/img/indexing/reindex_32.png","icon-large":"/plugins/cms/resources/img/indexing/reindex_48.png"}, {id: "org.ametys.cms.content.indexing.solr.admin.ReindexAllContents", pluginName: "cms"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.model.SkinModelActions.generateSkin","selection-target-id":"^skin-model$","label":"Générer une charte","description":"Génére une nouvelle charte graphique à partir du modèle sélectionné","icon-small":"/plugins/skinfactory/resources/img/model/generate_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/generate_32.png","icon-large":"/plugins/skinfactory/resources/img/model/generate_48.png"}, {id: "org.ametys.skinfactory.admin.model.generateskin", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-sites","label":"Sites","description":"Cet outil permet de gérer et configurer les sites","icon-small":"/plugins/web/resources/img/site/site_16.png","icon-medium":"/plugins/web/resources/img/site/site_32.png","icon-large":"/plugins/web/resources/img/site/site_48.png"}, {id: "org.ametys.web.admin.Sites", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.plugins.admin.system.SystemAnnouncementController", Ext.apply({"action":"Ametys.plugins.admin.system.SystemActions.setAnnouncementAvailable","toggle-enabled":"true","label":"Activer","description":"Active ou désactive l'annonce système.","announcement-on-description":"L'annonce système d'accueil est actuellement activée.<br/><br/>Cliquez ici pour désactiver l'annonce système d'accueil.","announcement-off-description":"L'annonce système d'accueil est actuellement désactivée.<br/><br/>Cliquez ici pour activer l'annonce système d'accueil.","icon-small":"/plugins/admin/resources/img/system/activate_16.png","icon-medium":"/plugins/admin/resources/img/system/activate_32.png","icon-large":"/plugins/admin/resources/img/system/activate_48.png","announcement-on-icon-small":"/plugins/admin/resources/img/system/deactivate_16.png","announcement-on-icon-medium":"/plugins/admin/resources/img/system/deactivate_32.png","announcement-on-icon-large":"/plugins/admin/resources/img/system/deactivate_48.png","announcement-off-icon-small":"/plugins/admin/resources/img/system/activate_16.png","announcement-off-icon-medium":"/plugins/admin/resources/img/system/activate_32.png","announcement-off-icon-large":"/plugins/admin/resources/img/system/activate_48.png","toggle-state":false,"available":false}, {id: "org.ametys.plugins.admin.system.SetAnnouncement", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.repository.admin.JCRRepositoryLink.open","label":"Repository JCR","description":"Accès à l'explorateur JCR","icon-small":"/plugins/repository/resources/img/administrator/jcr_16.png","icon-medium":"/plugins/repository/resources/img/administrator/jcr_32.png","icon-large":"/plugins/repository/resources/img/administrator/jcr_48.png"}, {id: "org.ametys.plugins.repository.administrator.JCRRepository", pluginName: "repository"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.coreui.users.UsersActions.impersonate","label":"Prendre le contrôle","description":"Se connecter au CMS avec les identifiants de l'utilisateur sélectionné.","selection-target-id":"^user$","selection-enable-multiselection":"false","selection-description-multiselectionforbidden":"Plusieurs utilisateurs sont actuellement sélectionnés.<br/>Sélectionnez un seul utilisateur afin de pouvoir se connecter avec ses identifiants.","help":"org.ametys.core.users.rights","icon-small":"/plugins/core/resources/img/users/impersonate_16.png","icon-medium":"/plugins/core/resources/img/users/impersonate_32.png","icon-large":"/plugins/core/resources/img/users/impersonate_48.png"}, {id: "org.ametys.core.users.Impersonate", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.buildAll","label":"Reconstruire le live complet","description":"Reconstruit tout le workspace live (les sites ne seront plus accessibles le temps de cette reconstruction).<br/>Vous pouvez reconstruire le workspace 'live' d'un seul site en sélectionnant un site.","icon-small":"/plugins/web/resources/img/site/build_live_16.png","icon-medium":"/plugins/web/resources/img/site/build_live_32.png","icon-large":"/plugins/web/resources/img/site/build_live_48.png"}, {id: "org.ametys.web.admin.site.BuildAll", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.open","selection-target-id":"^(site)$","label":"Ouvrir dans le CMS","description":"Ouvre le site sélectionné dans l'espace de contribution CMS","icon-small":"/plugins/web/resources/img/site/open_16.png","icon-medium":"/plugins/web/resources/img/site/open_32.png","icon-large":"/plugins/web/resources/img/site/open_48.png"}, {id: "org.ametys.web.admin.site.Open", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.saveConfig","selection-target-id":"^(site)$","selection-subtarget-id":"^(site-configuration)$","label":"Sauvegarder","description":"Sauvegarder les modifications","icon-small":"/plugins/web/resources/img/site/config/save_16.png","icon-medium":"/plugins/web/resources/img/site/config/save_32.png","icon-large":"/plugins/web/resources/img/site/config/save_48.png"}, {id: "org.ametys.web.admin.site.SaveConfig", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-users","label":"Utilisateurs","description":"Gestion des utilisateurs de l'application<br/>Cet outil vous permet de créer, modifier, rechercher les utilisateurs.<br/>Attention ! La recherche ne renvoie que 100 résultats.","help":"org.ametys.core.users","icon-small":"/plugins/core/resources/img/users/user_16.png","icon-medium":"/plugins/core/resources/img/users/user_32.png","icon-large":"/plugins/core/resources/img/users/user_48.png"}, {id: "org.ametys.core.Users", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.clearAllCache","label":"Vider le cache complet","description":"Vide le cache de tous les sites.<br/>Vous pouvez vider le cache d'un seul site en sélectionnant un site.","icon-small":"/plugins/web/resources/img/site/clear_all_16.png","icon-medium":"/plugins/web/resources/img/site/clear_all_32.png","icon-large":"/plugins/web/resources/img/site/clear_all_48.png"}, {id: "org.ametys.web.admin.site.ClearAllCache", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-skin","label":"Chartes graphiques","description":"Gestion des chartes graphiques","icon-small":"/plugins/web/resources/img/skin/skin_16.png","icon-medium":"/plugins/web/resources/img/skin/skin_32.png","icon-large":"/plugins/web/resources/img/skin/skin_48.png"}, {id: "org.ametys.web.skin.SkinTool", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.plugins.admin.system.SystemAnnouncementController", Ext.apply({"opentool-id":"uitool-admin-system","label":"Accès","description":"Cet outil permet de mettre en place une annonce d'accueil pour les utilisateurs.","announcement-on-description":"L'annonce système d'accueil est actuellement activée.","announcement-off-description":"L'annonce système d'accueil est actuellement désactivée.","icon-small":"/plugins/admin/resources/img/system/announcement_16.png","icon-medium":"/plugins/admin/resources/img/system/announcement_32.png","icon-large":"/plugins/admin/resources/img/system/announcement_48.png","announcement-on-icon-small":"/plugins/admin/resources/img/system/announcement_on_16.png","announcement-on-icon-medium":"/plugins/admin/resources/img/system/announcement_on_32.png","announcement-on-icon-large":"/plugins/admin/resources/img/system/announcement_on_48.png","announcement-off-icon-small":"/plugins/admin/resources/img/system/announcement_off_16.png","announcement-off-icon-medium":"/plugins/admin/resources/img/system/announcement_off_32.png","announcement-off-icon-large":"/plugins/admin/resources/img/system/announcement_off_48.png","available":false}, {id: "org.ametys.plugins.admin.System", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"label":"Importer un modèle","description":"Importe un modèle à partir d'un fichier ZIP.","icon-small":"/plugins/skinfactory/resources/img/model/import_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/import_32.png","icon-large":"/plugins/skinfactory/resources/img/model/import_48.png","menu-items":["org.ametys.plugins.artisteer.administration.importmodel","org.ametys.skinfactory.admin.model.importmodel"]}, {id: "org.ametys.skinfactory.admin.model.import", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.artisteer.ImportActions.importModel","label":"Importer un modèle Artisteer","description":"Importer un modèle créé avec 'Artisteer pour Ametys'","icon-small":"/plugins/artisteer/resources/img/artisteer_16.png","icon-medium":"/plugins/artisteer/resources/img/artisteer_32.png","icon-large":"/plugins/artisteer/resources/img/artisteer_48.png"}, {id: "org.ametys.plugins.artisteer.administration.importmodel", pluginName: "artisteer"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.model.SkinModelActions.import","label":"Importer un modèle Ametys","description":"Importe un modèle Ametys à partir d'un fichier ZIP.","icon-small":"/plugins/skinfactory/resources/img/model/import_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/import_32.png","icon-large":"/plugins/skinfactory/resources/img/model/import_48.png"}, {id: "org.ametys.skinfactory.admin.model.importmodel", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.model.SkinModelActions.delete","selection-target-id":"^skin-model$","label":"Supprimer","description":"Supprime le modèle sélectionné","icon-small":"/plugins/skinfactory/resources/img/model/delete_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/delete_32.png","icon-large":"/plugins/skinfactory/resources/img/model/delete_48.png"}, {id: "org.ametys.skinfactory.admin.model.delete", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.plugins.admin.logs.LogLevelController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.changeLogLevel","selection-target-id":"^(log-category)$","selection-enable-multiselection":"false","level":"WARN","label":"Passer en WARNING","description":"Changer le niveau de log de la catégorie sélectionnée en \"warning\".","icon-small":"/plugins/admin/resources/img/logs/level_warn_16.png","icon-medium":"/plugins/admin/resources/img/logs/level_warn_32.png","icon-large":"/plugins/admin/resources/img/logs/level_warn_48.png"}, {id: "org.ametys.plugins.admin.logslevel.Warn", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.config.SaveConfigAction.save","tool-id":"uitool-admin-config","selection-target-id":"^configuration$","label":"Sauver et redémarrer","description":"Enregistre les valeurs du formulaire et rédemarre l'application.","icon-small":"/plugins/admin/resources/img/config/save_16.png","icon-medium":"/plugins/admin/resources/img/config/save_32.png","icon-large":"/plugins/admin/resources/img/config/save_48.png"}, {id: "org.ametys.plugins.admin.config.Save", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.skin.SkinActions.saveConfig","selection-target-id":"^(skin|skin-temp)$","selection-subtarget-id":"^(skin-configuration)$","label":"Sauvegarder","description":"Sauvegarder les modifications","icon-small":"/plugins/web/resources/img/skin/config/save_16.png","icon-medium":"/plugins/web/resources/img/skin/config/save_32.png","icon-large":"/plugins/web/resources/img/skin/config/save_48.png"}, {id: "org.ametys.web.admin.skin.SaveConfig", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-workspaces","label":"Workspaces","description":"Ouvre un outil permettant de visualiser les workspaces sous forme d'un arbre.","icon-small":"/plugins/admin/resources/img/plugins/workspaces_16.png","icon-medium":"/plugins/admin/resources/img/plugins/workspaces_32.png","icon-large":"/plugins/admin/resources/img/plugins/workspaces_48.png"}, {id: "org.ametys.plugins.admin.Workspaces", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.purge","label":"Purger les journaux","description":"Effacer les journaux datés de plus de 12 jours.","icon-small":"/plugins/admin/resources/img/logs/clean_16.png","icon-medium":"/plugins/admin/resources/img/logs/clean_32.png","icon-large":"/plugins/admin/resources/img/logs/clean_48.png"}, {id: "org.ametys.plugins.admin.logs.Purge", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-jvmstatus","label":"Général","description":"Cet outil vous permet de consulter l'état général du système et de la JVM","icon-small":"/plugins/admin/resources/img/jvmstatus/jvmstatus_16.png","icon-medium":"/plugins/admin/resources/img/jvmstatus/jvmstatus_32.png","icon-large":"/plugins/admin/resources/img/jvmstatus/jvmstatus_48.png"}, {id: "org.ametys.plugins.admin.JVMStatus", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.system.SystemActions.deleteMessage","selection-target-id":"^system-announcement-message$","selection-target-parameter":{"name":"^language$","value":"!\\*"},"label":"Supprimer","description":"Supprimer l'annonce sélectionnée.","icon-small":"/plugins/admin/resources/img/system/delete_announcement_16.png","icon-medium":"/plugins/admin/resources/img/system/delete_announcement_32.png","icon-large":"/plugins/admin/resources/img/system/delete_announcement_48.png"}, {id: "org.ametys.plugins.admin.system.Delete", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.plugins.coreui.configurableformpanel.TestsController", Ext.apply({"action":"Ametys.plugins.coreui.configurableformpanel.TestsController.check","mode":"all","selection-target-id":"^form$","label":"Tester tous","description":"Lancer tous les tests, quel que soit leur statut actuel.","icon-small":"/plugins/core-ui/resources/img/Ametys/common/form/configurable/tests/testall_16.png","icon-medium":"/plugins/core-ui/resources/img/Ametys/common/form/configurable/tests/testall_32.png","icon-large":"/plugins/core-ui/resources/img/Ametys/common/form/configurable/tests/testall_48.png"}, {id: "org.ametys.plugins.core.configurableformpanel.CheckAll", pluginName: "core-ui"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*//*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.model.SkinModelActions.applyAll","selection-target-id":"^skin-model$","label":"Réappliquer aux chartes","description":"Réapplique le modèle sélectionné à toutes les chartes utilisant ce modèle","icon-small":"/plugins/skinfactory/resources/img/model/apply_all_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/apply_all_32.png","icon-large":"/plugins/skinfactory/resources/img/model/apply_all_48.png"}, {id: "org.ametys.skinfactory.admin.model.applyall", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-logslevel","label":"Configuration","description":"Cet outil permet de configurer les niveaux de logs catégorie par catégorie.<br/>Pour modifier de manière permanente le niveau de logs, configurez le fichier WEB-INF\\\\log4j.xml.","icon-small":"/plugins/admin/resources/img/logs/loglevel_16.png","icon-medium":"/plugins/admin/resources/img/logs/loglevel_32.png","icon-large":"/plugins/admin/resources/img/logs/loglevel_48.png"}, {id: "org.ametys.plugins.admin.LogsLevel", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-monitoring","label":"Monitoring","description":"Cet outil vous permet de visualiser les courbes de suivi de l'état du système.","icon-small":"/plugins/admin/resources/img/jvmstatus/monitoring_16.png","icon-medium":"/plugins/admin/resources/img/jvmstatus/monitoring_32.png","icon-large":"/plugins/admin/resources/img/jvmstatus/monitoring_48.png"}, {id: "org.ametys.plugins.admin.Monitoring", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-admin-global-statistics","label":"Statistiques globales","description":"Voir les statistiques sur l'ensemble des sites","icon-small":"/plugins/web/resources/img/site/statistics_16.png","icon-medium":"/plugins/web/resources/img/site/statistics_32.png","icon-large":"/plugins/web/resources/img/site/statistics_48.png"}, {id: "org.ametys.web.admin.site.GlobalStatistics", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"label":"Importer une charte","description":"Importer une charte à partir d'un fichier zip","icon-small":"/plugins/web/resources/img/administrator/skins/import_16.png","icon-medium":"/plugins/web/resources/img/administrator/skins/import_32.png","icon-large":"/plugins/web/resources/img/administrator/skins/import_48.png","menu-items":["org.ametys.plugins.artisteer.administration.importskin","org.ametys.web.admin.skin.importskin"]}, {id: "org.ametys.web.admin.skin.import", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.artisteer.ImportActions.importSkin","label":"Importer une charte Artisteer","description":"Importer une charte graphique créée avec 'Artisteer pour Ametys'","icon-small":"/plugins/artisteer/resources/img/artisteer_16.png","icon-medium":"/plugins/artisteer/resources/img/artisteer_32.png","icon-large":"/plugins/artisteer/resources/img/artisteer_48.png"}, {id: "org.ametys.plugins.artisteer.administration.importskin", pluginName: "artisteer"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.skin.SkinActions.import","label":"Importer une charte Ametys","description":"Importer une charte Ametys à partir d'un fichier zip","icon-small":"/plugins/web/resources/img/administrator/skins/import_16.png","icon-medium":"/plugins/web/resources/img/administrator/skins/import_32.png","icon-large":"/plugins/web/resources/img/administrator/skins/import_48.png"}, {id: "org.ametys.web.admin.skin.importskin", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.unsaveConfig","selection-target-id":"^(site)$","selection-subtarget-id":"^(site-configuration)$","label":"Fermer sans sauver","description":"Annule les modifications","icon-small":"/plugins/web/resources/img/site/config/unsave_16.png","icon-medium":"/plugins/web/resources/img/site/config/unsave_32.png","icon-large":"/plugins/web/resources/img/site/config/unsave_48.png"}, {id: "org.ametys.web.admin.site.UnsaveConfig", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*//*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.coreui.profiles.ProfilesTool.save","disabled":"true","selection-target-id":"^profile$","selection-subtarget-id":"^form$","selection-subtarget-parameter":{"name":"isDirty","value":"true"},"label":"Sauvegarder","description":"Sauvegarder les modifications effectuées sur ce profil.","icon-small":"/plugins/core/resources/img/profiles/save_16.png","icon-medium":"/plugins/core/resources/img/profiles/save_32.png","icon-large":"/plugins/core/resources/img/profiles/save_48.png"}, {id: "org.ametys.core.profiles.SaveRights", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.coreui.profiles.ProfilesTool.discardChanges","disabled":"true","selection-target-id":"^profile$","selection-subtarget-id":"^form$","selection-subtarget-parameter":{"name":"isDirty","value":"true"},"label":"Annuler","description":"Annuler les modifications en cours du profil.","icon-small":"/plugins/core/resources/img/profiles/unsave_16.png","icon-medium":"/plugins/core/resources/img/profiles/unsave_32.png","icon-large":"/plugins/core/resources/img/profiles/unsave_48.png"}, {id: "org.ametys.core.profiles.Discard", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*//*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.remove","selection-target-id":"^(site)$","label":"Supprimer","description":"Supprime le site sélectionné","icon-small":"/plugins/web/resources/img/site/delete_site_16.png","icon-medium":"/plugins/web/resources/img/site/delete_site_32.png","icon-large":"/plugins/web/resources/img/site/delete_site_48.png"}, {id: "org.ametys.web.admin.site.Delete", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.statistics","selection-target-id":"^(site)$","label":"Statistiques du site","description":"Cet outil permet de visualiser les statistiques du site","icon-small":"/plugins/web/resources/img/site/statistics_16.png","icon-medium":"/plugins/web/resources/img/site/statistics_32.png","icon-large":"/plugins/web/resources/img/site/statistics_48.png"}, {id: "org.ametys.web.admin.site.Statistics", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*//*
   control = Ext.create("Ametys.ribbon.element.ui.button.OpenToolButtonController", Ext.apply({"opentool-id":"uitool-profiles","label":"Profils de droits","description":"Gestion des profils de droits.<br/>Un profil est un ensemble de droits. Chaque utilisateur possède un profil qui permet de connaître la liste de ses droits.","help":"org.ametys.core.rights.profiles","icon-small":"/plugins/core/resources/img/profiles/profile_16.gif","icon-medium":"/plugins/core/resources/img/profiles/profile_32.gif","icon-large":"/plugins/core/resources/img/profiles/profile_50.gif"}, {id: "org.ametys.core.Profiles", pluginName: "core"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.plugins.admin.logs.LogLevelController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.changeLogLevel","selection-target-id":"^(log-category)$","selection-enable-multiselection":"false","level":"ERROR","label":"Passer en ERROR","description":"Changer le niveau de log de la catégorie sélectionnée en \"error\".","icon-small":"/plugins/admin/resources/img/logs/level_error_16.png","icon-medium":"/plugins/admin/resources/img/logs/level_error_32.png","icon-large":"/plugins/admin/resources/img/logs/level_error_48.png"}, {id: "org.ametys.plugins.admin.logslevel.Error", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
/*
   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.configure","selection-target-id":"^(site)$","label":"Configurer","description":"Modifie la configuration du site.<br/><br/>Naviguez en utilisant les onglets et modifiez les valeurs des paramètres.","icon-small":"/plugins/web/resources/img/uitool-site/configure_site_16.png","icon-medium":"/plugins/web/resources/img/uitool-site/configure_site_32.png","icon-large":"/plugins/web/resources/img/uitool-site/configure_site_48.png"}, {id: "org.ametys.web.admin.site.Configure", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.skin.SkinActions.delete","selection-target-id":"^skin$","label":"Supprimer","description":"Supprimer la charte sélectionnée","icon-small":"/plugins/web/resources/img/administrator/skins/delete_16.png","icon-medium":"/plugins/web/resources/img/administrator/skins/delete_32.png","icon-large":"/plugins/web/resources/img/administrator/skins/delete_48.png"}, {id: "org.ametys.web.admin.skin.delete", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.skinfactory.model.SkinModelActions.export","selection-target-id":"^skin-model$","label":"Exporter","description":"Exporte le modèle sélectionné au format ZIP","icon-small":"/plugins/skinfactory/resources/img/model/export_16.png","icon-medium":"/plugins/skinfactory/resources/img/model/export_32.png","icon-large":"/plugins/skinfactory/resources/img/model/export_48.png"}, {id: "org.ametys.skinfactory.admin.model.export", pluginName: "skinfactory"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.web.site.SiteActions.buildLive","selection-target-id":"^(site)$","label":"Reconstruire le live du site","description":"Reconstruit le workspace live du site sélectionné (le site ne sera plus accessible le temps de cette reconstruction).","icon-small":"/plugins/web/resources/img/site/build_live_16.png","icon-medium":"/plugins/web/resources/img/site/build_live_32.png","icon-large":"/plugins/web/resources/img/site/build_live_48.png"}, {id: "org.ametys.web.admin.site.BuildLive", pluginName: "web"}));
   Ametys.ribbon.RibbonManager.registerUI(control);
*/
   control = Ext.create("Ametys.plugins.admin.logs.LogLevelController", Ext.apply({"action":"Ametys.plugins.admin.logs.LogsActions.changeLogLevel","selection-target-id":"^(log-category)$","selection-enable-multiselection":"false","level":"INHERIT","label":"Hériter le mode","description":"Hériter le mode de la catégorie de log parente.","icon-small":"/plugins/admin/resources/img/logs/inherit_16.png","icon-medium":"/plugins/admin/resources/img/logs/inherit_32.png","icon-large":"/plugins/admin/resources/img/logs/inherit_48.png"}, {id: "org.ametys.plugins.admin.logslevel.Inherit", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

   control = Ext.create("Ametys.ribbon.element.ui.ButtonController", Ext.apply({"action":"Ametys.plugins.admin.plugins.PluginsActions.activate","selection-target-id":"^(plugin-by-file-node|plugin-by-extension-point-node)$","selection-target-parameter":{"name":"^inactiveFeature$","value":"true"},"label":"Activer","description":"Permet d'activer la feature sélectionnée.","icon-small":"/plugins/admin/resources/img/plugins/activate_16.png","icon-medium":"/plugins/admin/resources/img/plugins/activate_32.png","icon-large":"/plugins/admin/resources/img/plugins/activate_48.png"}, {id: "org.ametys.plugins.admin.plugins.Activate", pluginName: "admin"}));
   Ametys.ribbon.RibbonManager.registerUI(control);

/*
 * RIBBON
 */
/** Tab 1 */
var tab_1 = Ext.create("Ametys.ui.fluent.ribbon.Panel", {title: "Administration",items: []});

var fgp_1_1_small = [];
var fgp_1_1_medium = [];
var fgp_1_1_large = [];
fgp_1_1_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.config.Open").addUI("large"));

    var fgp_1_1 = {title: 'Configuration',
        priority: 0,
        smallItems: fgp_1_1_small,
        items: fgp_1_1_medium,
        largeItems: fgp_1_1_large
    };

    // Dialog box launcher
    
    
    tab_1.add(fgp_1_1);

var fgp_1_2_small = [];
var fgp_1_2_medium = [];
var fgp_1_2_large = [];
fgp_1_2_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.System").addUI("large"));
fgp_1_2_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.JVMStatus").addUI("large"));
fgp_1_2_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.SystemProperties").addUI("large"));
fgp_1_2_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.Monitoring").addUI("large"));

    var fgp_1_2 = {title: 'Système',
        priority: 0,
        smallItems: fgp_1_2_small,
        items: fgp_1_2_medium,
        largeItems: fgp_1_2_large
    };

    // Dialog box launcher
    
    
    tab_1.add(fgp_1_2);

var fgp_1_3_small = [];
var fgp_1_3_medium = [];
var fgp_1_3_large = [];
fgp_1_3_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.Logs").addUI("large"));
fgp_1_3_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.LogsLevel").addUI("large"));

    var fgp_1_3 = {title: 'Journaux',
        priority: 0,
        smallItems: fgp_1_3_small,
        items: fgp_1_3_medium,
        largeItems: fgp_1_3_large
    };

    // Dialog box launcher
    
    
    tab_1.add(fgp_1_3);

var fgp_1_4_small = [];
var fgp_1_4_medium = [];
var fgp_1_4_large = [];
fgp_1_4_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.PluginsByFile").addUI("large"));
fgp_1_4_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.PluginsByExtensionPoint").addUI("large"));
fgp_1_4_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.admin.Workspaces").addUI("large"));

    var fgp_1_4 = {title: 'Plugins & Workspaces',
        priority: 0,
        smallItems: fgp_1_4_small,
        items: fgp_1_4_medium,
        largeItems: fgp_1_4_large
    };

    // Dialog box launcher
    
    
    tab_1.add(fgp_1_4);
/*
var fgp_1_5_small = [];
var fgp_1_5_medium = [];
var fgp_1_5_large = [];
fgp_1_5_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.web.admin.Sites").addUI("large"));
fgp_1_5_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.plugins.repository.administrator.JCRRepository").addUI("large"));
fgp_1_5_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.web.skin.SkinTool").addUI("large"));

    var fgp_1_5 = {title: 'Application',
        priority: 0,
        smallItems: fgp_1_5_small,
        items: fgp_1_5_medium,
        largeItems: fgp_1_5_large
    };

    // Dialog box launcher
    
    
    tab_1.add(fgp_1_5);

var fgp_1_6_small = [];
var fgp_1_6_medium = [];
var fgp_1_6_large = [];
fgp_1_6_medium.push(Ametys.ribbon.RibbonManager.getUI("org.ametys.cms.content.indexing.solr.admin.ReindexAllContents").addUI("large"));

    var fgp_1_6 = {title: 'Indexation',
        priority: 0,
        smallItems: fgp_1_6_small,
        items: fgp_1_6_medium,
        largeItems: fgp_1_6_large
    };

    // Dialog box launcher
    
    
    tab_1.add(fgp_1_6);
*/
ribbonItems.push(tab_1);
