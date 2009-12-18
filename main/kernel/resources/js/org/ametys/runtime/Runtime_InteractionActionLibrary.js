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

Ext.namespace('org.ametys.runtime');

/**
  * This class allow a plugin to define a simple context path  
  * relative url by emulating a class around it
  */

org.ametys.runtime.Link = function (plugin, parameters)
{
  	var link = parameters['Link'];
  	var mode = parameters['Mode'];
  	
  	runtimeRedirectTo(link, mode, plugin);
}
