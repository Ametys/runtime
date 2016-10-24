/*
 *  Copyright 2011 Anyware Services
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

package org.ametys.core.util.dom;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DOM layer on top if a {@link File} hierarchy.
 */
public class FileElement extends AbstractWrappingAmetysElement<File>
{
    /**
     * Constructor.
     * @param file the underlying {@link File}.
     */
    public FileElement(File file)
    {
        this(file, null);
    }

    /**
     * Constructor.
     * @param file the underlying {@link File}.
     * @param parent the parent {@link Element}.
     */
    public FileElement(File file, FileElement parent)
    {
        super(file, parent);
    }
    
    @Override
    public String getTagName()
    {
        return _object.isDirectory() ? "collection" : "resource";
    }
    
    @Override
    protected Map<String, AmetysAttribute> _lookupAttributes()
    {
        Map<String, AmetysAttribute> result = new HashMap<>();
        
        result.put("name", new AmetysAttribute("name", "name", null, _object.getName(), this));
        
        return result;
    }
    
    @Override
    public boolean hasChildNodes()
    {
        File[] files = _object.listFiles();
        return files != null ? files.length > 0 : false;
    }
    
    @Override
    public Node getFirstChild()
    {
        File[] files = _object.listFiles();
        
        if (files != null && files.length > 0)
        {
            return new FileElement(files[0], this);
        }
        
        return null;
    }
    
    @Override
    public Node getNextSibling()
    {
        if (_parent == null)
        {
            return null;
        }
        
        File parent = (File) ((AbstractWrappingAmetysElement) _parent).getWrappedObject();
        
        File[] children = parent.listFiles();
        
        boolean isNext = false;
        File nextSibling = null;
        int i = 0;
        
        while (nextSibling == null && i < children.length)
        {
            File child = children[i++];
            
            if (isNext)
            {
                nextSibling = child;
            }
            else if (_object.getAbsolutePath().equals(child.getAbsolutePath()))
            {
                isNext = true;
            }
        }
        
        return nextSibling == null ? null : new FileElement(nextSibling, (FileElement) _parent);
    }
}
