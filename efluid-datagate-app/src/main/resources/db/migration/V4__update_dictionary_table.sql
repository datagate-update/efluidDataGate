ALTER TABLE DICTIONARY
ADD SELECT_CLAUSE_NEW CLOB;
UPDATE DICTIONARY SET SELECT_CLAUSE_NEW = SELECT_CLAUSE;
ALTER TABLE DICTIONARY DROP COLUMN SELECT_CLAUSE;
ALTER TABLE DICTIONARY RENAME COLUMN SELECT_CLAUSE_NEW TO SELECT_CLAUSE;