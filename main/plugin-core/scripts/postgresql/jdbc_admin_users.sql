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

DROP TABLE IF EXISTS AdminUsers;

CREATE TABLE AdminUsers (
  login varchar(64) PRIMARY KEY NOT NULL,
  firstname varchar(64) default NULL,
  lastname varchar(64) NOT NULL,
  email varchar(64) DEFAULT NULL,
  password varchar(128) NOT NULL,
  salt varchar(128) DEFAULT NULL
);
INSERT INTO AdminUsers (login, firstname, lastname, password, salt) VALUES ('admin', 'User', 'Administrator', '67ba4bf3d6c7baeda1e9e42f958d4765713ab5444fb708d53a12dde60b81e04423d0c5399c03cd2c013304daa091de8aff7d659f80f0e3818c0af2d3626e4e6d', 'OUX2PB7esaVkRcww6WBIbMoZVTkEO8E7w0ok0Dssmv9gNL9R');

COMMIT;
