delete from Users;
insert into Users (login, firstname, lastname, email) values 
('test', 'Test', 'TEST', 'test@test.te'),
('test2', 'Test2', 'TEST2', 'test2@test.te');

delete from Groups;
insert into Groups (Label) values ('Group 1');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = last_insert_id()), 'test');

insert into Rights_Profile(Label) value('Profil 1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
((SELECT Id FROM Rights_Profile WHERE Id = last_insert_id()), 'right1'),
((SELECT Id FROM Rights_Profile WHERE Id = last_insert_id()), 'right2');
insert into Rights_UserRights (Profile_Id, Login, Context) values
((SELECT Id FROM Rights_Profile WHERE Id = last_insert_id()), 'test', '/application/test'),
((SELECT Id FROM Rights_Profile WHERE Id = last_insert_id()), 'test', '/application/test2/test2');
insert into Rights_GroupRights (Profile_Id, Group_Id, Context) values
((SELECT Id FROM Rights_Profile WHERE Id in (select max(Id) from Rights_Profile)), (SELECT Id FROM Groups WHERE Id in (select max(Id) from Groups)), '/application/test3');

insert into Rights_Profile(Label) value('Profil 2');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
((SELECT Id FROM Rights_Profile WHERE Id = last_insert_id()), 'right3');
insert into Rights_UserRights (Profile_Id, Login, Context) values
((SELECT Id FROM Rights_Profile WHERE Id in (select max(Id) from Rights_Profile)), 'test2', '/application/test2/test2'),
((SELECT Id FROM Rights_Profile WHERE Id in (select max(Id) from Rights_Profile)), 'test', '/application/test3');

