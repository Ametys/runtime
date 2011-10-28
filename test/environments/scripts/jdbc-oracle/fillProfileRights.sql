delete from Users;
insert into Users (login, firstname, lastname, email) values 
('test', 'Test', 'TEST', 'test@test.te');
insert into Users (login, firstname, lastname, email) values 
('test2', 'Test2', 'TEST2', 'test2@test.te');

delete from Groups;
insert into Groups (Id, Label) values (seq_groups.nextval, 'Group 1');
insert into Groups_Users (Group_Id, Login) values (seq_groups.currval, 'test');

insert into Rights_Profile(Id, Label) values (seq_rights_profile.nextval, 'Profil 1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values (seq_rights_profile.currval, 'right1');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values (seq_rights_profile.currval, 'right2');

insert into Rights_UserRights (Profile_Id, Login, Context) values (seq_rights_profile.currval, 'test', '/application/test');
insert into Rights_UserRights (Profile_Id, Login, Context) values (seq_rights_profile.currval, 'test', '/application/test2/test2');
insert into Rights_GroupRights (Profile_Id, Group_Id, Context) values
((select max(Id) from Rights_Profile), (select max(Id) from Groups), '/application/test3');

insert into Rights_Profile(Id, Label) values (seq_rights_profile.nextval, 'Profil 2');
insert into Rights_ProfileRights (Profile_Id, Right_Id) values (seq_rights_profile.currval, 'right3');
insert into Rights_UserRights (Profile_Id, Login, Context) values
((select max(Id) from Rights_Profile), 'test2', '/application/test2/test2');
insert into Rights_UserRights (Profile_Id, Login, Context) values
((select max(Id) from Rights_Profile), 'test', '/application/test3');
