/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.runtime.plugins.admin.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.core.util.I18nUtils;
import org.ametys.core.util.I18nizableTextComparator;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.config.ConfigParameter;
import org.ametys.runtime.config.ParameterCategory;
import org.ametys.runtime.config.ParameterGroup;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterHelper;


/**
 * SAX the configuration model with current values of configuration
 */
public class ConfigGenerator extends AbstractGenerator implements Serviceable
{
    private I18nUtils _i18nUtils;
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "config");
        
        ConfigManager configManager = ConfigManager.getInstance();

        XMLUtils.startElement(contentHandler, "configuration");
        Map<I18nizableText, ParameterCategory> categories = _sortCategories(configManager.getCategories());
        _saxConfiguration(categories, configManager.getParameterCheckers());
        XMLUtils.endElement(contentHandler, "configuration");
        
        XMLUtils.startElement(contentHandler, "configuration-values");
        _saxValues(configManager.getValues());
        XMLUtils.endElement(contentHandler, "configuration-values");
        
        XMLUtils.endElement(contentHandler, "config");
        contentHandler.endDocument();
    }
    
    private void _saxValues(Map<String, Object> values) throws SAXException
    {
        XMLUtils.startElement(contentHandler, "values");
        
        for (String parameterId : values.keySet())
        {
            Object value = values.get(parameterId);
            
            if (value != null)
            {
                XMLUtils.createElement(contentHandler, parameterId, ParameterHelper.valueToString(value)); 
            }
        }
        
        XMLUtils.endElement(contentHandler, "values");
    }
    
    private  void _saxConfiguration(Map<I18nizableText, ParameterCategory> categories, Map<String, ParameterCheckerDescriptor> checkers) throws SAXException, ProcessingException
    {
        for (I18nizableText categoryKey : categories.keySet())
        {
            AttributesImpl tabAttrs = new AttributesImpl();
            tabAttrs.addCDATAAttribute("role", "tabs");
            XMLUtils.startElement(contentHandler, "fieldsets", tabAttrs);
            
            categoryKey.toSAX(contentHandler, "label");
            
            ParameterCategory category = categories.get(categoryKey);
            
            // Category checkers
            Set<ParameterCheckerDescriptor> paramCheckersCategories = category.getParamCheckers();
            if (paramCheckersCategories != null)
            {
                for (ParameterCheckerDescriptor paramChecker : paramCheckersCategories)
                {
                    paramChecker.toSAX(contentHandler);
                }
            }
            
            XMLUtils.startElement(contentHandler, "elements");

            for (I18nizableText groupKey : category.getGroups().keySet())
            {
                ParameterGroup group = category.getGroups().get(groupKey);

                AttributesImpl fieldsetAttrs = new AttributesImpl();
                fieldsetAttrs.addCDATAAttribute("role", "fieldset");
                XMLUtils.startElement(contentHandler, "fieldsets", fieldsetAttrs);

                groupKey.toSAX(contentHandler, "label");

                // Group checkers
                Set<ParameterCheckerDescriptor> paramCheckersGroups = group.getParamCheckers();
                if (paramCheckersGroups != null)
                {
                    for (ParameterCheckerDescriptor paramChecker : paramCheckersGroups)
                    {
                        paramChecker.toSAX(contentHandler);
                    }
                }
                
                // Group switch
                String switchId = null;
                if (group.getSwitch() != null)
                {
                    switchId = group.getSwitch();
                    ConfigParameter switcher = group.getParameter(switchId);

                    XMLUtils.startElement(contentHandler, "switcher");
                    XMLUtils.createElement(contentHandler, "id", switchId); 
                    switcher.getLabel().toSAX(contentHandler, "label");
                    XMLUtils.createElement(contentHandler, "default-value", ParameterHelper.valueToString(switcher.getDefaultValue()));
                    XMLUtils.endElement(contentHandler, "switcher");
                }
                
                XMLUtils.startElement(contentHandler, "elements");
                for (ConfigParameter param : group.getParams(false))
                {
                    String paramId = param.getId();
                    
                    XMLUtils.startElement(contentHandler, paramId);   
                    
                    ParameterHelper.toSAXParameterInternal(contentHandler, param, null);
                    
                    // Disable condition
                    if (param.getDisableConditions() != null)
                    {
                        XMLUtils.createElement(contentHandler, "disable-conditions", param.disableConditionsToJSON());
                    }
                    
                    // Sax parameter checkers attached to a single parameter
                    for (String paramCheckerId : checkers.keySet())
                    {
                        ParameterCheckerDescriptor paramChecker = checkers.get(paramCheckerId);
                        String uiRefParamId = paramChecker.getUiRefParamId();
                        if (uiRefParamId != null && uiRefParamId.equals(paramId))
                        {
                            paramChecker.toSAX(contentHandler);
                        }
                    }
                    
                    XMLUtils.endElement(contentHandler, paramId); 
                }
                XMLUtils.endElement(contentHandler, "elements");

                XMLUtils.endElement(contentHandler, "fieldsets");
            }
            
            XMLUtils.endElement(contentHandler, "elements");
            XMLUtils.endElement(contentHandler, "fieldsets");
        }
    }

    private Map<I18nizableText, ParameterCategory> _sortCategories(Map<I18nizableText, ParameterCategory> categories)
    {
        TreeMap<I18nizableText, ParameterCategory> sortedCategories = new TreeMap<>(new I18nizableTextComparator(_i18nUtils));
        sortedCategories.putAll(categories);

        return sortedCategories;
    }
}
