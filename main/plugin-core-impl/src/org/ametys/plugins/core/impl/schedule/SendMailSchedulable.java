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

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import org.ametys.core.schedule.Schedulable;
import org.ametys.core.util.mail.SendMailHelper;
import org.ametys.plugins.core.schedule.Scheduler;

/**
 * A {@link Schedulable} job for sending emails.
 */
public class SendMailSchedulable extends AbstractStaticSchedulable
{
    /** The key for the sender of the email */
    public static final String SENDER_KEY = "sender";
    /** The key for the recipients of the email */
    public static final String RECIPIENTS_KEY = "recipients";
    /** The key for the subject of the email */
    public static final String SUBJECT_KEY = "subject";
    /** The key for the body of the email */
    public static final String BODY_KEY = "body";
    
    @Override
    public void execute(JobExecutionContext context) throws Exception
    {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String sender = (String) jobDataMap.get(Scheduler.PARAM_VALUES_PREFIX + SENDER_KEY);
        String recipients = (String) jobDataMap.get(Scheduler.PARAM_VALUES_PREFIX + RECIPIENTS_KEY);
        String subject = (String) jobDataMap.get(Scheduler.PARAM_VALUES_PREFIX + SUBJECT_KEY);
        String body = (String) jobDataMap.get(Scheduler.PARAM_VALUES_PREFIX + BODY_KEY);

        StringBuilder builder = new StringBuilder(); // the builder for the addresses separated by a space
        for (String recipient : recipients.split("\\n"))
        {
            if (builder.length() != 0)
            {
                builder.append(" ");
            }
            builder.append(recipient.trim());
        }
        
        SendMailHelper.sendMail(subject, body, null, builder.toString(), sender);
    }
}
