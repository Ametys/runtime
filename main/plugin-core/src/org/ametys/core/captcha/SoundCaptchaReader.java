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
package org.ametys.core.captcha;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.reading.AbstractReader;
import org.xml.sax.SAXException;

/**
 * Generates a sound captcha to WAV format and set it in request attributes
 */
public class SoundCaptchaReader extends AbstractReader
{

    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        // TODO
        
        // Request request = ObjectModelHelper.getRequest(objectModel);
        // AudioInputStream audioIs = new DefaultManageableSoundCaptchaService().getSoundChallengeForID(request.getSession().getId(), Locale.FRENCH);
        // AudioSystem.write(audioIs, Type.WAVE, out); 
    }

}
