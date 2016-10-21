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
insert into Users (login, firstname, lastname, email, password, salt) values 
('test', 'Test', 'TEST', 'test@test.te', 'CY9rzUYh03PK3k6DJie09g==', null);
insert into Users (login, firstname, lastname, email, password, salt) values 
('test2', 'Test2', 'TEST2', 'test2@test.te', '6151b762d08841411cf52184a2538cddaf3dc221914eda9198c236ec37a9f6a929f613b16a5c4bccfa483e7376f241212bf040a557addc5ef2adea9dc8c6f5e7', 'Eyp1pUMWKO9PKy67vYaEUezy2Zu2DQF72RG0qakzvoSgSX6r');

delete from Groups;
insert into Groups (Id, Label) values (seq_groups.nextval, 'Group 1');
insert into Groups_Users (Group_Id, Login, UserPopulation_Id) values (seq_groups.currval, 'test', 'population');

insert into Rights_Profile(Id, Label) values ('Profil1', 'Profil 1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values ('Profil1', 'right1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values ('Profil1', 'right2');

insert into Rights_AllowedUsers (Profile_Id, Login, UserPopulation_Id, Context) values ('Profil1', 'test', 'population', '/test');

insert into Rights_Profile(Id, Label) values ('Profil2', 'Profil 2');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values ('Profil2', 'right3');
