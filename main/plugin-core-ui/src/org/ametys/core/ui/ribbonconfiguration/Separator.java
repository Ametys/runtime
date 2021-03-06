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
package org.ametys.core.ui.ribbonconfiguration;

import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A menu separator
 */
public class Separator implements Element
{
    @Override
    public List<Element> getChildren()
    {
        return new ArrayList<>();
    }
    
    @Override
    public void toSAX(ContentHandler handler) throws SAXException
    {
        XMLUtils.createElement(handler, "separator");
    }

    public boolean isSame(Element element)
    {
        return element instanceof Separator;
    }

    public int getColumns()
    {
        return 1;
    }

    public void setColumns(int size)
    {
        // do nothing
    }
}
