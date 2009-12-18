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
package org.ametys.runtime.plugins.core.user.ldap;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.user.CredentialsAwareUsersManager;


/**
 * Use an ldap directory for getting the list of users and also authenticating
 * them.<br>
 * This driver depends of the config parameters needed by the LdapUsers
 * extension :<br>
 * @see org.ametys.runtime.plugins.core.user.ldap.LdapUsersManager
 */
public class CredentialsAwareLdapUsersManager extends LdapUsersManager implements CredentialsAwareUsersManager
{
    public boolean checkCredentials(Credentials credentials)
    {
        String login = credentials.getLogin();
        String password = credentials.getPassword();
        
        boolean authenticated = false;

        // Vérifier que le mot de passe n'est pas vide
        if (password != null && password.length() != 0)
        {
            // Récupérer le DN de l'utilisateur
            String userDN = getUserDN(login);
            if (userDN != null)
            {
                DirContext context = null;

                // Récupérer les paramètres de connexion
                Hashtable<String, String> env = _getContextEnv();

                // Modifier le dn et le mot de passe pour l'authentification
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
                env.put(Context.SECURITY_PRINCIPAL, userDN);
                env.put(Context.SECURITY_CREDENTIALS, password);

                try
                {
                    // Connexion et authentification au serveur ldap
                    context = new InitialDirContext(env);
                    // Authentification réussie
                    authenticated = true;
                }
                catch (AuthenticationException e)
                {
                    if (getLogger().isInfoEnabled())
                    {
                        getLogger().info("Authentication failed", e);
                    }
                }
                catch (NamingException e)
                {
                    // Erreur
                    getLogger().error("Error communication with ldap server", e);
                }
                finally
                {
                    // Fermer les ressources de connexion
                    _cleanup(context, null);
                }
            }
        }
        else if (getLogger().isDebugEnabled())
        {
            getLogger().debug("LDAP Authentication failed since no password (or an empty one) was given");
        }

        // Si une erreur est arrivée, ne pas authentifier l'utilisateur
        return authenticated;
    }

    /**
     * Get the distinguished name of an user by his login.
     * 
     * @param login Login of the user.
     * @return The dn of the user, or null if there is no match or if multiple
     *         matches.
     */
    protected String getUserDN(String login)
    {
        String userDN = null;
        DirContext context = null;
        NamingEnumeration results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            // Créer le filtre de recherche
            String filter = "(&" + _usersObjectFilter + "(" + _usersLoginAttribute + "={0}))";
            Object[] params = new Object[] {login};

            SearchControls constraints = new SearchControls();
            // Choisir la profondeur de la recherche paramétrée
            constraints.setSearchScope(_usersSearchScope);
            // Ne pas demander d'attributs, on veut seulement le DN
            constraints.setReturningAttributes(new String[] {});

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, filter, params, constraints);

            // Remplir la liste des utilisateurs
            if (results.hasMoreElements())
            {
                SearchResult result = (SearchResult) results.nextElement();

                // Récupére le DN
                userDN = result.getName();
                if (result.isRelative())
                {
                    // Retrouver le DN de façon absolue
                    NameParser parser = context.getNameParser("");
                    Name topDN = parser.parse(context.getNameInNamespace());
                    topDN.addAll(parser.parse(_usersRelativeDN));
                    topDN.addAll(parser.parse(userDN));
                    userDN = topDN.toString();
                }
            }
            
            if (results.hasMoreElements())
            {
                // Annuler le résultat car plusieurs correspondances pour un
                // login
                userDN = null;
                getLogger().error("Multiple matches for attribute \"" + _usersLoginAttribute + "\" and value = \"" + login + "\"");
            }
        }
        catch (NamingException e)
        {
            getLogger().error("Error communication with ldap server", e);
        }

        finally
        {
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }
        return userDN;
    }
}
