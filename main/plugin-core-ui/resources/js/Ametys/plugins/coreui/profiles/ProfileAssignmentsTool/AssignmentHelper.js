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
 * This helper provides methods to update local assignments (client-side) on records
 */
Ext.define('Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.AssignmentHelper', {
	singleton: true,
	
	/**
	 * Compute and update the local assignments of a given column (=profile)
	 * @param {Ext.data.Model[]} records The records to update.
	 * @param {String} profileId The id of profile which is the id of column
	 */
	computeAndUpdateLocalInducedAssignments: function (records, profileId, callback)
	{
		// When iterating, we need to be sure anonymous and anyconnected are the two first records (for then computing the others)
        // and we need to be sure group records come before all user records, as user records can be computed from their groups values
        
        // First, update local values for Anonymous and "any connected user" records
        var anonymousRecord = this.findAnonymousRecord(records);
        this.computeAndUpdateLocalInducedAssignmentsForAnonymous (records, anonymousRecord, anonymousRecord.get(profileId), profileId);
        
        var anyConnectedRecord = this.findAnyConnectedRecord(records);
        this.computeAndUpdateLocalInducedAssignmentsForAnyconnected (records, anyConnectedRecord, anyConnectedRecord.get(profileId), profileId);
        
        // Then, do a first iteration to update group records
        records.each(function(record) {
            var type = record.get('targetType');
            if (type == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_GROUP)
            {
            	this.computeAndUpdateLocalInducedAssignmentsForGroup(records, record, record.get(profileId), profileId);
            }
        }, this);
        
        // Finally, do a second iteration to update user records
        records.each(function(record) {
            var type = record.get('targetType');
            if (type == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_USER)
            {
            	this.computeAndUpdateLocalInducedAssignmentsForUser(records, record, record.get(profileId), profileId);
            }
        }, this);
	},
	
	/**
	 * Update the local assignment for anonymous 
	 * @param {Ext.data.Model} record The anonymous record
	 * @param {String} currentValue The current assignment value
	 * @param {String} profileId The id of concerned profile
	 * @return {Boolean} true if changes were made, false otherwise
	 */
	computeAndUpdateLocalInducedAssignmentsForAnonymous: function (records, record, currentValue, profileId)
	{
		var hasLocalAssignment = currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY;
		if (!hasLocalAssignment)
		{
			// Check the value for any connected users
			var anyconnectedRecord = this.findAnyConnectedRecord(records);
            var assignmentForAnyconnected = anyconnectedRecord.get(profileId);
			
            if (assignmentForAnyconnected == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY)
            {
            	// If there is no local assignment and any connected user is denied, so Anonymous will be denied
                record.set(profileId, Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_ANYCONNECTED, {dirty: false}); 
                return true;
            }
		}
		
		return false;
	},
	
	/**
	 * Update the local assignment for any connected user 
	 * @param {Ext.data.Model} record The any connected user record
	 * @param {String} currentValue The current assignment value
	 * @param {String} profileId The id of concerned profile
	 * @return {Boolean} true if changes were made, false otherwise
	 */
	computeAndUpdateLocalInducedAssignmentsForAnyconnected: function (records, record, currentValue, profileId)
	{
		var hasLocalAssignment = currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY;
		if (!hasLocalAssignment)
		{
			// Check the value for anonymous
			var anonymousRecord = this.findAnonymousRecord(records);
            var assignmentForAnonymous = anonymousRecord.get(profileId);
            
            if (assignmentForAnonymous == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || assignmentForAnonymous == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW)
            {
            	// If there is no local assignment and anonymous user is allowed, so any connected user will be allowed
            	record.set(profileId, Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANONYMOUS, {dirty: false}); 
            }
		}
	},
	
	/**
	 * Update the local assignment for a group
	 * @param {Ext.data.Model} record The group record
	 * @param {String} currentValue The current assignment value
	 * @param {String} profileId The id of concerned profile
	 * @return {Boolean} true if changes were made, false otherwise
	 */
	computeAndUpdateLocalInducedAssignmentsForGroup: function (records, record, currentValue, profileId)
	{
		var hasLocalAssignment = currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY;
		if (!hasLocalAssignment)
		{
			// Get the current assignment for anonymous
        	var anonymousRecord = this.findAnonymousRecord(records);
            var assignmentForAnonymous = anonymousRecord.get(profileId);
            
            // Get the current access type for any connected users
        	var anyconnectedRecord = this.findAnyConnectedRecord(records);
        	var assignmentForAnyconnected = anyconnectedRecord.get(profileId);
        	
            if (assignmentForAnonymous == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || assignmentForAnonymous == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW)
            {
            	// If Anonymous is allowed (by inheritance or not), the group is allowed
                record.set(profileId, Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANONYMOUS, {dirty: false}); 
                return true;
            }
            else if (assignmentForAnyconnected == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY || assignmentForAnyconnected == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_DENY)
            {
            	// If any connected user is denied (by inheritance or not), the group is denied
            	record.set(profileId, Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_ANYCONNECTED, {dirty: false}); 
            	return true;
            }
            else if (assignmentForAnyconnected == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || assignmentForAnyconnected == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW)
            {
            	// If any connected user is allowed (by inheritance or not), the group is allowed
            	record.set(profileId, Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_ANYCONNECTED, {dirty: false}); 
            	return true;
            }
		}
		
		return false;
	},
	
	/**
	 * Update the local assignment for a group
	 * @param {Ext.data.Model} record The user record
	 * @param {String} currentValue The current assignment value
	 * @param {String} profileId The id of concerned profile
	 * @return {Boolean} true if changes were made, false otherwise
	 */
	computeAndUpdateLocalInducedAssignmentsForUser: function (records, record, currentValue, profileId)
	{
		var hasLocalAssignment = currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || currentValue == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY;
		if (!hasLocalAssignment)
		{
			// First check the access given by the user's groups
			var isAllowedByGroup = false;
			var groups = record.get('groups');
			
			for (var i=0; i < groups.length; i++)
			{
				var group = groups[i];
				var groupRecord = this.findGroupRecord(records, group.groupId, group.groupDirectory);
				
				if (groupRecord != null)
				{
					var assignmentForGroup = groupRecord.get(profileId);
					if (assignmentForGroup == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY || assignmentForGroup == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_DENY)
					{
						// If at least one user's group is denied, so the user is denied
						record.set(profileId, Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_DENY_BY_GROUP, {dirty: false}); 
		            	return true;
					}
					else if (assignmentForGroup == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW || assignmentForGroup == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_INHERITED_ALLOW)
					{
						isAllowedByGroup = true;
					}
				}
			}
			
			// If at least one user's group is allowed, the user is allowed
			if (isAllowedByGroup)
			{
				record.set(profileId, Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.ACCESS_TYPE_ALLOW_BY_GROUP, {dirty: false}); 
            	return true;
			}
			
			// If no user's group has allowed to determine access, check anonymous and any connected user access as for a group 
			return this.computeAndUpdateLocalInducedAssignmentsForGroup(records, record, currentValue, profileId);
		}
		return false;
	},
	
	/**
     * Find the Anonynous record
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} records The records to search into.
     * @return {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} the Anonymous record or null if not found
     */
    findAnonymousRecord: function(records)
    {
    	var anonymousRecord = null;
        records.each(function(record) {
            if (record.get('targetType') == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANONYMOUS)
            {
            	anonymousRecord = record; // stop iteration
            	return false;
            }
        }, this);
        
        return anonymousRecord;
    },
    
    /**
     * Find the "any connected user" record
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} records The records to search into.
     * @return {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} the "any connected user" record or null if not found
     */
    findAnyConnectedRecord: function (records)
    {
    	var anyconnectedRecord = null;
        records.each(function(record) {
            if (record.get('targetType') == Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.TARGET_TYPE_ANYCONNECTEDUSER)
            {
            	anyconnectedRecord = record;
            	return false; // stop iteration
            }
        }, this);
        
        return anyconnectedRecord;
    },
    
    /**
     * @private
     * Find a group record
     * @param {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} records The records to search into
     * @param {String} groupId the id of the group
     * @param {String} groupDirectory the id of the group directory
     * @return {Ametys.plugins.coreui.profiles.ProfileAssignmentsTool.Entry} the group record or null if not found
     */
    findGroupRecord: function(records, groupId, groupDirectory)
    {
    	var groupRecord = null;
    	records.each(function(record) {
            if (record.get('groupId') == groupId && record.get('groupDirectory') == groupDirectory)
            {
            	groupRecord = record;
            	return false; // stop iteration
            }
        }, this);
    	
        return groupRecord;
    }

});
