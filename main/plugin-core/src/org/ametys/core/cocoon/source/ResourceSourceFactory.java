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
package org.ametys.core.cocoon.source;

import java.io.IOException;
import java.util.Map;

import org.apache.excalibur.source.Source;

/**
 * Extension of Excalibur ResourceSourceFactory to use own ResourceSource
 */
public final class ResourceSourceFactory extends org.apache.excalibur.source.impl.ResourceSourceFactory
{
    @Override
    public Source getSource(String location, Map parameters) throws IOException
    {
        return new ResourceSource(location);
    }
}
