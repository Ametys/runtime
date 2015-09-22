package org.ametys.plugins.core.right.profile;

import java.util.Map;

import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.plugins.core.impl.right.profile.ProfileBasedRightsManager;

/**
 * This implementation creates a control only available if the rights manager is a {@link ProfileBasedRightsManager}
 */
public class ProfileBasedClientSideElement extends StaticClientSideElement
{
    @Override
    public Script getScript(Map<String, Object> contextParameters)
    {
        if (_rightsManager instanceof ProfileBasedRightsManager)
        {
            return super.getScript(contextParameters);
        }
        
        return null;
    }
}
