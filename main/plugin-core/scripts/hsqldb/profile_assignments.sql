--
--  Copyright 2013 Anyware Services
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
CREATE CACHED TABLE Rights_AllowedProfilesAnyCon (
        Profile_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Context)
);
CREATE CACHED TABLE Rights_DeniedProfilesAnyCon (
        Profile_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Context)
);
CREATE CACHED TABLE Rights_AllowedProfilesAnonym (
        Profile_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Context)
);
CREATE CACHED TABLE Rights_DeniedProfilesAnonym (
        Profile_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Context)
);

CREATE CACHED TABLE Rights_AllowedGroups (
        Profile_Id VARCHAR(200) NOT NULL,
        Group_Id VARCHAR(200) NOT NULL,
		GroupDirectory_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Group_Id, GroupDirectory_Id, Context)
);

CREATE CACHED TABLE Rights_DeniedGroups (
        Profile_Id VARCHAR(200) NOT NULL,
        Group_Id VARCHAR(200) NOT NULL,
		GroupDirectory_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Group_Id, GroupDirectory_Id, Context)
);

CREATE CACHED TABLE Rights_AllowedUsers (
        Profile_Id VARCHAR(200) NOT NULL,
        Login VARCHAR(200) NOT NULL,
		UserPopulation_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Login, UserPopulation_Id, Context)
);

CREATE CACHED TABLE Rights_DeniedUsers (
        Profile_Id VARCHAR(200) NOT NULL,
        Login VARCHAR(200) NOT NULL,
		UserPopulation_Id VARCHAR(200) NOT NULL,
        Context VARCHAR(200) NOT NULL,
        PRIMARY KEY(Profile_Id, Login, UserPopulation_Id, Context)
);
