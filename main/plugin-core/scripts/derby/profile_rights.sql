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
CREATE TABLE Rights_Profile(
	Id int PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), 
	Label VARCHAR(200),
	Context VARCHAR(200)
);

CREATE TABLE Rights_ProfileRights(
	Profile_Id int NOT NULL, 
	Right_Id VARCHAR(200) NOT NULL, 
	PRIMARY KEY(Profile_Id, Right_Id)
);

CREATE TABLE Rights_GroupRights(
	Profile_Id int NOT NULL, 
	Group_Id VARCHAR(200) NOT NULL,
	GroupDirectory_Id VARCHAR(200) NOT NULL,
	Context VARCHAR(200) NOT NULL, 
	PRIMARY KEY(Profile_Id, Group_Id, GroupDirectory_Id, Context)
);

CREATE TABLE Rights_UserRights(
	Profile_Id int NOT NULL, 
	Login VARCHAR(200) NOT NULL, 
	UserPopulation_Id VARCHAR(200) NOT NULL, 
	Context VARCHAR(200) NOT NULL, 
	PRIMARY KEY(Profile_Id, Login, UserPopulation_Id, Context)
);
