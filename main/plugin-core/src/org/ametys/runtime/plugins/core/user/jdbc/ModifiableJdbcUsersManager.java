/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.user.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.user.InvalidModificationException;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UserListener;
import org.ametys.runtime.util.ConnectionHelper;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.StringUtils;
import org.ametys.runtime.util.parameter.EnumeratorValue;
import org.ametys.runtime.util.parameter.ParameterHelper;


/**
 * Use a jdbc driver for getting the list of users and also update them.<br>
 * This driver depends of the config parameters needed by the JdbcUsers
 * extension.
 */
public class ModifiableJdbcUsersManager extends JdbcUsersManager implements ModifiableUsersManager
{
    /** List on user listener */
    protected List<UserListener> _listeners = new ArrayList<UserListener>();

    public void registerListener(UserListener listener)
    {
        _listeners.add(listener);
    }

    public void removeListener(UserListener listener)
    {
        _listeners.remove(listener);
    }
    
    public void saxModel(ContentHandler handler) throws SAXException
    {
        for (JdbcParameter parameter : _parameters.values())
        {
            AttributesImpl parameterAttr = new AttributesImpl();
            parameterAttr.addAttribute("", "plugin", "plugin", "CDATA", parameter.getPluginName());
            XMLUtils.startElement(handler, "", parameter.getId(), parameterAttr);
    
            XMLUtils.createElement(handler, "label", parameter.getLabelKey());
            XMLUtils.createElement(handler, "description", parameter.getDescriptionKey());
            XMLUtils.createElement(handler, "type", parameter.getTypeAsString());
            
            if (parameter.getWidget() != null)
            {
                XMLUtils.createElement(handler, "widget", parameter.getWidget());
            }
            
            if (parameter.getEnumerator() != null)
            {
                XMLUtils.startElement(handler, "enumeration");
                Collection<EnumeratorValue> values = parameter.getEnumerator().getValues();
                for (EnumeratorValue enumeratorValue : values)
                {
                    Object value = enumeratorValue.getValue();
                    String valueAsString = ParameterHelper.valueToString(value);
                    I18nizableText label = enumeratorValue.getLabel();
    
                    // Produit l'option
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "value", "value", "CDATA", valueAsString);
                    
                    XMLUtils.startElement(handler, "option", attrs);
                    label.toSAX(handler);
                    XMLUtils.endElement(handler, "option");
                }
                XMLUtils.endElement(handler, "enumeration");
            }

            XMLUtils.endElement(handler, parameter.getId());
        }
    }
    
    /**
     * Add a new user to the list.
     * 
     * @param userInformation Informations about the user. The key is the id and the value is the corresponding value
     * @throws InvalidModificationException if the login exists yet or if at least one of the parameter is invalid.
     */
    public void add(Map<String, String> userInformation) throws InvalidModificationException
    {
        Connection con = null;
        PreparedStatement stmt = null;

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting adding a new user");
        }
        
        // Vérifie la présence de tous les paramètres
        Set<String> errorFields = new HashSet<String>();
        for (JdbcParameter parameter : _parameters.values())
        {
            String untypedvalue = userInformation.get(parameter.getId());
            Object typedvalue = ParameterHelper.castValue(untypedvalue, parameter.getType());
            if (parameter.getValidator() != null)
            {
                if (!parameter.getValidator().validate(typedvalue))
                {
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("The field '" + parameter.getId() + "' is not valid");
                    }
                    errorFields.add(parameter.getId());
                }
            }
        }
        
        if (errorFields.size() > 0)
        {
            throw new InvalidModificationException("The creation of user failed because of invalid parameter values", errorFields);
        }
        
        String login = userInformation.get("login");

        try
        {
            // Effectuer la connexion à la base de données via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            stmt = createAddStatement(con, userInformation);

            // Effectuer la requête et vérifier le résultat
            if (stmt.executeUpdate() != 1)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("The user to remove '" + login + "' was not removed.");
                }
                throw new InvalidModificationException("Error no user inserted");
            }

            for (UserListener listener : _listeners)
            {
                listener.userAdded(login);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            throw new InvalidModificationException("Error during the communication with the database", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }

    private PreparedStatement createAddStatement(Connection con, Map<String, String> userInformation) throws SQLException
    {
        String beginClause = "INSERT INTO " + _tableName + " (";
        String middleClause = ") VALUES (";
        String endClause = ")";
        
        StringBuffer intoClause = new StringBuffer();
        StringBuffer valueClause = new StringBuffer();
        for (JdbcParameter parameter : _parameters.values())
        {
            if (intoClause.length() > 0)
            {
                intoClause.append(", ");
                valueClause.append(", ");
            }
            intoClause.append(parameter.getColumn());
            valueClause.append("?");
        }
        
        String sqlRequest = beginClause + intoClause.toString() + middleClause + valueClause + endClause;
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(sqlRequest);
        }
        
        PreparedStatement stmt = con.prepareStatement(sqlRequest);

        int i = 1;
        for (JdbcParameter parameter : _parameters.values())
        {
            if (parameter.getType() == ParameterHelper.TYPE.PASSWORD)
            {
                String encryptedPassword = StringUtils.md5Base64(userInformation.get(parameter.getId()));
                if (encryptedPassword == null)
                {
                    String message = "Cannot encode password";
                    getLogger().error(message);
                    throw new SQLException(message);
                }
                stmt.setString(i++, encryptedPassword);
            }
            else
            {
                stmt.setString(i++, userInformation.get(parameter.getId()));
            }
        }
        
        return stmt;
    }

    /**
     * Modify informations about an user of the list.
     * 
     * @param userInformation New informations about the user.
     * @throws InvalidModificationException if the login does not match in the
     *             list or if at least one of the parameter is invalid.
     */
    public void update(Map<String, String> userInformation) throws InvalidModificationException
    {
        Connection con = null;
        PreparedStatement stmt = null;

        Set<String> errorFields = new HashSet<String>();
        for (String id : userInformation.keySet())
        {
            JdbcParameter parameter = _parameters.get(id);
            if (parameter != null)
            {
                String untypedvalue = userInformation.get(parameter.getId());
                Object typedvalue = ParameterHelper.castValue(untypedvalue, parameter.getType());
                if (parameter.getValidator() != null)
                {
                    if (!parameter.getValidator().validate(typedvalue))
                    {
                        if (getLogger().isDebugEnabled())
                        {
                            getLogger().debug("The field '" + parameter.getId() + "' is not valid");
                        }
                        errorFields.add(parameter.getId());
                    }
                }
            }
        }
        
        if (errorFields.size() > 0)
        {
            throw new InvalidModificationException("The modification of user failed because of invalid parameter values", errorFields);
        }

        String login = userInformation.get("login");
        if (login == null || login.length() == 0)
        {
            throw new InvalidModificationException("Cannot update without login information");
        }

        try
        {
            // Effectuer la connexion à la base de données
            // via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            stmt = createModifyStatement(con, userInformation);

            // Effectuer la requête
            if (stmt.executeUpdate() != 1)
            {
                throw new InvalidModificationException("Error. User '" + login + "' not updated");
            }

            for (UserListener listener : _listeners)
            {
                listener.userUpdated(login);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            throw new InvalidModificationException("Error communication with database", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }

    private PreparedStatement createModifyStatement(Connection con, Map<String, String> userInformation) throws SQLException
    {
        // Contruire la requête pour modifier un utilisateur
        String beginClause = "UPDATE " + _tableName + " SET ";
        String endClause = " WHERE " + _parameters.get("login").getColumn() + " = ?";

        StringBuffer columnNames = new StringBuffer("");
        for (String id : userInformation.keySet())
        {
            JdbcParameter parameter = _parameters.get(id);
            if (parameter != null && !"login".equals(id) && !(parameter.getType() == ParameterHelper.TYPE.PASSWORD && (userInformation.get(parameter.getId()) == null)))
            {
                if (columnNames.length() > 0)
                {
                    columnNames.append(", ");
                }
                columnNames.append(parameter.getColumn() + " = ?");
            }
        }
        
        
        String sqlRequest = beginClause + columnNames.toString() + endClause;
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(sqlRequest);
        }

        PreparedStatement stmt = con.prepareStatement(sqlRequest);
        _fillModifyStatement(stmt, userInformation);

        return stmt;
    }
    
    private void _fillModifyStatement(PreparedStatement stmt, Map<String, String> userInformation) throws SQLException
    {
        int index = 1;
        for (String id : userInformation.keySet())
        {
            JdbcParameter parameter = _parameters.get(id);
            if (parameter != null && !"login".equals(id))
            {
                if (parameter.getType() == ParameterHelper.TYPE.PASSWORD)
                {
                    if (userInformation.get(parameter.getId()) != null)
                    {
                        String encryptedPassword = StringUtils.md5Base64(userInformation.get(parameter.getId()));
                        if (encryptedPassword == null)
                        {
                            String message = "Cannot encrypt password";
                            getLogger().error(message);
                            throw new SQLException(message);
                        }
                        stmt.setString(index++, encryptedPassword);
                    }
                }
                else
                {
                    stmt.setString(index++, userInformation.get(parameter.getId()));
                }
            }
        }
        stmt.setString(index++, userInformation.get("login"));
    }

    public void remove(String login) throws InvalidModificationException
    {
        Connection con = null;
        PreparedStatement stmt = null;

        try
        {
            // Effectuer la connexion à la base de données via un pool de connexion
            con = ConnectionHelper.getConnection(_poolName);

            // Contruire la requête pour supprimer un utilisateur
            String sqlRequest = "DELETE FROM " + _tableName + " WHERE " + _parameters.get("login").getColumn() + " = ?";
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sqlRequest);
            }

            stmt = con.prepareStatement(sqlRequest);
            stmt.setString(1, login);

            // Effectuer la requête et vérifier le résultat
            if (stmt.executeUpdate() != 1)
            {
                throw new InvalidModificationException("Error user was not deleted");
            }

            for (UserListener listener : _listeners)
            {
                listener.userRemoved(login);
            }

        }
        catch (SQLException e)
        {
            throw new InvalidModificationException("Error during the communication with the database", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(con);
        }
    }
}
