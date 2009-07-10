package org.ametys.runtime.plugins.core.group.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;

/**
 * Add or remove users from an existing group
 *
 */
public class UpdateUsersGroupAction extends CurrentUserProviderServiceableAction
{
    private GroupsManager _groupsManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _groupsManager = (GroupsManager) smanager.lookup(GroupsManager.ROLE);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String groupId = request.getParameter("id");
        String[] usersList = request.getParameterValues("objects");
        boolean add = parameters.getParameterAsBoolean("add", true);
        
        if (!(_groupsManager instanceof ModifiableGroupsManager))
        {
            throw new IllegalArgumentException("The group manager used is not modifiable");
        }
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is modifying the group '" + groupId + "'.";
            if (_isSuperUser())
            {
                userMessage = "Administrator";
            }
            else
            {
                String currentUserLogin = _getCurrentUser();
                userMessage = "User '" + currentUserLogin + "'";
            }
            
            getLogger().info(userMessage + " " + endMessage);
        }
        
        ModifiableGroupsManager mgm = (ModifiableGroupsManager) _groupsManager;
        Group ug = mgm.getGroup(groupId);
        if (ug == null)
        {
            if (ug == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    String userMessage = null;
                    String endMessage = "is modifying a group '" + groupId + "' but the group does not exists.";
                    if (_isSuperUser())
                    {
                        userMessage = "Administrator";
                    }
                    else
                    {
                        String currentUserLogin = _getCurrentUser();
                        userMessage = "User '" + currentUserLogin + "'";
                    }
                    
                    getLogger().warn(userMessage + " " + endMessage);
                }
                
                Map<String, String> result = new HashMap<String, String>();
                result.put("message", "missing");
                return result;
            }
        }
        else
        {
            for (int i = 0; usersList != null && i < usersList.length; i++)
            {
                if (add)
                {
                    ug.addUser(usersList[i]);
                }
                else
                {
                    ug.removeUser(usersList[i]);
                }
            }
        }
        mgm.update(ug);
        
        return EMPTY_MAP;
    }
}
