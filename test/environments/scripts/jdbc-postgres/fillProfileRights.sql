delete from Users;
insert into Users (login, firstname, lastname, email) values 
('test', 'Test', 'TEST', 'test@test.te'),
('test2', 'Test2', 'TEST2', 'test2@test.te');

delete from Groups;
insert into Groups (Label) values ('Group 1');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = currval('groups_id_seq')), 'test');

insert into Rights_Profile(Id, Label) values(nextval('seq_rights_profile'), 'Profil 1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
(currval('seq_rights_profile'), 'right1'),
(currval('seq_rights_profile'), 'right2');
insert into Rights_UserRights (Profile_Id, Login, Context) values
(currval('seq_rights_profile'), 'test', '/application/test'),
(currval('seq_rights_profile'), 'test', '/application/test2/test2');
insert into Rights_GroupRights (Profile_Id, Group_Id, Context) values
((SELECT Id FROM Rights_Profile WHERE Id in (select max(Id) from Rights_Profile)), (SELECT Id FROM Groups WHERE Id in (select max(Id) from Groups)), '/application/test3');

insert into Rights_Profile(Id, Label) values(nextval('seq_rights_profile'), 'Profil 2');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
(currval('seq_rights_profile'), 'right3');
insert into Rights_UserRights (Profile_Id, Login, Context) values
((SELECT Id FROM Rights_Profile WHERE Id in (select max(Id) from Rights_Profile)), 'test2', '/application/test2/test2'),
((SELECT Id FROM Rights_Profile WHERE Id in (select max(Id) from Rights_Profile)), 'test', '/application/test3');

