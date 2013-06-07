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

insert into Users (login, firstname, lastname, email) values 
('test', 'Test', 'TEST', 'test@test.te');
insert into Users (login, firstname, lastname, email) values 
('test2', 'Test2', 'TEST2', 'test2@test.te');

insert into Groups (Id, Label) values (seq_groups.nextval, 'Group 1');
insert into Groups_Users (Group_Id, Login) values 
(seq_groups.currval, 'test');

insert into Groups (Id, Label) values (seq_groups.nextval, 'Group 2');
insert into Groups_Users (Group_Id, Login) values 
(seq_groups.currval, 'test');
insert into Groups_Users (Group_Id, Login) values 
(seq_groups.currval, 'test2');

insert into Groups (Id, Label) values (seq_groups.nextval, 'Group 3');
insert into Groups_Users (Group_Id, Login) values 
(seq_groups.currval, 'test2');

insert into Groups (Id, Label) values (seq_groups.nextval, 'Group 4');
