/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.plugins.core.captcha;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.captcha.BufferedImageReader;
import org.ametys.core.captcha.CaptchaHelper;

/**
 * Creates a captcha using the id given as src
 */
public class CreateCaptchaAction extends AbstractAction
{
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        String cancelledKey = parameters.getParameter("cancelledKey", "");
        if (StringUtils.isNotEmpty(cancelledKey))
        {
            CaptchaHelper.removeCaptcha(cancelledKey);
        }
        
        boolean fisheye = parameters.getParameterAsBoolean("fisheye", false);
        boolean noise = parameters.getParameterAsBoolean("noise", false);
        int color = parameters.getParameterAsInteger("color", 0x000000);
        int width = parameters.getParameterAsInteger("width", 200);
        int height = parameters.getParameterAsInteger("height", 50);
        
        BufferedImage bi = CaptchaHelper.generateImageCaptcha(source, new Integer(color), noise, fisheye, width, height);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(BufferedImageReader.REQUEST_ATTRIBUTE, bi);
        
        return EMPTY_MAP;
    }

}
