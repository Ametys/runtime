/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.administrator.password;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;

import org.ametys.runtime.util.MapHandler;
import org.ametys.runtime.util.StringUtils;
import org.ametys.runtime.workspaces.admin.authentication.AdminAuthenticateAction;


/**
 * Action that sets the new password into de admin.xml file using MD5 algorithm.
 * <br>
 * The password is changed only after some verifications are done onto
 * parameters. <br>
 * Parameters of sitemap are <br>
 * oldPassword, newPassword and confirmPassword. <br>
 * The action can returns null on error or an empty map on success
 */
public class SetPasswordAction extends AbstractAction implements ThreadSafe
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Starting SetPassword");
        }

        try
        {
            // Récupèration des paramètres
            String oldPassword = parameters.getParameter("oldPassword");
            String newPassword = parameters.getParameter("newPassword");
            String confirmPassword = parameters.getParameter("confirmPassword");

            // Encryptage du oldPassword
            String oldEncryptedPassword = StringUtils.md5Base64(oldPassword);

            // Lecture du fichier d'admin
            Map<String, String> admin = new HashMap<String, String>();
            Source adminSource = resolver.resolveURI("context://" + AdminAuthenticateAction.ADMINISTRATOR_PASSWORD_FILENAME);
            if (adminSource.exists())
            {
                SourceUtil.toSAX(adminSource, new MapHandler(admin));
            }
            
            // Récupération du mot de passe crypté actuel
            String currentEncryptedPassword = admin.get("password");

            // Vérifications
            if (currentEncryptedPassword != null && (!currentEncryptedPassword.equals(oldEncryptedPassword)
                                                    || newPassword == null 
                                                    || "".equals(newPassword)
                                                    || !newPassword.equals(confirmPassword)))
            {
                
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Administrator failed to change password (empty passwor, wrong old password, wrong confirmation)...");
                }

                Map<String, String> results = new HashMap<String, String>();
                results.put("result", "FAILED");
                return results;
            }
            else
            {
                // Sauvegarde apres encryptage du newPassword
                String adminFilename = ObjectModelHelper.getContext(objectModel).getRealPath(AdminAuthenticateAction.ADMINISTRATOR_PASSWORD_FILENAME);
                save(adminFilename, StringUtils.md5Base64(newPassword));
                
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Administrator has successfully changed password");
                }

                Map<String, String> results = new HashMap<String, String>();
                results.put("result", "SUCCESS");
                return results;
            }
        }
        catch (Exception e)
        {
            getLogger().error("An unknown error occured while changing the password", e);

            Map<String, String> results = new HashMap<String, String>();
            results.put("result", "FAILED");
            return results;
        }
    }

    /*
     * Effectue la sauvegarde du nouveau password dans la source du fichier
     * admin.xml
     */
    private void save(String adminFilename, String newEncryptedPassword) throws Exception
    {
        File adminFile = new File(adminFilename);
        adminFile.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(adminFile);

        // create a transformer for saving sax into a file
        TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
        // create the result where to write
        StreamResult sResult = new StreamResult(os);
        th.setResult(sResult);

        // create the format of result
        Properties format = new Properties();
        format.put(OutputKeys.METHOD, "xml");
        format.put(OutputKeys.INDENT, "yes");
        format.put(OutputKeys.ENCODING, "UTF-8");
        th.getTransformer().setOutputProperties(format);

        // Envoi des événements sax
        th.startDocument();
        XMLUtils.startElement(th, "admin");
        XMLUtils.startElement(th, "password");
        XMLUtils.data(th, newEncryptedPassword);
        XMLUtils.endElement(th, "password");
        XMLUtils.endElement(th, "admin");       
        th.endDocument();

        os.close();
    }
}
