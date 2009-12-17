/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
drop table if exists Rights_Profile;
CREATE TABLE Rights_Profile(
Id int PRIMARY KEY NOT NULL auto_increment, 
Label VARCHAR(200)
)ENGINE=innodb;

drop table if exists Rights_ProfileRights;
CREATE TABLE Rights_ProfileRights(
Profile_Id int NOT NULL, 
Right_Id VARCHAR(200) NOT NULL, 
PRIMARY KEY(Profile_Id, Right_Id)
)ENGINE=innodb;

drop table if exists Rights_GroupRights;
CREATE TABLE Rights_GroupRights(
Profile_Id int NOT NULL, 
Group_Id VARCHAR(200) NOT NULL, 
Context VARCHAR(200) NOT NULL, 
PRIMARY KEY(Profile_Id, Group_Id, Context)
)ENGINE=innodb;

drop table if exists Rights_UserRights;
CREATE TABLE Rights_UserRights(
Profile_Id int NOT NULL, 
Login VARCHAR(200) NOT NULL, 
Context VARCHAR(200) NOT NULL, 
PRIMARY KEY(Profile_Id, Login, Context)
)ENGINE=innodb;
