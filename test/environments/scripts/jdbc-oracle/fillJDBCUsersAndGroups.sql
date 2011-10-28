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
