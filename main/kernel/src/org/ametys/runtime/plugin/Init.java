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
package org.ametys.runtime.plugin;

/**
 * Interface for runtime initialization classes
 */
public interface Init
{
    /** Avalon Role */
    public static final String ROLE = Init.class.getName();

    /**
     * Method to be implemented by user Init class. Here you should manage the
     * business part of your application init.
     * As an Avalon component, class extending this interface have access to all Cocoon-managed components, all plugins, ...
     * @throws Exception if an error occurs
     */
    public void init() throws Exception;
}
