insert into Users (login, firstname, lastname, email) values 
('test', 'Test', 'TEST', 'test@test.te'),
('test2', 'Test2', 'TEST2', 'test2@test.te');

insert into Groups (Label) values ('Group 1');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = IDENTITY_VAL_LOCAL()), 'test');

insert into Groups (Label) values ('Group 2');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = IDENTITY_VAL_LOCAL()), 'test'),
((SELECT Id FROM Groups WHERE Id = IDENTITY_VAL_LOCAL()), 'test2');

insert into Groups (Label) values ('Group 3');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = IDENTITY_VAL_LOCAL()), 'test2');

insert into Groups (Label) values ('Group 4');
