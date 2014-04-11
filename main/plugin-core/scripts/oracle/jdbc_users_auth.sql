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
CREATE TABLE Users 
(
	login varchar(64) NOT NULL,
  	firstname varchar(64) default NULL,
  	lastname varchar(64) NOT NULL,
  	email varchar(64) DEFAULT NULL,
  	password varchar(128)  NOT NULL,
  	salt varchar(128)  DEFAULT NULL,
  	PRIMARY KEY (login)
);