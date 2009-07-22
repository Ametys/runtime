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
CREATE TABLE Groups
(
	Id number NOT NULL, 
	Label VARCHAR(200),
	PRIMARY KEY (Id)
);

CREATE TABLE Groups_Users
(
	Group_Id number NOT NULL, 
	Login VARCHAR (200) NOT NULL, 
	PRIMARY KEY (Group_Id, Login)
);

create sequence seq_groups;
