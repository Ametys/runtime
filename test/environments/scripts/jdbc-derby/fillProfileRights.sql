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

delete from Users;
insert into Users (login, firstname, lastname, email) values 
('test', 'Test', 'TEST', 'test@test.te'),
('test2', 'Test2', 'TEST2', 'test2@test.te');

delete from Groups;
insert into Groups (Label) values ('Group 1');
insert into Groups_Users (Group_Id, Login, UserPopulation_Id) values 
(IDENTITY_VAL_LOCAL(), 'test', 'population');

insert into Rights_Profile(Label) values ('Profil 1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
(IDENTITY_VAL_LOCAL(), 'right1'),
(IDENTITY_VAL_LOCAL(), 'right2');
insert into Rights_UserRights (Profile_Id, Login, UserPopulation_Id, Context) values
(IDENTITY_VAL_LOCAL(), 'test', 'population', '/application/test'),
(IDENTITY_VAL_LOCAL(), 'test', 'population', '/application/test2/test2');
insert into Rights_GroupRights (Profile_Id, Group_Id, GroupDirectory_Id, Context) values
((select max(Id) from Rights_Profile), TRIM(CAST(CAST((select max(Id) from Groups) AS CHAR(200)) AS VARCHAR(200))), 'sql_group_directory', '/application/test3');

insert into Rights_Profile(Label) values ('Profil 2');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
(IDENTITY_VAL_LOCAL(), 'right3');
insert into Rights_UserRights (Profile_Id, Login, UserPopulation_Id, Context) values
((select max(Id) from Rights_Profile), 'test2', 'population', '/application/test2/test2'),
((select max(Id) from Rights_Profile), 'test', 'population', '/application/test3');

