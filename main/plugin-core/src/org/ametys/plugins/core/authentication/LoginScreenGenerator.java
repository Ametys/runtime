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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        if (request.getAttribute(AuthenticateAction.REQUEST_POPULATIONS) != null && ((List<UserPopulation>) request.getAttribute(AuthenticateAction.REQUEST_POPULATIONS)).size() == 1)
        {
            // The population is already known
            XMLUtils.createElement(contentHandler, "populationId", ((List<UserPopulation>) request.getAttribute(AuthenticateAction.REQUEST_POPULATIONS)).get(0).getId());
        }
        else if (request.getAttribute(AuthenticateAction.REQUEST_POPULATIONS) != null)
        {
            _generatePopulations(request);
        }
        
        if (request.getAttribute(AuthenticateAction.REQUEST_CHOOSE_CP_LIST) != null)
        {
            _generateCredentialProviders(request);
        }
        
        if (request.getAttribute(AuthenticateAction.REQUEST_FORM_BASED_CREDENTIAL_PROVIDER) != null)
        {
            _generateLoginForm(request);
        }
        
        _generateBackButton(request);
        
        XMLUtils.endElement(contentHandler, "LoginScreen");
        contentHandler.endDocument();
    }
    
    private void _generatePopulations(Request request) throws SAXException
    {
        boolean withCombobox = (boolean) request.getAttribute(AuthenticateAction.REQUEST_AMETYS_PUBLIC);
        
        XMLUtils.startElement(contentHandler, "PopulationsForm");
        XMLUtils.createElement(contentHandler, "invalidError", Boolean.toString("true".equals(request.getAttribute(AuthenticateAction.REQUEST_INVALID_POPULATION))));
        XMLUtils.createElement(contentHandler, "populationCombobox", String.valueOf(withCombobox));
        if (withCombobox)
        {
            @SuppressWarnings("unchecked")
            List<UserPopulation> ups = (List) request.getAttribute(AuthenticateAction.REQUEST_POPULATIONS);
            XMLUtils.startElement(contentHandler, "populations");
            for (UserPopulation up : ups)
            {
                XMLUtils.startElement(contentHandler, "population");
                XMLUtils.createElement(contentHandler, "id", String.valueOf(up.getId()));
                XMLUtils.createElement(contentHandler, "label", String.valueOf(up.getLabel()));
                XMLUtils.endElement(contentHandler, "population");
            }
            XMLUtils.endElement(contentHandler, "populations");
        }
        XMLUtils.endElement(contentHandler, "PopulationsForm");
    }
    
    private void _generateCredentialProviders(Request request) throws SAXException
    {
        @SuppressWarnings("unchecked")
        List<CredentialProvider> credentialProviders = (List<CredentialProvider>) request.getAttribute(AuthenticateAction.REQUEST_CHOOSE_CP_LIST);
        
        XMLUtils.startElement(contentHandler, "credentialProviders");
        for (int index = 0; index < credentialProviders.size(); index++)
        {
            CredentialProvider cp = credentialProviders.get(index);
            CredentialProviderModel cpModel = _credentialProviderFactory.getExtension(cp.getCredentialProviderModelId());
            
            XMLUtils.startElement(contentHandler, "credentialProvider");
            XMLUtils.createElement(contentHandler, "index", String.valueOf(index));
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
        XMLUtils.endElement(contentHandler, "credentialProviders");
    }
    
    private void _generateLoginForm(Request request) throws SAXException
    {
        _loginFormManager.deleteAllPastLoginFailedBDD();
        
        FormCredentialProvider formBasedCP = (FormCredentialProvider) request.getAttribute(AuthenticateAction.REQUEST_FORM_BASED_CREDENTIAL_PROVIDER);
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
        
        boolean showErrors = !"true".equals(request.getAttribute(AuthenticateAction.REQUEST_INVALID_POPULATION));
        
        XMLUtils.startElement(contentHandler, "LoginForm");
        
        XMLUtils.createElement(contentHandler, "autocomplete", String.valueOf(autoComplete));
        XMLUtils.createElement(contentHandler, "rememberMe", String.valueOf(rememberMe));
        XMLUtils.createElement(contentHandler, "useCaptcha", String.valueOf(captcha));
        XMLUtils.createElement(contentHandler, "showErrors", String.valueOf(showErrors));
        
        // We want to store what is the index of the FormCredentialProvider among its siblings
        XMLUtils.createElement(contentHandler, "indexForm", String.valueOf(request.getAttribute(AuthenticateAction.REQUEST_INDEX_FORM_CP)));
        
        XMLUtils.endElement(contentHandler, "LoginForm");
    }
    
    private void _generateBackButton(Request request) throws SAXException
    {
        // generate an hidden input for accessing the previous page for "back" feature
        Map<String, Object> inputParams = new HashMap<>();
        if (request.getParameter(AuthenticateAction.SUBMITTED_POPULATION_PARAMETER_NAME) != null)
        {
            inputParams.put(AuthenticateAction.SUBMITTED_POPULATION_PARAMETER_NAME, request.getParameter(AuthenticateAction.SUBMITTED_POPULATION_PARAMETER_NAME));
        }
        if (request.getParameter(AuthenticateAction.SUBMITTED_CP_INDEX_PARAMETER_NAME) != null)
        {
            inputParams.put(AuthenticateAction.SUBMITTED_CP_INDEX_PARAMETER_NAME, request.getParameter(AuthenticateAction.SUBMITTED_CP_INDEX_PARAMETER_NAME));
        }
        XMLUtils.createElement(contentHandler, "inputParams", _jsonUtils.convertObjectToJson(inputParams));
        
        // display a back button if there is a previous screen, i.e. there is at least one parameter
        boolean drawBackButton = request.getParameter(AuthenticateAction.SUBMITTED_POPULATION_PARAMETER_NAME) != null || request.getParameter(AuthenticateAction.SUBMITTED_CP_INDEX_PARAMETER_NAME) != null; 
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
