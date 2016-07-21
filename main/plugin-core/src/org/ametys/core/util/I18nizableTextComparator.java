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
package org.ametys.core.util;

import java.util.Comparator;

import org.ametys.runtime.i18n.I18nizableText;

/**
 * Sort I18nizableText with their translation
 */
public class I18nizableTextComparator implements Comparator<I18nizableText>
{
    private I18nUtils _i18nUtils;

    /**
     * Constructor.
     * @param i18nUtils the {@link I18nUtils} for actual translations.
     */
    public I18nizableTextComparator(I18nUtils i18nUtils)
    {
        _i18nUtils = i18nUtils;
    }

    @Override
    public int compare(I18nizableText t1, I18nizableText t2)
    {
        String s1 = _i18nUtils.translate(t1);
        String s2 = _i18nUtils.translate(t2);
        
        if (s1 != null && s2 != null)
        {
            return _i18nUtils.translate(t1).compareTo(_i18nUtils.translate(t2));
        }
        else if (s1 == null && s2 == null)
        {
            return 0;
        }
        else if (s1 != null)
        {
            // s2 is null
            return 1;
        }
        else
        {
            // s1 is null
            return -1;
        }
    }
}
