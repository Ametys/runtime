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
package org.ametys.runtime.plugins.core.administrator.logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

import org.ametys.runtime.cocoon.JSonReader;

/**
 * Get the log levels' 
 */
@SuppressWarnings("unchecked")
public class GetLogsLevel extends ServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> result = new HashMap<> ();
        
        try
        {
            LoggerRepository loggerRepository = LogManager.getLoggerRepository();
            List<Logger> loggers = new ArrayList<Logger>();
            Enumeration<org.apache.log4j.Logger> enumLogger = loggerRepository.getCurrentLoggers();
            
            while (enumLogger.hasMoreElements())
            {
                loggers.add(enumLogger.nextElement());
            }
            
            loggers.add(loggerRepository.getRootLogger());
            
            Collections.sort(loggers, new LoggerComparator());
            
            Map<String, Object> logCategories = new HashMap<>();
            Set<Map<String, String>> categoryLevels = new HashSet<Map<String, String>> ();
            
            for (Logger logger : loggers)
            {
                Level level = logger.getLevel();

                String category = logger.getName();
                if (category.equals("root"))
                {
                    logCategories.put("children", new ArrayList<> ());
                    logCategories.put("name", "root");
                    logCategories.put("fullname", "root");
                    logCategories.put("level", level.toString());
                }
                else
                {
                    Map<String, String> categoryLevel = new HashMap<>();
                    categoryLevel.put("category", category);
                    categoryLevel.put("level", level == null ? "inherit" : level.toString());
                    categoryLevels.add(categoryLevel);
                }
            }

            String fullName = null;
            for (Map<String, String> categoryLevel : categoryLevels)
            {
                fullName = categoryLevel.get("category");
                createCategory((List<Map<String, Object>>) logCategories.get("children"), fullName, categoryLevel.get("level"), fullName);
            }
            
            result.put("children", logCategories);
        }
        catch (Exception e) 
        {
             // nothing to do
            getLogger().warn("Unable to access internal logger properties", e);
        }
        
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        return EMPTY_MAP;
    }
    
    /**
     * Creates a log category 
     * @param children the list of categories already created
     * @param categoryName the name of the category to create
     * @param level the level of the category to create
     * @param fullName the full name of the category
     */
    public void createCategory(List<Map<String, Object>> children, String categoryName, String level, String fullName)
    {
        int i = categoryName.indexOf(".");
        if (i != -1)
        {
            // Creates intermediary tree nodes
            String parentCategoryName = categoryName.substring(0, i);
            String childCategoryName = categoryName.substring(i + 1);
            
            Map<String, Object> parentCategoryNode = _getNode(children, parentCategoryName);
            
            if (parentCategoryNode == null)
            {
                // We did not find any existing node, let's create it
                parentCategoryNode = new HashMap<> ();
                parentCategoryNode.put("children", new ArrayList<> ());
                parentCategoryNode.put("level", "inherit");
                parentCategoryNode.put("name", parentCategoryName);
                parentCategoryNode.put("fullname", fullName.substring(0, fullName.indexOf(parentCategoryName)) + parentCategoryName);
                
                children.add(parentCategoryNode);
            }
            
            createCategory((List<Map<String, Object>>) parentCategoryNode.get("children"), childCategoryName, level, fullName);
        }
        else
        {
            Map<String, Object> categoryNode = _getNode(children, categoryName);
            if (categoryNode == null)
            {
                // Creates tree leaves
                categoryNode = new HashMap<> ();
                
                categoryNode.put("children", new ArrayList<> ());
                categoryNode.put("level", level);
                categoryNode.put("name", categoryName);
                categoryNode.put("fullname", fullName);
    
                children.add(categoryNode);
            }
            else
            {
                // The intermediary node was already created, let's update it
                categoryNode.put("level", level);
            }
        }
    }
    
    private Map<String, Object> _getNode(List<Map<String, Object>> children, String categoryName)
    {
        for (Map<String, Object> child : children)
        {
            if (StringUtils.equals((String) child.get("name"), categoryName))
            {
                return child;
            }
        }
        return null;
    }
}
