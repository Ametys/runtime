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
drop table if exists Users;
CREATE TABLE Users (
  login varchar(32) PRIMARY KEY NOT NULL,
  firstname varchar(64) default NULL,
  lastname varchar(64) NOT NULL,
  email varchar(64)  NOT NULL,
  password varchar(128)  NOT NULL
)ENGINE=innodb;