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
package org.ametys.core.ui;

import java.util.List;

/**
 * Interface for client side elements that supports relations between a source and a target 
 */
public interface ClientSideRelation extends ClientSideElement
{
    /**
     * Return the type of source relation supported by this Client Side Relation
     * @return the type of source relation supported by this Client Side Relation
     */
    public List<String> getSourceRelationType();
    
    /**
     * Return the type of target relation supported by this Client Side Relation
     * @return the type of target relation supported by this Client Side Relation
     */
    public List<String> getTargetRelationType();
}
