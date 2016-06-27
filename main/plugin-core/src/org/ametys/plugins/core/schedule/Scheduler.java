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
package org.ametys.plugins.core.schedule;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.utils.PoolingConnectionProvider;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.datasource.ConnectionHelper.DatabaseType;
import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.core.right.RightsManager;
import org.ametys.core.schedule.AmetysJob;
import org.ametys.core.schedule.Runnable;
import org.ametys.core.schedule.Runnable.FireProcess;
import org.ametys.core.schedule.RunnableExtensionPoint;
import org.ametys.core.schedule.Schedulable;
import org.ametys.core.schedule.SchedulableExtensionPoint;
import org.ametys.core.script.ScriptRunner;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.util.LambdaUtils;
import org.ametys.plugins.core.impl.schedule.DefaultRunnable;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * The scheduler component
 */
public class Scheduler extends AbstractLogEnabled implements Component, Initializable, Disposable, Serviceable, Contextualizable
{
    /** The Avalon Role */
    public static final String ROLE = Scheduler.class.getName();
    
    /** The group name for jobs */
    public static final String JOB_GROUP = "runtime.job";
    /** The group name for triggers */
    public static final String TRIGGER_GROUP = "runtime.trigger";
    /** The key for the id of the schedulable to execute */
    public static final String KEY_SCHEDULABLE_ID = "schedulableId";
    /** The key for the runnable id */
    public static final String KEY_RUNNABLE_ID = "id";
    /** The key for the runnable label */
    public static final String KEY_RUNNABLE_LABEL = "label";
    /** The key for the runnable description */
    public static final String KEY_RUNNABLE_DESCRIPTION = "description";
    /** The key for the runnable fire process property */
    public static final String KEY_RUNNABLE_FIRE_PROCESS = "fireProcess";
    /** The key for 'run at startup' jobs indicating if the job has already been executed and is now completed */
    public static final String KEY_RUNNABLE_STARTUP_COMPLETED = "runAtStartupCompleted";
    /** The key for the runnable cron expression */
    public static final String KEY_RUNNABLE_CRON = "cron";
    /** The key for the runnable removable property */
    public static final String KEY_RUNNABLE_REMOVABLE = "removable";
    /** The key for the runnable modifiable property */
    public static final String KEY_RUNNABLE_MODIFIABLE = "modifiable";
    /** The key for the runnable deactivatable property */
    public static final String KEY_RUNNABLE_DEACTIVATABLE = "deactivatable";
    /** The key for the runnable volatile property */
    public static final String KEY_RUNNABLE_VOLATILE = "volatile";
    /** The prefix for the parameter values of the runnable job in the job data map */
    public static final String PARAM_VALUES_PREFIX = "parameterValues#";
    /** Name of the parameter holding the datasource id for Quartz */
    public static final String DATASOURCE_CONFIG_NAME = "runtime.scheduler.datasource";
    
    /** The name of the configuration file for Quartz */
    private static final String __QUARTZ_CONFIG_FILE_NAME = "quartz.properties";
    /** The id of the right to execute actions on tasks */
    private static final String __RIGHT_SCHEDULER = "CORE_Rights_TaskScheduler";
    
