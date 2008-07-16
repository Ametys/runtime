insert into Users (login, firstname, lastname, email, password) values 
('test', 'Test', 'TEST', 'test@test.te', 'CY9rzUYh03PK3k6DJie09g=='),
('test2', 'Test2', 'TEST2', 'test2@test.te', 'CY9rzUYh03PK3k6DJie09g==');

insert into Groups (Label) values ('Group 1');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = last_insert_id()), 'test');

insert into Groups (Label) values ('Group 2');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = last_insert_id()), 'test'),
((SELECT Id FROM Groups WHERE Id = last_insert_id()), 'test2');

insert into Groups (Label) values ('Group 3');
insert into Groups_Users (Group_Id, Login) values 
((SELECT Id FROM Groups WHERE Id = last_insert_id()), 'test2');

insert into Groups (Label) values ('Group 4');
