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
import java.util.Map;
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
    /** Time allowed to delete data into the BDD (days) */
    public static final Integer TIME_ALLOWED = 1;
    
    /** Name of the input parameters html field */
    public static final String PARAM_INPUT_PARAMETERS = "inputParameters";
    
    /** The CredentialProvider factory */
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
        Boolean tryedAnInvalidPopulationId = (Boolean) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_INVALID_POPULATION);
        List<UserPopulation> usersPopulations = (List<UserPopulation>) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_POPULATIONS);
        List<CredentialProvider> availableCredentialProviders = (List<CredentialProvider>) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_LIST);
        
        _generatePopulations(usersPopulations, isAmetysPublic, tryedAnInvalidPopulationId == Boolean.TRUE);
        
        boolean formGenerated = _generateLoginForm(request, availableCredentialProviders);
        _generateCredentialProviders(availableCredentialProviders, formGenerated);
        
        _generateBackButton(request);
        
        XMLUtils.endElement(contentHandler, "LoginScreen");
        contentHandler.endDocument();
    }
    
    private void _generatePopulations(List<UserPopulation> usersPopulations, boolean isAmetysPublic, boolean tryedAnInvalidPopulationId) throws SAXException
    {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("invalid", tryedAnInvalidPopulationId ? "true" : "false");
        XMLUtils.startElement(contentHandler, "populations", attrs);
        
        if (isAmetysPublic)
        {
            for (UserPopulation up : usersPopulations)
            {
                AttributesImpl attrs2 = new AttributesImpl();
                attrs2.addCDATAAttribute("id", up.getId());
                XMLUtils.startElement(contentHandler, "population", attrs2);
                XMLUtils.createElement(contentHandler, "label", String.valueOf(up.getLabel()));
                XMLUtils.endElement(contentHandler, "population");
            }
        }
    
        XMLUtils.endElement(contentHandler, "populations");
    }
    
    private void _generateCredentialProviders(List<CredentialProvider> credentialProviders, boolean formAlreadyGenerated) throws SAXException
    {
        if (credentialProviders == null)
        {
            return;
        }
        
        XMLUtils.startElement(contentHandler, "credentialProviders");
        for (int index = 0; index < credentialProviders.size(); index++)
        {
            CredentialProvider cp = credentialProviders.get(index);
            // We should not send the FormCredentialProvider button, if it have been inlined
            if (!formAlreadyGenerated || !(cp instanceof FormCredentialProvider))
            {
                CredentialProviderModel cpModel = _credentialProviderFactory.getExtension(cp.getCredentialProviderModelId());
                
                AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("index", String.valueOf(index));
                XMLUtils.startElement(contentHandler, "credentialProvider", attrs);
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
                XMLUtils.endElement(contentHandler, "credentialProvider");
            }
        }
        XMLUtils.endElement(contentHandler, "credentialProviders");
    }
    
    private boolean _generateLoginForm(Request request, List<CredentialProvider> availableCredentialProviders) throws SAXException
    {
        if (availableCredentialProviders == null)
        {
            return false;
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
            return false;
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
            
            captcha = nbConnect >= 3;
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
        
        return true;
    }
    
    private void _generateBackButton(Request request) throws SAXException
    {
        // display a back button if there is a previous screen, i.e. there is at least one parameter
        boolean drawBackButton = request.getParameter(AuthenticateAction.REQUEST_PARAMETER_POPULATION_NAME) != null || request.getParameter(AuthenticateAction.REQUEST_PARAMETER_CREDENTIALPROVIDER_INDEX) != null; 
        XMLUtils.createElement(contentHandler, "backButton", Boolean.toString(drawBackButton));
        
        if (drawBackButton)
        {
            XMLUtils.startElement(contentHandler, "backButtonParams");
            String stringBackParams = request.getParameter(PARAM_INPUT_PARAMETERS);
            Map<String, Object> backParams = _jsonUtils.convertJsonToMap(stringBackParams);
            for (String paramName : backParams.keySet())
            {
                AttributesImpl atts = new AttributesImpl();
                atts.addCDATAAttribute("name", paramName);
                atts.addCDATAAttribute("value", (String) backParams.get(paramName));
                XMLUtils.createElement(contentHandler, "backButtonParam", atts);
            }
            XMLUtils.endElement(contentHandler, "backButtonParams");
        }
    }
}
