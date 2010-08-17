/*
 *  Copyright 2009 Anyware Services
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

Ext.namespace('org.ametys.administration.SuperUser');

org.ametys.administration.SuperUser = function ()
{
}

org.ametys.administration.SuperUser.act = function (pluginName, params)
{
	org.ametys.administration.SuperUser.pluginName = pluginName;

	RUNTIME_Plugin_Runtime_SelectUser.initialize('core');
	RUNTIME_Plugin_Runtime_SelectUser.act(org.ametys.administration.SuperUser._affectSuperUser);	
}

org.ametys.administration.SuperUser._affectSuperUser = function (users) 
{
	var logins = [];
	for (var i in users)
	{
		logins.push(i);
	}
	
	var serverMessage = new org.ametys.servercomm.ServerMessage(org.ametys.administration.SuperUser.pluginName, "/administrator/superuser/affect", { login: logins }, org.ametys.servercomm.ServerComm.PRIORITY_SYNCHRONOUS, null, this, null);
	var response = org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);

	if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SUPERUSER_AFFECT_ERROR"/>", response, "org.ametys.administration.SuperUser._affectSuper"))
    {
       return;
    }
	
	Ext.Msg.show ({
		title: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SUPERUSER_AFFECT_TITLE"/>",
		msg: "<i18n:text i18n:key="PLUGINS_CORE_ADMINISTRATOR_SUPERUSER_AFFECT_SUCCESS"/>",
		buttons: Ext.Msg.OK,
		icon: Ext.MessageBox.INFO
	});
}