    /** The service manager */
    protected ServiceManager _manager;
    /** The context */
    protected Context _context;
    /** The extension point for {@link Runnable}s */
    protected RunnableExtensionPoint _runnableEP;
    /** The extension point for {@link Schedulable}s */
    protected SchedulableExtensionPoint _schedulableEP;
    /** The Quartz scheduler */
    protected org.quartz.Scheduler _scheduler;
    /** The manager for SQL datasources */
    protected SQLDataSourceManager _sqlDataSourceManager;
    /** The source resolver */
    protected SourceResolver _sourceResolver;
    /** The rights manager */
    protected RightsManager _rightsManager;
    /** The provider of current user */
    protected CurrentUserProvider _currentUserProvider;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _runnableEP = (RunnableExtensionPoint) manager.lookup(RunnableExtensionPoint.ROLE);
        _schedulableEP = (SchedulableExtensionPoint) manager.lookup(SchedulableExtensionPoint.ROLE);
        _sqlDataSourceManager = (SQLDataSourceManager) manager.lookup(SQLDataSourceManager.ROLE);
        _sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
        _rightsManager = (RightsManager) manager.lookup(RightsManager.ROLE);
        _currentUserProvider = (CurrentUserProvider) manager.lookup(CurrentUserProvider.ROLE);
    }
    
    @Override
    public void initialize() throws Exception
    {
        // Be sure the tables are created
        String dsId = Config.getInstance().getValueAsString(DATASOURCE_CONFIG_NAME);
        _checkAndCreateTables(dsId);
        
        // Get info about datasource
        Map<String, String> dsParameters = _sqlDataSourceManager.getDataSourceDefinition(dsId).getParameters();
        String url = dsParameters.get("url");
        String user = dsParameters.get("user");
        String password = dsParameters.get("password");
        String driver = dsParameters.get("driver");
        DatabaseType dbType = ConnectionHelper.getDatabaseType(url);
        String dsName = "quartzDb";
        
        // Set the properties for Quartz
        Properties props = new Properties();
        props.load(Scheduler.class.getResourceAsStream(__QUARTZ_CONFIG_FILE_NAME));
        props.setProperty("org.quartz.jobStore.dataSource", dsName);
        props.setProperty(StdSchedulerFactory.PROP_DATASOURCE_PREFIX + "." + dsName + "." + PoolingConnectionProvider.DB_DRIVER, driver);
        props.setProperty(StdSchedulerFactory.PROP_DATASOURCE_PREFIX + "." + dsName + "." + PoolingConnectionProvider.DB_URL, url);
        props.setProperty(StdSchedulerFactory.PROP_DATASOURCE_PREFIX + "." + dsName + "." + PoolingConnectionProvider.DB_USER, user);
        props.setProperty(StdSchedulerFactory.PROP_DATASOURCE_PREFIX + "." + dsName + "." + PoolingConnectionProvider.DB_PASSWORD, password);
        props.setProperty("org.quartz.jobStore.driverDelegateClass", _getDriverDelegateClass(dbType));
        StdSchedulerFactory factory = new StdSchedulerFactory(props);
        _scheduler = factory.getScheduler();
        
        // Initialize AmetysJob class
        AmetysJob.initialize(_manager, _context, getLogger());
    }
    
    /* copy/paste of SqlTablesInit#init() because SqlTablesInit comes too late */
    private void _checkAndCreateTables(String dataSourceId)
    {
        DataSource dataSource = null;
        try
        {
            dataSource = _sqlDataSourceManager.getSQLDataSource(dataSourceId);
        }
        catch (Exception e)
        {
            // silently ignore
        }
        
        if (dataSource == null)
        {
            getLogger().warn("Configured data source could not be found. Data source id: '{}'", StringUtils.defaultString(dataSourceId));
            return;
        }
        
        try
        {
            // Test and create tables
            Connection connection = dataSource.getConnection();
            
            try
            {
                _initTables(connection, dataSourceId);
            }
            finally
            {
                ConnectionHelper.cleanup(connection);
            }
        }
        catch (Exception e)
        {
            String errorMsg = String.format("Error during SQL tables initialization for data source id: '%s'.",
                    StringUtils.defaultString(dataSourceId));
            getLogger().error(errorMsg, e);
        }
    }
    
    private void _initTables(Connection connection, String dataSourceId) throws SQLException
    {
        String scriptFolder = null;
        
        DatabaseType dbType = ConnectionHelper.getDatabaseType(connection);
        switch (dbType)
        {
            case DATABASE_DERBY:
                scriptFolder = "derby"; break;
            case DATABASE_HSQLDB:
                scriptFolder = "hsqldb"; break;
            case DATABASE_MYSQL:
                scriptFolder = "mysql"; break;
            case DATABASE_ORACLE:
                scriptFolder = "oracle"; break;
            case DATABASE_POSTGRES:
                scriptFolder = "postgresql"; break;
            case DATABASE_UNKNOWN:
            default:
                getLogger().warn("This data source is not compatible with the automatic creation of the SQL tables. The tables will not be created. Data source id: '{}'", dataSourceId);
                return;
        }
        
        // location = plugin:PLUGIN_NAME://scripts/SCRIPT_FOLDER/SQL_FILENAME
        String locationPrefix = "plugin:core://scripts/" + scriptFolder + "/";
        
        String tableName = "qrtz_job_details"; // assume that either none of the tables are created, either all are created
        if (!_tableExists(connection, tableName))
        {
            String location = locationPrefix + "quartz.sql";
            Source source = null;
            
            try
            {
                source = _sourceResolver.resolveURI(location);
                ScriptRunner.runScript(connection, source.getInputStream());
            }
            catch (IOException | SQLException e)
            {
                getLogger().error(String.format("Unable to run the SQL script for file at location: %s.\nAll pendings script executions are aborted.", location), e);
                return;
            }
            finally
            {
                if (source != null)
                {
                    _sourceResolver.release(source);
                }
            }
        }
    }
    
    private boolean _tableExists(Connection connection, String tableName) throws SQLException
    {
        ResultSet rs = null;
        boolean schemaExists = false;
        
        String name = tableName;
        DatabaseMetaData metaData = connection.getMetaData();
        
        if (metaData.storesLowerCaseIdentifiers())
        {
            name = tableName.toLowerCase();
        }
        else if (metaData.storesUpperCaseIdentifiers())
        {
            name = tableName.toUpperCase();
        }
        
        try
        {
            rs = metaData.getTables(null, null, name, null);
            schemaExists = rs.next();
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
        }
        
        return schemaExists;
    }
    
    private String _getDriverDelegateClass(DatabaseType dbType)
    {
        switch (dbType)
        {
            case DATABASE_DERBY:
            case DATABASE_MYSQL:
            case DATABASE_ORACLE:
                return "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
            case DATABASE_HSQLDB:
                return "org.quartz.impl.jdbcjobstore.HSQLDBDelegate";
            case DATABASE_POSTGRES:
                return "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";
            default:
                return "";
        }
    }
    
    /**
     * Starts the Quartz scheduler. Only call this method once !
     * @throws SchedulerException if an error occured
     */
    public void start() throws SchedulerException
    {
        _removeVolatileJobs();
        _scheduleConfigurableJobs();
        _triggerRunAtStartupJobs();
        
        getLogger().info("Start up the scheduler");
        _scheduler.start();
    }
    
    /**
     * Get the Quartz scheduler
     * @return the scheduler
     */
    public org.quartz.Scheduler getScheduler()
    {
        return _scheduler;
    }
    
    private void _removeVolatileJobs() throws SchedulerException
    {
        for (JobKey jobKey : getJobs())
        {
            JobDataMap jobDataMap = _scheduler.getJobDetail(jobKey).getJobDataMap();
            if (jobDataMap.getBoolean(KEY_RUNNABLE_VOLATILE))
            {
                _scheduler.deleteJob(jobKey);
            }
        }
    }
    
    private void _scheduleConfigurableJobs()
    {
        try
        {
            for (String runnableId : _runnableEP.getExtensionsIds())
            {
                JobKey jobKey = new JobKey(runnableId, JOB_GROUP);
                if (_scheduler.checkExists(jobKey))
                {
                    // Check if exists, but it should never do since the configurable are told volatile and were removed before
                    _scheduler.deleteJob(jobKey);
                }
                Runnable runnable = _runnableEP.getExtension(runnableId);
                
                scheduleJob(runnable);
            }
        }
        catch (SchedulerException e)
        {
            getLogger().error("An exception occured during the scheduling of configurable runnables", e);
        }
    }
    
    private void _triggerRunAtStartupJobs() throws SchedulerException
    {
        for (JobKey jobKey : getJobs())
        {
            JobDataMap jobDataMap = _scheduler.getJobDetail(jobKey).getJobDataMap();
            if (FireProcess.STARTUP.toString().equals(jobDataMap.getString(KEY_RUNNABLE_FIRE_PROCESS))
                    && !jobDataMap.getBoolean(KEY_RUNNABLE_STARTUP_COMPLETED))
            {
                _scheduler.triggerJob(jobKey);
            }
        }
    }
    
    /**
     * Schedules a job
     * @param runnable The runnable job to schedule
     * @throws SchedulerException if the Job or Trigger cannot be added to the Scheduler, or there is an internal Scheduler error.
     */
    public void scheduleJob(Runnable runnable) throws SchedulerException
    {
        String runnableId = runnable.getId();
        String schedulableId = runnable.getSchedulableId();
        
        // Schedule the job
        JobDetail jobDetail = null;
        JobBuilder jobBuilder = JobBuilder.newJob(AmetysJob.class)
                .withIdentity(runnableId, JOB_GROUP)
                .usingJobData(KEY_SCHEDULABLE_ID, schedulableId)
                .usingJobData(_runnableToJobDataMap(runnable));
        
        Date firstTime = null;
        switch (runnable.getFireProcess())
        {
            case STARTUP:
                // will fire at next startup
                jobDetail = jobBuilder.storeDurably().build();
                
                _scheduler.addJob(jobDetail, true);
                getLogger().info("{} has been scheduled to run at next startup of the application", jobDetail.getKey());
                break;
            case NOW:
                // will be triggered now
                jobDetail = jobBuilder.storeDurably().build();
                
                Trigger simpleTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(runnableId, TRIGGER_GROUP)
                        .startNow()
                        .build();
                
                firstTime = _scheduler.scheduleJob(jobDetail, simpleTrigger);
                getLogger().info("{} has been scheduled to run as soon as possible", jobDetail.getKey());
                break;
            case CRON:
            default:
                // based on a cron trigger
                jobDetail = jobBuilder.storeDurably().build();
                
                CronScheduleBuilder schedBuilder = CronScheduleBuilder.cronSchedule(runnable.getCronExpression());
                switch (runnable.getMisfirePolicy())
                {
                    case IGNORE:
                        schedBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                        break;
                    case FIRE_ONCE:
                        schedBuilder.withMisfireHandlingInstructionFireAndProceed();
                        break;
                    case DO_NOTHING:
                    default:
                        schedBuilder.withMisfireHandlingInstructionDoNothing();
                        break;
                }
                CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(runnableId, TRIGGER_GROUP)
                        .startNow()
                        .withSchedule(schedBuilder)
                        .build();
                
                firstTime = _scheduler.scheduleJob(jobDetail, cronTrigger);
                getLogger().info("{} has been scheduled to run at: {} and repeat based on expression: {}", jobDetail.getKey(), firstTime, cronTrigger.getCronExpression());
                break;
        }
    }
    
    private JobDataMap _runnableToJobDataMap(Runnable runnable)
    {
        Map<String, Object> result = new HashMap<>();
        result.put(KEY_RUNNABLE_ID, runnable.getId());
        result.put(KEY_RUNNABLE_LABEL, I18nizableText.i18nizableTextToString(runnable.getLabel()));
        result.put(KEY_RUNNABLE_DESCRIPTION, I18nizableText.i18nizableTextToString(runnable.getDescription()));
        result.put(KEY_RUNNABLE_FIRE_PROCESS, runnable.getFireProcess().toString());
        result.put(KEY_RUNNABLE_CRON, runnable.getCronExpression());
        result.put(KEY_RUNNABLE_REMOVABLE, runnable.isRemovable());
        result.put(KEY_RUNNABLE_MODIFIABLE, runnable.isModifiable());
        result.put(KEY_RUNNABLE_DEACTIVATABLE, runnable.isDeactivatable());
        result.put(KEY_RUNNABLE_VOLATILE, runnable.isVolatile());
        
        // parameter values
        for (Object paramId : runnable.getParameterValues().keySet())
        {
            result.put(PARAM_VALUES_PREFIX + paramId, runnable.getParameterValues().get(paramId));
        }
        
        return new JobDataMap(result);
    }
    
    /**
     * Gets the jobs of the Quartz scheduler
     * @return the jobs
     * @throws SchedulerException if an error occured
     */
    public Set<JobKey> getJobs() throws SchedulerException
    {
        return _scheduler.getJobKeys(GroupMatcher.jobGroupEquals(JOB_GROUP));
    }
    
    /**
     * Gets tasks information
     * @param taskIds The ids of the tasks
     * @return The tasks information
     * @throws SchedulerException if an error occured
     */
    @Callable
    public List<Map<String, Object>> getTasksInformation(List<String> taskIds) throws SchedulerException
    {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (JobKey jobKey : getJobs())
        {
            String id = jobKey.getName();
            if (taskIds.contains(id))
            {
                Map<String, Object> task = new HashMap<>();
                JobDataMap jobDataMap = _scheduler.getJobDetail(jobKey).getJobDataMap();
                task.put("id", id);
                task.put("modifiable", jobDataMap.getBoolean(KEY_RUNNABLE_MODIFIABLE));
                task.put("removable", jobDataMap.getBoolean(KEY_RUNNABLE_REMOVABLE));
                task.put("deactivatable", jobDataMap.getBoolean(KEY_RUNNABLE_DEACTIVATABLE));
                result.add(task);
            }
        }
        
        return result;
    }
    
    /**
     * Gets all the scheduled tasks
     * @return the scheduled tasks
     * @throws Exception if an error occured
     */
    public List<Map<String, Object>> getTasksAsJson() throws Exception
    {
        List<String> executingJobs = _scheduler.getCurrentlyExecutingJobs().stream()
                .map(JobExecutionContext::getJobDetail)
                .map(JobDetail::getKey)
                .map(JobKey::getName)
                .collect(Collectors.toList());
        
        return getJobs().stream()
                .map(LambdaUtils.wrap(key-> _jobToJson(key, executingJobs)))
                .collect(Collectors.toList());
    }
    
    private Map<String, Object> _jobToJson(JobKey jobKey, List<String> executingJobs) throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        
        JobDataMap jobDataMap = _scheduler.getJobDetail(jobKey).getJobDataMap();
        String id = jobDataMap.getString(KEY_RUNNABLE_ID);
        result.put("id", id);
        result.put("label", I18nizableText.stringToI18nizableText((String) jobDataMap.get(KEY_RUNNABLE_LABEL)));
        result.put("description", I18nizableText.stringToI18nizableText((String) jobDataMap.get(KEY_RUNNABLE_DESCRIPTION)));
        result.put("fireProcess", jobDataMap.getString(KEY_RUNNABLE_FIRE_PROCESS));
        result.put("removable", jobDataMap.getBoolean(KEY_RUNNABLE_REMOVABLE));
        result.put("modifiable", jobDataMap.getBoolean(KEY_RUNNABLE_MODIFIABLE));
        result.put("deactivatable", jobDataMap.getBoolean(KEY_RUNNABLE_DEACTIVATABLE));
        result.put("lastDuration", jobDataMap.get(AmetysJob.KEY_LAST_DURATION));
        result.put("success", jobDataMap.get(AmetysJob.KEY_SUCCESS));
        result.put("running", executingJobs.contains(id));
        
        String schedulableId = jobDataMap.getString(KEY_SCHEDULABLE_ID);
        if (_schedulableEP.hasExtension(schedulableId))
        {
            Schedulable schedulable = _schedulableEP.getExtension(schedulableId);
            result.put("schedulable", _schedulableToJson(schedulable));
        }
        
        TriggerKey triggerKey = new TriggerKey(jobKey.getName(), TRIGGER_GROUP);
        if (_scheduler.checkExists(triggerKey))
        {
            Trigger trigger = _scheduler.getTrigger(triggerKey);
            _triggerToJson(trigger, result);
            if (trigger instanceof CronTrigger)
            {
                result.put("cronExpression", ((CronTrigger) trigger).getCronExpression());
            }
            result.put("completed", false);
        }
        else
        {
            result.put("enabled", true);
            result.put("completed", !Runnable.FireProcess.STARTUP.toString().equals(jobDataMap.getString(KEY_RUNNABLE_FIRE_PROCESS)) || jobDataMap.getBoolean(KEY_RUNNABLE_STARTUP_COMPLETED));
        }
        result.put("previousFireTime", jobDataMap.get(AmetysJob.KEY_PREVIOUS_FIRE_TIME));
        
        return result;
    }
    
    private void _triggerToJson(Trigger trigger, Map<String, Object> result) throws Exception
    {
        result.put("enabled", !TriggerState.PAUSED.equals(_scheduler.getTriggerState(trigger.getKey())));
        result.put("nextFireTime", trigger.getNextFireTime());
    }
    
    private Map<String, Object> _schedulableToJson(Schedulable schedulable) throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        result.put("id", schedulable.getId());
        result.put("label", schedulable.getLabel());
        result.put("description", schedulable.getDescription());
        result.put("iconGlyph", schedulable.getIconGlyph());
        result.put("iconSmall", schedulable.getIconSmall());
        result.put("iconMedium", schedulable.getIconMedium());
        result.put("iconLarge", schedulable.getIconLarge());
        result.put("private", schedulable.isPrivate());
        
        Map<String, Object> params = new LinkedHashMap<>();
        for (String paramId : schedulable.getParameters().keySet())
        {
            params.put(schedulable.getId() + "$" + paramId, ParameterHelper.toJSON(schedulable.getParameters().get(paramId)));
        }
        result.put("parameters", params);
        
        return result;
    }
    
    /**
     * Gets the configuration for creating/editing a runnable (so returns all the schedulables and their parameters)
     * @return A map containing information about what is needed to create/edit a runnable
     * @throws Exception If an error occurs.
     */
    @Callable
    public Map<String, Object> getEditionConfiguration() throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> fireProcesses = new ArrayList<>();
        for (FireProcess fireProcessVal : FireProcess.values())
        {
            Map<String, Object> fireProcess = new LinkedHashMap<>();
            fireProcess.put("value", fireProcessVal.toString());
            String i18nKey = "PLUGINS_CORE_UI_TASKS_DIALOG_FIRE_PROCESS_OPTION_" + fireProcessVal.toString() + "_LABEL";
            fireProcess.put("label", new I18nizableText("plugin.core-ui", i18nKey));
            fireProcesses.add(fireProcess);
        }
        result.put("fireProcesses", fireProcesses);
        
        List<Object> schedulables = new ArrayList<>();
        for (String schedulableId : _schedulableEP.getExtensionsIds())
        {
            Schedulable schedulable = _schedulableEP.getExtension(schedulableId);
            if (!schedulable.isPrivate())
            {
                schedulables.add(_schedulableToJson(schedulable));
            }
        }
        result.put("schedulables", schedulables);
        
        return result;
    }
    
    /**
     * Gets the values of the parameters of the given task
     * @param id The id of the task
     * @return The values of the parameters
     */
    @Callable
    public Map<String, Object> getParameterValues(String id)
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            JobDetail jobDetail = _scheduler.getJobDetail(new JobKey(id, JOB_GROUP));
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            result.put("id", id);
            result.put("label", I18nizableText.stringToI18nizableText((String) jobDataMap.get(KEY_RUNNABLE_LABEL)));
            result.put("description", I18nizableText.stringToI18nizableText((String) jobDataMap.get(KEY_RUNNABLE_DESCRIPTION)));
            result.put("fireProcess", jobDataMap.getString(KEY_RUNNABLE_FIRE_PROCESS));
            result.put("cron", jobDataMap.getString(KEY_RUNNABLE_CRON));
            String schedulableId = jobDataMap.getString(KEY_SCHEDULABLE_ID);
            result.put("schedulableId", schedulableId);
            Map<String, Object> params = new HashMap<>();
            result.put("params", params);
            for (String param : jobDataMap.keySet())
            {
                if (param.startsWith(PARAM_VALUES_PREFIX))
                {
                    params.put(schedulableId + "$" + param.substring(PARAM_VALUES_PREFIX.length()), jobDataMap.get(param));
                }
            }
            return result;
        }
        catch (SchedulerException e)
        {
            getLogger().error("An error occured when trying to retrieve the parameter values of the task " + id, e);
            result.put("error", "scheduler-error");
            return result;
        }
    }
    
    /**
     * Returns true if the given task is modifiable.
     * @param id The id of the task
     * @return true if the given task is modifiable.
     * @throws SchedulerException if an error occured 
     */
    @Callable
    public Map<String, Object> isModifiable(String id) throws SchedulerException
    {
        Map<String, Object> result = new HashMap<>();
        
        JobDetail jobDetail = _scheduler.getJobDetail(new JobKey(id, JOB_GROUP));
        if (jobDetail == null)
        {
            // Does not exist anymore
            result.put("error", "not-found");
            return result;
        }
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        result.put("modifiable", jobDataMap.getBoolean(KEY_RUNNABLE_MODIFIABLE));
        return result;
    }
    
    /**
     * Adds a new task.
     * @param label The label
     * @param description The description
     * @param fireProcess the fire process
     * @param cron The cron expression
     * @param schedulableId The id of the schedulable model
     * @param params The values of the parameters
     * @return A result map
     * @throws SchedulerException if an error occured
     */
    @Callable
    public Map<String, Object> add(String label, String description, String fireProcess, String cron, String schedulableId, Map<String, String> params) throws SchedulerException
    {
        if (_rightsManager.hasRight(_currentUserProvider.getUser(), __RIGHT_SCHEDULER, "/application") != RightsManager.RightResult.RIGHT_OK)
        {
            // FIXME Currently unable to assign rights to a user in the _admin workspace
            // throw new RightsException("Insufficient rights to add a task");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        if (_schedulableEP.getExtension(schedulableId) == null)
        {
            result.put("error", "invalid-schedulable");
            return result;
        }
        else if (_schedulableEP.getExtension(schedulableId).isPrivate())
        {
            result.put("error", "private");
            return result;
        }
        
        String id = _generateUniqueId(label);
        return _edit(id, label, description, FireProcess.valueOf(fireProcess.toUpperCase()), cron, schedulableId, false, params);
    }
    
    private String _generateUniqueId(String label) throws SchedulerException
    {
        String value = label.toLowerCase().trim().replaceAll("[\\W_]", "-").replaceAll("-+", "-").replaceAll("^-", "");
        int i = 2;
        String suffixedValue = value;
        while (_scheduler.checkExists(new JobKey(suffixedValue, JOB_GROUP)))
        {
            suffixedValue = value + i;
            i++;
        }
        
        return suffixedValue;
    }
    
    /**
     * Edits the given task.
     * @param id The id of the task
     * @param label The label
     * @param description The description
     * @param fireProcess the fire process
     * @param cron The cron expression
     * @param params The values of the parameters
     * @return A result map
     * @throws SchedulerException if an error occured
     */
    @Callable
    public Map<String, Object> edit(String id, String label, String description, String fireProcess, String cron, Map<String, String> params) throws SchedulerException
    {
        if (_rightsManager.hasRight(_currentUserProvider.getUser(), __RIGHT_SCHEDULER, "/application") != RightsManager.RightResult.RIGHT_OK)
        {
            // FIXME Currently unable to assign rights to a user in the _admin workspace
            // throw new RightsException("Insufficient rights to edit a task");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        // Check if exist
        JobDetail jobDetail = null;
        JobKey jobKey = new JobKey(id, JOB_GROUP);
        jobDetail = _scheduler.getJobDetail(jobKey);
        
        // Check if modifiable
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        boolean isModifiable = jobDataMap.getBoolean(KEY_RUNNABLE_MODIFIABLE);
        if (!isModifiable)
        {
            result.put("error", "no-modifiable");
            return result;
        }
        
        // Remove the associated job as we will create another one
        if (!_scheduler.deleteJob(jobKey))
        {
            // Deletion did not succeed
            result.put("error", "not-found");
            return result;
        }
        
        String schedulableId = jobDataMap.getString(KEY_SCHEDULABLE_ID);
        boolean isVolatile = jobDataMap.getBoolean(KEY_RUNNABLE_VOLATILE);
        return _edit(id, label, description, FireProcess.valueOf(fireProcess.toUpperCase()), cron, schedulableId, isVolatile, params);
    }
    
    private Map<String, Object> _edit(String id, String label, String description, FireProcess fireProcess, String cron, String schedulableId, boolean isVolatile, Map<String, String> params)
    {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> typedParams = _getTypedParams(params, schedulableId);
        
        boolean deactivatable = !FireProcess.STARTUP.equals(fireProcess); // cannot disable a startup job as we do not attach any trigger to it
        Runnable runnable = new DefaultRunnable(id, new I18nizableText(label), new I18nizableText(description), fireProcess, cron, schedulableId, true, true, deactivatable, null, isVolatile, typedParams);
        
        try
        {
            scheduleJob(runnable);
        }
        catch (SchedulerException e)
        {
            getLogger().error("An error occured when trying to add/edit the task " + id, e);
            result.put("error", "scheduler-error");
            return result;
        }
        
        result.put("id", id);
        return result;
    }
    
    private Map<String, Object> _getTypedParams(Map<String, String> params, String schedulableId)
    {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Parameter<ParameterType>> declaredParams = _schedulableEP.getExtension(schedulableId).getParameters();
        for (String nameWithPrefix : params.keySet())
        {
            String[] splitStr = nameWithPrefix.split("\\$", 2);
            String prefix = splitStr[0];
            String paramName = splitStr[1];
            if (prefix.equals(schedulableId) && declaredParams.containsKey(paramName))
            {
                String originalValue = params.get(nameWithPrefix);
                
                Parameter<ParameterType> parameter = declaredParams.get(paramName);
                ParameterType type = parameter.getType();
                
                Object typedValue = ParameterHelper.castValue(originalValue, type);
                result.put(paramName, typedValue);
            }
            else if (prefix.equals(schedulableId))
            {
                getLogger().warn("The parameter {} is not declared in schedulable {}. It will be ignored", paramName, schedulableId);
            }
        }
        
        return result;
    }
    
    /**
     * Removes the given task.
     * @param id The id of the task
     * @return A result map
     */
    @Callable
    public Map<String, Object> remove(String id)
    {
        if (_rightsManager.hasRight(_currentUserProvider.getUser(), __RIGHT_SCHEDULER, "/application") != RightsManager.RightResult.RIGHT_OK)
        {
            // FIXME Currently unable to assign rights to a user in the _admin workspace
            // throw new RightsException("Insufficient rights to remove a task");
        }
        
        Map<String, Object> result = new HashMap<>();
        JobKey jobKey = new JobKey(id, JOB_GROUP);
        JobDetail jobDetail = null;
        try
        {
            jobDetail = _scheduler.getJobDetail(jobKey);
        }
        catch (SchedulerException e)
        {
            getLogger().error("An error occured when trying to remove the task " + id, e);
            result.put("error", "scheduler-error");
            return result;
        }
        if (jobDetail == null)
        {
            result.put("error", "not-found");
            return result;
        }
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        boolean isRemovable = jobDataMap.getBoolean(KEY_RUNNABLE_REMOVABLE);
        if (!isRemovable)
        {
            result.put("error", "no-removable");
            return result;
        }
        
        try
        {
            if (_scheduler.deleteJob(jobKey))
            {
                result.put("id", id);
                return result;
            }
            else
            {
                // Deletion did not succeed
                result.put("error", "no-delete");
                return result;
            }
        }
        catch (SchedulerException e)
        {
            getLogger().error("An error occured when trying to remove the task " + id, e);
            result.put("error", "scheduler-error");
            return result;
        }
    }
    
    /**
     * Enables/disables the given task.
     * @param id The id of the task
     * @param enabled true to enable the task, false to disable it.
     * @return A result map
     */
    @Callable
    public Map<String, Object> enable(String id, boolean enabled)
    {
        if (_rightsManager.hasRight(_currentUserProvider.getUser(), __RIGHT_SCHEDULER, "/application") != RightsManager.RightResult.RIGHT_OK)
        {
            // FIXME Currently unable to assign rights to a user in the _admin workspace
            // throw new RightsException("Insufficient rights to enable a task");
        }
        
        Map<String, Object> result = new HashMap<>();
        
        JobKey jobKey = new JobKey(id, JOB_GROUP);
        JobDetail jobDetail = null;
        try
        {
            jobDetail = _scheduler.getJobDetail(jobKey);
        }
        catch (SchedulerException e)
        {
            getLogger().error("An error occured when trying to enable/disable the task " + id, e);
            result.put("error", "scheduler-error");
            return result;
        }
        if (jobDetail == null)
        {
            result.put("error", "not-found");
            return result;
        }
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        boolean isDeactivatable = jobDataMap.getBoolean(KEY_RUNNABLE_DEACTIVATABLE);
        if (!isDeactivatable)
        {
            result.put("error", "no-deactivatable");
            return result;
        }
        
        try
        {
            if (enabled)
            {
                _scheduler.resumeJob(jobKey);
            }
            else
            {
                _scheduler.pauseJob(jobKey);
            }
        }
        catch (SchedulerException e)
        {
            getLogger().error("An error occured when trying to enable/disable the task " + id, e);
            result.put("error", "scheduler-error");
            return result;
        }
        
        result.put("id", id);
        return result;
    }
    
    /**
     * Removes the completed tasks
     * @return The information of deleted tasks
     * @throws SchedulerException if an error occured
     */
    @Callable
    public List<Map<String, Object>> removeCompletedTasks() throws SchedulerException
    {
        if (_rightsManager.hasRight(_currentUserProvider.getUser(), __RIGHT_SCHEDULER, "/application") != RightsManager.RightResult.RIGHT_OK)
        {
            // FIXME Currently unable to assign rights to a user in the _admin workspace
            // throw new RightsException("Insufficient rights to remove a task");
        }
        
        List<Map<String, Object>> targets = new ArrayList<>();
        
        List<JobKey> jobsToRemove = new ArrayList<>();
        for (JobKey jobKey : getJobs())
        {
            JobDetail jobDetail = _scheduler.getJobDetail(jobKey);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            if (!Runnable.FireProcess.STARTUP.toString().equals(jobDataMap.getString(KEY_RUNNABLE_FIRE_PROCESS)) && !_scheduler.checkExists(new TriggerKey(jobKey.getName(), TRIGGER_GROUP)) // no trigger left when CRON or NOW
                    || Runnable.FireProcess.STARTUP.toString().equals(jobDataMap.getString(KEY_RUNNABLE_FIRE_PROCESS)) && jobDataMap.getBoolean(KEY_RUNNABLE_STARTUP_COMPLETED)) // explicitly said completed when STARTUP
            {
                jobsToRemove.add(jobKey);
            }
        }
        
        targets.addAll(getTasksInformation(jobsToRemove.stream().map(JobKey::getName).collect(Collectors.toList())));
        for (JobKey jobKey : jobsToRemove)
        {
            _scheduler.deleteJob(jobKey);
        }
        
        return targets;
    }
    
    /**
     * Returns the enabled state of the task.
     * @param id The id of the task
     * @return A result map
     */
    @Callable
    public Map<String, Object> isEnabled(String id)
    {
        Map<String, Object> result = new HashMap<>();
        
        try
        {
            TriggerState state = _scheduler.getTriggerState(new TriggerKey(id, TRIGGER_GROUP));
            if (state == null)
            {
                // Does not exist anymore
                result.put("error", "not-found");
                return result;
            }
            result.put("enabled", !TriggerState.PAUSED.equals(state));
        }
        catch (SchedulerException e)
        {
            getLogger().error("An error occured when trying to retrive the enable state of the task " + id, e);
            result.put("error", "scheduler-error");
            return result;
        }
        return result;
    }

    @Override
    public void dispose()
    {
        try
        {
            _scheduler.shutdown();
            AmetysJob.dispose();
        }
        catch (SchedulerException e)
        {
            getLogger().error("Fail to shutdown scheduler", e);
        }
    }
}
