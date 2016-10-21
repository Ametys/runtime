--
--  Copyright 2016 Anyware Services
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

--_ignore_exceptions_=on
DROP TABLE Rights_ProfileRights;

DROP TABLE Rights_AllowedGroups;
DROP TABLE Rights_AllowedUsers;
DROP TABLE Rights_DeniedGroups;
DROP TABLE Rights_DeniedUsers;
DROP TABLE Rights_AllowedProfilesAnyCon;
DROP TABLE Rights_DeniedProfilesAnyCon;
DROP TABLE Rights_AllowedProfilesAnonym;
DROP TABLE Rights_DeniedProfilesAnonym;

DROP TABLE Rights_Profile;

DROP TABLE UserPopulationsByContext;
DROP TABLE GroupDirectoriesByContext;

DROP TABLE AdminUsers;
DROP TABLE Groups_Users;
DROP TABLE Groups;
DROP TABLE Users;

DROP TABLE UserPreferences;

DROP SEQUENCE seq_groups;
DROP SEQUENCE seq_rights_profile;
DROP SEQUENCE seq_profile_assignments;
