/*
 *  Copyright 2012 Anyware Services
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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.ibatis.session.SqlSession;
import org.joda.time.DateTime;

import org.ametys.core.datasource.AbstractMyBatisDAO;

/**
 * Manages registration and password recovery of users.
 */
public class LoginFormManager extends AbstractMyBatisDAO implements Component
{
    /** The component role. */
    public static final String ROLE = LoginFormManager.class.getName();
    
    /** Time allowed to delete data into the BDD (days) */
    public static final int TIME_ALLOWED = 1;
    
    /**
     * Get the number of failed connections with this login
     * @param login The user's login
     * @return nbConnect
     */
    public int requestNbConnectBDD(String login)
    {
        try (SqlSession sqlSession = getSession())
        {
            String stmtId = "LoginFormManager.getNbConnect";
            
            Map<String, Object> params = new HashMap<>();
            params.put("login", login);
            
            List<Integer> results = sqlSession.selectList(stmtId, params);
            return results.isEmpty() ? 0 : results.get(0);
        }
        catch (Exception e)
        {
            getLogger().error("Error during the connection to the database", e);
            return 0;
        }
    }
    
    /**
     * Delete all past failed connections
     */
    public void deleteAllPastLoginFailedBDD()
    {
        try (SqlSession sqlSession = getSession())
        {
            String stmtId = "LoginFormManager.purgeRecords";
            
            Map<String, Object> params = new HashMap<>();
            
            DateTime dateToday = new DateTime();
            DateTime thresholdDate = dateToday.minusDays(TIME_ALLOWED);
            Timestamp threshold = new Timestamp(thresholdDate.getMillis());
            
            params.put("threshold", threshold);
            
            sqlSession.delete(stmtId, params);
            sqlSession.commit();
        }
        catch (Exception e)
        {
            getLogger().error("Error during the connection to the database", e);
        }
    }
}
