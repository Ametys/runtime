insert into UserPreferences(login, context, data) values 
('user', '/empty', '<?xml version="1.0" encoding="UTF-8"?><UserPreferences></UserPreferences>'),
('user', '/one', '<?xml version="1.0" encoding="UTF-8"?><UserPreferences><pref1>one</pref1></UserPreferences>'),
('user', '/two', '<?xml version="1.0" encoding="UTF-8"?><UserPreferences><pref1>one</pref1><pref2>two</pref2></UserPreferences>'),
('user', '/all', '<?xml version="1.0" encoding="UTF-8"?>
<UserPreferences>
<pref1>one</pref1>
<pref2>two</pref2>
<long>27</long>
<double>3.14</double>
<date>1987-10-09T00:00:00.000+02:00</date>
<boolean>true</boolean>
</UserPreferences>');