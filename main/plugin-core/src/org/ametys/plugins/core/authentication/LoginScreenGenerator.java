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
package org.ametys.plugins.core.authentication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.core.authentication.CredentialProvider;
import org.ametys.core.authentication.CredentialProviderFactory;
import org.ametys.core.authentication.CredentialProviderModel;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.util.I18nUtils;
import org.ametys.core.util.JSONUtils;
import org.ametys.plugins.core.impl.authentication.FormCredentialProvider;
import org.ametys.runtime.config.Config;

/**
 * SAX configuration of the login screen
 */
public class LoginScreenGenerator extends ServiceableGenerator
{
    /** Name of the input parameters html field */
    protected CredentialProviderFactory _credentialProviderFactory;
    
    /** Login form manager */
    protected LoginFormManager _loginFormManager;

    /** The JSON helper */
    protected JSONUtils _jsonUtils;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _credentialProviderFactory = (CredentialProviderFactory) smanager.lookup(CredentialProviderFactory.ROLE);
        _loginFormManager = (LoginFormManager) smanager.lookup(LoginFormManager.ROLE);
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
        super.service(smanager);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "LoginScreen");
        
        
        boolean isAmetysPublic = Config.getInstance() != null ? Config.getInstance().getValueAsBoolean("runtime.ametys.public") : false/* in safe mode, we only have one population */;
        
        Boolean invalidPopulationIds = (Boolean) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_INVALID_POPULATION);
        boolean shouldDisplayUserPopulationsList = (boolean) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_SHOULD_DISPLAY_USER_POPULATIONS_LIST);
        List<UserPopulation> usersPopulations = (List<UserPopulation>) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_AVAILABLE_USER_POPULATIONS_LIST);
        String chosenPopulationId = (String) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_USER_POPULATION_ID);
        if (shouldDisplayUserPopulationsList)
        {
            _generatePopulations(usersPopulations, isAmetysPublic, invalidPopulationIds == Boolean.TRUE, chosenPopulationId);
        }
        
        List<CredentialProvider> availableCredentialProviders = (List<CredentialProvider>) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_LIST);
        _generateCredentialProviders(availableCredentialProviders);
        
        _generateLoginForm(request, availableCredentialProviders);
        
        XMLUtils.endElement(contentHandler, "LoginScreen");
        contentHandler.endDocument();
    }
    
    private void _generatePopulations(List<UserPopulation> usersPopulations, boolean isAmetysPublic, boolean tryedAnInvalidPopulationId, String chosenPopulationId) throws SAXException
    {
        if (usersPopulations != null)
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("invalid", tryedAnInvalidPopulationId ? "true" : "false");
            attrs.addCDATAAttribute("public", isAmetysPublic ? "true" : "false");
            if (chosenPopulationId != null)
            {
                attrs.addCDATAAttribute("currentValue", chosenPopulationId);
            }
            XMLUtils.startElement(contentHandler, "UserPopulations", attrs);
            
            if (isAmetysPublic)
            {
                for (UserPopulation up : usersPopulations)
                {
                    AttributesImpl attrs2 = new AttributesImpl();
                    attrs2.addCDATAAttribute("id", up.getId());
                    XMLUtils.startElement(contentHandler, "UserPopulation", attrs2);
                    XMLUtils.createElement(contentHandler, "label", String.valueOf(up.getLabel()));
                    XMLUtils.endElement(contentHandler, "UserPopulation");
                }
            }
        
            XMLUtils.endElement(contentHandler, "UserPopulations");
        }
    }
    
    private void _generateCredentialProviders(List<CredentialProvider> credentialProviders) throws SAXException
    {
        if (credentialProviders == null)
        {
            return;
        }
        
        XMLUtils.startElement(contentHandler, "CredentialProviders");
        for (int index = 0; index < credentialProviders.size(); index++)
        {
            CredentialProvider cp = credentialProviders.get(index);
            CredentialProviderModel cpModel = _credentialProviderFactory.getExtension(cp.getCredentialProviderModelId());
            
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("index", String.valueOf(index));
            attrs.addCDATAAttribute("isForm", cp instanceof FormCredentialProvider ? "true" : "false");
            XMLUtils.startElement(contentHandler, "CredentialProvider", attrs);
            XMLUtils.createElement(contentHandler, "label", I18nUtils.getInstance().translate(cpModel.getConnectionLabel()));
            if (StringUtils.isNotEmpty(cpModel.getIconGlyph()))
            {
                XMLUtils.createElement(contentHandler, "iconGlyph", cpModel.getIconGlyph());
                XMLUtils.createElement(contentHandler, "iconDecorator", cpModel.getIconDecorator());
            }
            else if (StringUtils.isNotEmpty(cpModel.getIconMedium()))
            {
                XMLUtils.createElement(contentHandler, "iconMedium", cpModel.getIconMedium());
            }
            XMLUtils.createElement(contentHandler, "color", cpModel.getColor());
            XMLUtils.endElement(contentHandler, "CredentialProvider");
        }
        XMLUtils.endElement(contentHandler, "CredentialProviders");
    }
    
    private void _generateLoginForm(Request request, List<CredentialProvider> availableCredentialProviders) throws SAXException
    {
        if (availableCredentialProviders == null)
        {
            return;
        }
        
        FormCredentialProvider formBasedCP;
        
        Optional<CredentialProvider> foundAnyFormCredentialProvider = availableCredentialProviders.stream().filter(cp -> cp instanceof FormCredentialProvider).findAny();
        if (foundAnyFormCredentialProvider.isPresent())
        {
            formBasedCP = (FormCredentialProvider) foundAnyFormCredentialProvider.get();
        }
        else
        {
            // We found no form based
            return;
        }
        
        _loginFormManager.deleteAllPastLoginFailedBDD();
        
        String level = (String) formBasedCP.getParameterValues().get("runtime.authentication.form.security.level");
        
        boolean autoComplete = false;
        boolean rememberMe = false;
        boolean captcha = false;
        
        if (FormCredentialProvider.SECURITY_LEVEL_HIGH.equals(level))
        {
            String login = request.getParameter("login");
            int nbConnect = _loginFormManager.requestNbConnectBDD(login);
            
            captcha = nbConnect >= FormCredentialProvider.NB_CONNECTION_ATTEMPTS;
            autoComplete = false;
            rememberMe = false;
        }
        else if (FormCredentialProvider.SECURITY_LEVEL_LOW.equals(level))
        {
            autoComplete = true;
            rememberMe = true;
            captcha = false;
        }
        
        boolean showErrors = !"true".equals(request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_INVALID_POPULATION));
        
        XMLUtils.startElement(contentHandler, "LoginForm");
        
        XMLUtils.createElement(contentHandler, "autocomplete", String.valueOf(autoComplete));
        XMLUtils.createElement(contentHandler, "rememberMe", String.valueOf(rememberMe));
        XMLUtils.createElement(contentHandler, "useCaptcha", String.valueOf(captcha));
        XMLUtils.createElement(contentHandler, "showErrors", String.valueOf(showErrors));
        
        XMLUtils.endElement(contentHandler, "LoginForm");
        
        return;
    }
}
