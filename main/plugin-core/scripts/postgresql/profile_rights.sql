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
BEGIN;

DROP TABLE IF EXISTS Rights_Profile;
DROP TABLE IF EXISTS Rights_ProfileRights;
DROP TABLE IF EXISTS Rights_GroupRights;
DROP TABLE IF EXISTS Rights_UserRights;

CREATE TABLE Rights_Profile(
Id SERIAL PRIMARY KEY , 
Label VARCHAR(200));


CREATE TABLE Rights_ProfileRights(
Profile_Id VARCHAR(200) NOT NULL, 
Right_Id VARCHAR(200) NOT NULL, 
PRIMARY KEY(Profile_Id, Right_Id));

CREATE TABLE Rights_GroupRights(
Profile_Id VARCHAR(200) NOT NULL, 
Group_Id VARCHAR(200) NOT NULL, 
Context VARCHAR(200) NOT NULL, 
PRIMARY KEY(Profile_Id, Group_Id, Context));

CREATE TABLE Rights_UserRights(
Profile_Id VARCHAR(200) NOT NULL, 
Login VARCHAR(200) NOT NULL, 
Context VARCHAR(200) NOT NULL, 
PRIMARY KEY(Profile_Id, Login, Context));

COMMIT;