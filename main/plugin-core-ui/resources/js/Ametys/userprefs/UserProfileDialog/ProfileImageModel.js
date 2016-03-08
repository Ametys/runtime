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
 * This class is the model for profiles image entry. Used in the
 * {@link Ametys.userprefs.UserProfileDialog} to allow the user to choose
 * between different images.
 */
Ext.define('Ametys.userprefs.UserProfileDialog.ProfileImageModel', {
    extend: 'Ext.data.Model',
    
    fields: [
        {name: 'source', mapping: 'source', type: 'string'},
        {name: 'parameters', mapping: 'parameters'},
        {name: 'viewUrl', type: 'string', calculate: function(data)
            {
                if (data.source == 'upload-image')
                {
                    // special case, the "upload an image" button must be displayed
                    return Ametys.getPluginResourcesPrefix('core-ui') + '/img/user-profiles/upload-image.png';
                }
                
                var addedParams = {};
                
                // No cache on the userpref image to force to ensure updated preview
                if (data.source == 'userpref')
                {
                    addedParams.nocache = (new Date()).getTime().toString();
                }
                
                var qs = Ext.Object.toQueryString(Ext.merge(addedParams, data.parameters || {}));
                    
                return Ametys.getPluginDirectPrefix('core-ui') + '/current-user/source/' + data.source + '/image_64' + (qs ? ('?' + qs) : '');
            }
        },
        {name: 'description', type: 'string', calculate: function(data)
            {
                if (data.source == 'upload-image')
                {
                    return "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_UPLOADIMAGE}}";
                }
                
                var desc = "<u>{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_PREFIX}}</u> : ";
                
                switch (data.source)
                {
                    case 'localimage':
                        desc += "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_LOCALIMAGE}}";
                        break;
                    case 'gravatar':
                        desc += "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_GRAVATAR}}";
                        break;
                    case 'usersmanager':
                        desc += "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_USERSMANAGER}}";
                        break;
                    case 'initials':
                        desc += "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_INITIALS}}";
                        break;
                    case 'upload':
                        desc += "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_UPLOAD}}";
                        break;
                    case 'userpref':
                        desc += "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_USERPREF}}";
                        break;
                    case 'default':
                        desc += "{{i18n PLUGINS_CORE_UI_USER_PREFERENCES_PROFILE_IMAGE_FILE_DESC_DEFAULT}}";
                        break;
                }
                
                return desc;
            }
        }
    ]
});
