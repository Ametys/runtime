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
package org.ametys.plugins.core.impl.group.directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ametys.core.group.Group;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.GroupDirectoryModel;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * Empty implementation of {@link GroupDirectory}.<br>
 * Used when no groups of users is needed.
 */
public class EmptyGroupDirectory implements GroupDirectory
{
    /** The id */
    protected String _id;
    /** The label */
    protected I18nizableText _label;
    /** The id of the {@link GroupDirectoryModel} */
    private String _groupDirectoryModelId;
    /** The map of the values of the parameters */
    private Map<String, Object> _paramValues;
    
    @Override
    public String getId()
    {
        return _id;
    }
    
    @Override
    public I18nizableText getLabel()
    {
        return _label;
    }
    
    @Override
    public void setId(String id)
    {
        _id = id;
    }
    
    @Override
    public void setLabel(I18nizableText label)
    {
        _label = label;
    }
    
    @Override
    public String getGroupDirectoryModelId()
    {
        return _groupDirectoryModelId;
    }

    @Override
    public Map<String, Object> getParameterValues()
    {
        return _paramValues;
    }

    @Override
    public void init(String groupDirectoryModelId, Map<String, Object> paramValues)
    {
        _groupDirectoryModelId = groupDirectoryModelId;
        _paramValues = paramValues;
    }
    
    @Override
    public Group getGroup(String groupID)
    {
        return null;
    }

    @Override
    public Set<Group> getGroups()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getUserGroups(String login, String populationId)
    {
        return Collections.emptySet();
    }

    @Override
    public List<Map<String, Object>> groups2JSON(int count, int offset, Map parameters)
    {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> group2JSON(String id)
    {
        return null;
    }

}
