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
package org.ametys.plugins.core.impl.schedule;

import java.util.Map;

import org.ametys.core.schedule.Runnable;
import org.ametys.core.schedule.Runnable.MisfirePolicy;
import org.ametys.core.schedule.Schedulable;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * Implementation of {@link Runnable} which can be created by the UI.
 */
public class DefaultRunnable implements Runnable
{
    /** The id */
    protected String _id;
    /** The label */
    protected I18nizableText _label;
    /** The description */
    protected I18nizableText _description;
    /** true to run at startup */
    protected boolean _runAtStartup;
    /** The CRON expression */
    protected String _cron;
    /** The id of the linked {@link Schedulable} */
    protected String _schedulableId;
    /** true if it can be removed */
    protected boolean _removable;
    /** true if it can be edited */
    protected boolean _modifiable;
    /** true if it can be deactivated */
    protected boolean _deactivatable;
    /** The parameter values */
    protected Map<String, Object> _parameterValues;
    /** The misfire policy */
    protected MisfirePolicy _misfirePolicy;
    /** true if volatile */
    protected boolean _volatile;

    /**
     * Constructor
     * @param id the id
     * @param label the label
     * @param description the descritpion
     * @param runAtStartup true to run at startup (cron expression will be ignore)
     * @param cron the cron expression
     * @param schedulableId the id of the linked {@link Schedulable}
     * @param removable true if it can be removed
     * @param modifiable true if it can be edited
     * @param deactivatable true if it can be deactivated
     * @param misfirePolicy The misfire policy. Can be null, the default value is {@link MisfirePolicy#DO_NOTHING}
     * @param isVolatile true if it is volatile, i.e. if it must not survive to a server restart
     * @param parameters the parameter values
     */
    public DefaultRunnable(String id,
                           I18nizableText label,
                           I18nizableText description,
                           boolean runAtStartup,
                           String cron,
                           String schedulableId,
                           boolean removable,
                           boolean modifiable,
                           boolean deactivatable,
                           MisfirePolicy misfirePolicy,
                           boolean isVolatile,
                           Map<String, Object> parameters)
    {
        _id = id;
        _label = label;
        _description = description;
        _runAtStartup = runAtStartup;
        _cron = cron;
        _schedulableId = schedulableId;
        _removable = removable;
        _modifiable = modifiable;
        _deactivatable = deactivatable;
        _misfirePolicy = misfirePolicy != null ? misfirePolicy : MisfirePolicy.DO_NOTHING;
        _volatile = isVolatile;
        _parameterValues = parameters;
    }
    
    @Override
    public String getId()
    {
        return _id;
    }

    @Override
    public I18nizableText getLabel()
    {
        return _label;
    }

    @Override
    public I18nizableText getDescription()
    {
        return _description;
    }

    @Override
    public boolean runAtStartup()
    {
        return _runAtStartup;
    }

    @Override
    public String getCronExpression()
    {
        return _cron;
    }

    @Override
    public String getSchedulableId()
    {
        return _schedulableId;
    }

    @Override
    public boolean isRemovable()
    {
        return _removable;
    }

    @Override
    public boolean isModifiable()
    {
        return _modifiable;
    }

    @Override
    public boolean isDeactivatable()
    {
        return _deactivatable;
    }

    @Override
    public MisfirePolicy getMisfirePolicy()
    {
        return _misfirePolicy;
    }
    
    @Override
    public boolean isVolatile()
    {
        return _volatile;
    }

    @Override
    public Map<String, Object> getParameterValues()
    {
        return _parameterValues;
    }
}
