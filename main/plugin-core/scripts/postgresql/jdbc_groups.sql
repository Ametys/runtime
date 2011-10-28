--
--  Copyright 2009 Anyware Services
--
--  Licensed under the Apache License, Version 2.0 (the "License");
--  you may not use this file except in compliance with the License.
--  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.
--
BEGIN;

DROP TABLE IF EXISTS Groups ;
DROP TABLE IF EXISTS Groups_Users;

CREATE TABLE Groups(
  Id SERIAL PRIMARY KEY, 
  Label VARCHAR(200));
  
CREATE TABLE Groups_Users(
/*  Group_Id VARCHAR(200) NOT NULL,*/ 
  Group_Id int NOT NULL, 
  Login VARCHAR (200) NOT NULL, 
  PRIMARY KEY (Group_Id, Login));
COMMIT;
