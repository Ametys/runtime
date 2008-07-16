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

DROP TABLE IF EXISTS Groups ;
DROP TABLE IF EXISTS Groups_Users;

CREATE TABLE Groups(
  Id SERIAL PRIMARY KEY, 
  Label VARCHAR(200));
  
CREATE TABLE Groups_Users(
  Group_Id VARCHAR(200) NOT NULL, 
  Login VARCHAR (200) NOT NULL, 
  PRIMARY KEY (Group_Id, Login));
COMMIT;