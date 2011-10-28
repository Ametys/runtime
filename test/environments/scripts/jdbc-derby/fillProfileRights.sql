delete from Users;
insert into Users (login, firstname, lastname, email) values 
('test', 'Test', 'TEST', 'test@test.te'),
('test2', 'Test2', 'TEST2', 'test2@test.te');

delete from Groups;
insert into Groups (Label) values ('Group 1');
insert into Groups_Users (Group_Id, Login) values 
(IDENTITY_VAL_LOCAL(), 'test');

insert into Rights_Profile(Label) values ('Profil 1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
(IDENTITY_VAL_LOCAL(), 'right1'),
(IDENTITY_VAL_LOCAL(), 'right2');
insert into Rights_UserRights (Profile_Id, Login, Context) values
(IDENTITY_VAL_LOCAL(), 'test', '/application/test'),
(IDENTITY_VAL_LOCAL(), 'test', '/application/test2/test2');
insert into Rights_GroupRights (Profile_Id, Group_Id, Context) values
((select max(Id) from Rights_Profile), TRIM(CAST(CAST((select max(Id) from Groups) AS CHAR(200)) AS VARCHAR(200))), '/application/test3');

insert into Rights_Profile(Label) values ('Profil 2');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values 
(IDENTITY_VAL_LOCAL(), 'right3');
insert into Rights_UserRights (Profile_Id, Login, Context) values
((select max(Id) from Rights_Profile), 'test2', '/application/test2/test2'),
((select max(Id) from Rights_Profile), 'test', '/application/test3');

