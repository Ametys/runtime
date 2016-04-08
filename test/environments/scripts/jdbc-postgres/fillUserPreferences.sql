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

insert into UserPreferences(login, population, context, data) values 
('user', 'population', '/empty', '<?xml version="1.0" encoding="UTF-8"?><UserPreferences></UserPreferences>'),
('user', 'population', '/one', '<?xml version="1.0" encoding="UTF-8"?><UserPreferences><pref1>one</pref1></UserPreferences>'),
('user', 'population', '/two', '<?xml version="1.0" encoding="UTF-8"?><UserPreferences><pref1>one</pref1><pref2>two</pref2></UserPreferences>'),
('user', 'population', '/all', '<?xml version="1.0" encoding="UTF-8"?>
<UserPreferences>
<pref1>one</pref1>
<pref2>two</pref2>
<long>27</long>
<double>3.14</double>
<date>1987-10-09T00:00:00.000+02:00</date>
<boolean>true</boolean>
</UserPreferences>');