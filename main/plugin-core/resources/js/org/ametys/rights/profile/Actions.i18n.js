/*
 *  Copyright 2010 Anyware Services
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

Ext.namespace('org.ametys.rights.profile');

/**
 * Affect a profile to users 
 * @param profileId The profile id
 * @param users The users' logins in a Array
 * @param context The context right
 * @param callback The callback function
 * @param additionalParams {Object} The optional additional parameters. Can be null
 */
org.ametys.rights.profile.AddUsers = function (profileId, users, context, callback, additionalParams)
{
	var params = {'profiles': profileId, 'users': users};
	params.context = context;
	
	if (additionalParams != null)
	{
		for (var i in additionalParams)
		{
			params[i] = additionalParams[i];
		}
	}
	
	var serverMessage = new org.ametys.servercomm.ServerMessage('core', '/rights/assign', params, org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.rights.profile.AddUsers._callback, this, [profileId, callback]);
	org.ametys.servercomm.ServerComm.getInstance().send(serverMessage); 
}

org.ametys.rights.profile.AddUsers._callback = function (response, args)
{
	if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ASSIGNMENT_ADDUSER_ERROR"/>", response, "org.ametys.rights.profile.Remove"))
	{
		return;
	}
	
	if (typeof args[1] == 'function')
	{
		args[1] (args[0]);
	}
}


/**
 * Affect a profile to groups 
 * @param profileId The profile id
 * @param groups The groups' ids in a Array
 * @param context The context right
 * @param callback The callback function
 * @param additionalParams {Object} The optional additional parameters. Can be null
 */
org.ametys.rights.profile.AddGroups = function (profileId, groups, context, callback, additionalParams)
{
	var params = {'profiles': profileId, 'groups': groups};
	params.context = context;
	
	if (additionalParams != null)
	{
		for (var i in additionalParams)
		{
			params[i] = additionalParams[i];
		}
	}
	
	var serverMessage = new org.ametys.servercomm.ServerMessage('core', '/rights/assign', params, org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.rights.profile.AddGroups._callBack, this, [profileId, callback]);
	org.ametys.servercomm.ServerComm.getInstance().send(serverMessage); 
}

org.ametys.rights.profile.AddGroups._callBack = function (response, args)
{
	if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ASSIGNMENT_ADDGROUP_ERROR"/>", response, "org.ametys.rights.profile.Remove"))
	{
		return;
	}
	
	if (typeof args[1] == 'function')
	{
		args[1] (args[0]);
	}
}

/**
 * Remove assignment
 * @param profileId The profile id
 * @param context The context right
 * @param users The users' ids to remove
 * @param groups The groups' ids to remove
 * @param callback The callback function
 * @param additionalParams {Object} The optional additional parameters. Can be null
 */
org.ametys.rights.profile.Remove = function (profileId, context, users, groups, callback, additionalParams) 
{
	Ext.Msg.confirm("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ASSIGNMENT_REMOVE_LABEL"/>", 
			"<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ASSIGNMENT_REMOVE_CONFIRM"/>", 
			function (btn) {org.ametys.rights.profile.Remove._doIt (btn, profileId, context, users, groups, callback, additionalParams)}, 
			this);
}

org.ametys.rights.profile.Remove._doIt = function (btn, profileId, context, users, groups, callback, additionalParams) 
{
	if (btn == 'yes')
	{
		var params = {'users': users, 'groups': groups};
		params.profileId = profileId;
		params.context = context;
		
		if (additionalParams != null)
		{
			for (var i in additionalParams)
			{
				params[i] = additionalParams[i];
			}
		}
		
		var serverMessage = new org.ametys.servercomm.ServerMessage('core', '/rights/remove', params, org.ametys.servercomm.ServerComm.PRIORITY_MAJOR, org.ametys.rights.profile.Remove._callBack, this, [profileId, callback]);
		org.ametys.servercomm.ServerComm.getInstance().send(serverMessage);
	}
}

org.ametys.rights.profile.Remove._callBack = function (response, args)
{
	if (org.ametys.servercomm.ServerComm.handleBadResponse("<i18n:text i18n:key="PLUGINS_CORE_RIGHTS_ASSIGNMENT_REMOVE_ERROR"/>", response, "org.ametys.rights.profile.Remove"))
	{
		return;
	}
	
	if (typeof args[1] == 'function')
	{
		args[1] (args[0]);
	}
}

