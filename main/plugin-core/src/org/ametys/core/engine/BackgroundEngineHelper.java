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
package org.ametys.core.engine;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.background.BackgroundEnvironment;
import org.apache.cocoon.environment.commandline.AbstractCommandLineEnvironment;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Abstract class for work run in a separate thread.
 */
public final class BackgroundEngineHelper
{
    
    private BackgroundEngineHelper()
    {
        // Hides the default constructor.
    }
    
    /**
     * Test if cocoon is currently in an environment.
     * @return true if cocoon is currently in an environment, false otherwise.
     */
    public static boolean environmentExists()
    {
        return CocoonComponentManager.getCurrentEnvironment() != null;
    }
    
    /**
     * Create and enter a cocoon environment specific to the engine.
     * @param manager the avalon service manager.
     * @param context the cocoon environment context.
     * @param logger the class logger.
     * @return a Map with the environment information.
     */
    public static Map<String, Object> createAndEnterEngineEnvironment(ServiceManager manager, Context context, Logger logger)
    {
        BackgroundEnvironment environment;
        Processor processor;
        
        // Création de l'environnement cocoon particulier
        try
        {
            environment = new BackgroundEnvironment(logger, context);
            processor = (Processor) manager.lookup(Processor.ROLE);
        }
        catch (Exception e)
        {
            throw new CascadingRuntimeException("Error during environment's setup.", e);
        }
        
        // Random request ID: used by AbstractCachingPipeline to implement pipeline locking.
        String requestId = RandomStringUtils.randomAlphanumeric(8);
        environment.getObjectModel().put(AbstractCommandLineEnvironment.CLI_REQUEST_ID, requestId);
        
        Object processingKey = CocoonComponentManager.startProcessing(environment);
        int environmentDepth = CocoonComponentManager.markEnvironment();
        
        CocoonComponentManager.enterEnvironment(environment, new WrapperComponentManager(manager), processor);
        
        BackgroundEngineHookExtensionPoint backgroundEngineHookEP;
        try
        {
            backgroundEngineHookEP = (BackgroundEngineHookExtensionPoint) manager.lookup(BackgroundEngineHookExtensionPoint.ROLE);
        }
        catch (Exception e)
        {
            throw new CascadingRuntimeException("Error during environment's setup.", e);
        }
        
        Map<String, Object> result = new HashMap<>();
        
        result.put("manager", manager);
        result.put("logger", logger);
        result.put("environment", environment);
        result.put("processor", processor);
        result.put("processingKey", processingKey);
        result.put("environmentDepth", new Integer(environmentDepth));
        result.put("hookEP", backgroundEngineHookEP);
        
        // on enter hooks
        for (String hookId : backgroundEngineHookEP.getExtensionsIds())
        {
            BackgroundEngineHook hook = backgroundEngineHookEP.getExtension(hookId);
            hook.onEnteringEnvironment(result);
        }
        
        return result;
    }
    
    /**
     * Leave the cocoon environment.
     * @param environmentInformation the environment information.
     */
    public static void leaveEngineEnvironment(Map<String, Object> environmentInformation)
    {
        BackgroundEnvironment environment = (BackgroundEnvironment) environmentInformation.get("environment");
        Processor processor = (Processor) environmentInformation.get("processor");
        Object processingKey = environmentInformation.get("processingKey");
        int environmentDepth = ((Integer) environmentInformation.get("environmentDepth")).intValue();
        ServiceManager manager = (ServiceManager) environmentInformation.get("manager");
        Logger logger = (Logger) environmentInformation.get("logger");
        BackgroundEngineHookExtensionPoint backgroundEngineHookEP = (BackgroundEngineHookExtensionPoint) environmentInformation.get("hookEP");
        
        // on leave hooks
        for (String hookId : backgroundEngineHookEP.getExtensionsIds())
        {
            BackgroundEngineHook hook = backgroundEngineHookEP.getExtension(hookId);
            hook.onLeavingEnvironment(environmentInformation);
        }
        
        CocoonComponentManager.leaveEnvironment();
        CocoonComponentManager.endProcessing(environment, processingKey);
        
        try
        {
            CocoonComponentManager.checkEnvironment(environmentDepth, logger);
        }
        catch (Exception e)
        {
            throw new CascadingRuntimeException("Error checking the environment", e);
        }
        
        manager.release(backgroundEngineHookEP);
        manager.release(processor);
    }
}
