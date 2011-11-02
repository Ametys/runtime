--_separator_=$$$
DECLARE
    CURSOR tables is SELECT table_name FROM user_tables where table_name IN ('RIGHTS_PROFILE', 'RIGHTS_PROFILERIGHTS', 'RIGHTS_GROUPRIGHTS', 'RIGHTS_USERRIGHTS', 'GROUPS_USERS', 'GROUPS', 'USERS', 'USERPREFERENCES');
    CURSOR sequences is SELECT sequence_name FROM user_sequences where sequence_name IN ('SEQ_GROUPS', 'SEQ_RIGHTS_PROFILE');
BEGIN
	-- PURGE works only as of Oracle 10g Release 2.
    FOR TAB IN tables LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || TAB.TABLE_NAME || ' PURGE';
    END LOOP;
    
    FOR seq IN sequences LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || seq.sequence_name;
    END LOOP;
END;
--_separator_=;
