--
--  Copyright 2009 Anyware Services
--
--  Licensed under the Apache License, Version 2.0 (the "License");
--  you may not use this file except in compliance with the License.
--  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.
--

--_separator_=$$$
DECLARE
    CURSOR tables is SELECT table_name FROM user_tables where table_name IN ('RIGHTS_PROFILE', 'RIGHTS_PROFILERIGHTS', 'RIGHTS_ALLOWEDGROUPS', 'RIGHTS_ALLOWEDUSERS', 'RIGHTS_DENIEDGROUPS', 'RIGHTS_DENIEDUSERS', 'RIGHTS_ALLOWEDPROFILESANYCON', 'RIGHTS_DENIEDPROFILESANYCON', 'RIGHTS_ALLOWEDPROFILESANONYM', 'RIGHTS_DENIEDPROFILESANONYM', 'USERPOPULATIONSBYCONTEXT', 'GROUPDIRECTORIESBYCONTEXT', 'ADMINUSERS', 'GROUPS_USERS', 'GROUPS', 'USERS', 'USERPREFERENCES');
    CURSOR sequences is SELECT sequence_name FROM user_sequences where sequence_name IN ('SEQ_GROUPS', 'SEQ_RIGHTS_PROFILE', 'SEQ_PROFILE_ASSIGNMENTS');
BEGIN
	-- PURGE works only as of Oracle 10g Release 2.
    FOR TAB IN tables LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || TAB.TABLE_NAME || ' PURGE';
    END LOOP;
    
    FOR seq IN sequences LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || seq.sequence_name;
    END LOOP;
END;
--_separator_=;
