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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import org.ametys.core.authentication.CredentialProvider;
import org.ametys.core.authentication.CredentialProviderFactory;
import org.ametys.core.authentication.CredentialProviderModel;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
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
    
    /** The DAO for user populations */
    protected UserPopulationDAO _userPopulationDAO;

    /** The JSON helper */
    protected JSONUtils _jsonUtils;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _credentialProviderFactory = (CredentialProviderFactory) smanager.lookup(CredentialProviderFactory.ROLE);
        _userPopulationDAO = (UserPopulationDAO) smanager.lookup(UserPopulationDAO.ROLE);
        _loginFormManager = (LoginFormManager) smanager.lookup(LoginFormManager.ROLE);
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "LoginScreen");
        
        // LoginScreenGenerator can be used from the frontoffice AuthenticatAction : it can not use the requests attributes
        boolean isAmetysPublic = Config.getInstance() != null ? Config.getInstance().getValueAsBoolean("runtime.ametys.public") : false/* in safe mode, we only have one population */;
        
        boolean invalidPopulationIds = "true".equals(request.getParameter("invalidPopulationIds"));
        
        boolean shouldDisplayUserPopulationsList = "true".equals(request.getParameter("shouldDisplayUserPopulationsList"));
        
        List<UserPopulation> usersPopulations = null;
        String usersPopulationsIdsAsString = request.getParameter("usersPopulations");
        if (usersPopulationsIdsAsString != null)
        {
            usersPopulations = Arrays.stream(usersPopulationsIdsAsString.split(",")).map(_userPopulationDAO::getUserPopulation).collect(Collectors.toList());
        }
        
        String chosenPopulationId = request.getParameter("chosenPopulationId");
        
        if (shouldDisplayUserPopulationsList)
        {
            _generatePopulations(usersPopulations, isAmetysPublic, invalidPopulationIds, chosenPopulationId);
        }
        
        boolean availableCredentialProviders = "true".equals(request.getParameter("availableCredentialProviders"));
        List<CredentialProvider> credentialProviders = null;
        if (availableCredentialProviders && usersPopulations != null && !usersPopulations.isEmpty())
        {
            if (StringUtils.isNotBlank(chosenPopulationId))
            {
                credentialProviders = usersPopulations.stream().filter(userPop -> chosenPopulationId.equals(userPop.getId())).findAny().get().getCredentialProviders();
            }
            else
            {
                credentialProviders = usersPopulations.get(0).getCredentialProviders();
            }
        }
        int credentialProviderIndex = Integer.parseInt(request.getParameter("credentialProviderIndex"));

        _generateCredentialProviders(credentialProviders, credentialProviderIndex);
        
        _generateLoginForm(request, credentialProviders, credentialProviderIndex, invalidPopulationIds);
        
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
    
    private void _generateCredentialProviders(List<CredentialProvider> credentialProviders, int currentCredentialProvider) throws SAXException
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
            attrs.addCDATAAttribute("selected", index == currentCredentialProvider ? "true" : "false");
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
    
    private void _generateLoginForm(Request request, List<CredentialProvider> credentialProviders, int currentCredentialProvider, boolean invalidPopulationIds) throws SAXException
    {
        if (credentialProviders == null)
        {
            return;
        }
        
        FormCredentialProvider formBasedCP = null;
        if (currentCredentialProvider != -1 && credentialProviders.get(currentCredentialProvider) instanceof FormCredentialProvider)
        {
            formBasedCP = (FormCredentialProvider) credentialProviders.get(currentCredentialProvider);
        }
        else
        {
            Optional<CredentialProvider> foundAnyFormCredentialProvider = credentialProviders.stream().filter(cp -> cp instanceof FormCredentialProvider).findAny();
            if (foundAnyFormCredentialProvider.isPresent())
            {
                formBasedCP = (FormCredentialProvider) foundAnyFormCredentialProvider.get();
            }
            else
            {
                // We found no form based
                return;
            }
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
        
        boolean showErrors = !invalidPopulationIds;
        
        AttributesImpl attrs = new AttributesImpl();
        XMLUtils.startElement(contentHandler, "LoginForm", attrs);
        
        XMLUtils.createElement(contentHandler, "autocomplete", String.valueOf(autoComplete));
        XMLUtils.createElement(contentHandler, "rememberMe", String.valueOf(rememberMe));
        XMLUtils.createElement(contentHandler, "useCaptcha", String.valueOf(captcha));
        XMLUtils.createElement(contentHandler, "showErrors", String.valueOf(showErrors));
        
        XMLUtils.endElement(contentHandler, "LoginForm");
    }
}
