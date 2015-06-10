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

package org.ametys.plugins.core.parameter;


/**
 * A TextValidator where tags are removed from the text (to count chars for example)
 */
public class RichTextValidator extends TextValidator
{
    private static final String __DETECT_EMPTY_PARA = "<p( [^>]+)?>" + (char) 160 + "<\\/p>"; 
    private static final String __DETECT_EMPTY_LINES = "\\r?\\n"; 
    private static final String __DETECT_TAGS = "<[^>]*>"; 
    
    @Override
    protected String getText(Object value)
    {
        String base = super.getText(value);
        
        return base.replaceAll(__DETECT_EMPTY_LINES, "")
                    .replaceAll(__DETECT_EMPTY_PARA, "")
                    .replaceAll(__DETECT_TAGS, "");
    }
}
