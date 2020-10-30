-- More indexes for DB load of regenerated content and used FK
CREATE INDEX IDX_INDEXES_TIMESTAMP ON INDEXES(TIMESTAMP);
CREATE INDEX IDX_INDEXES_DICT_ENTRY ON INDEXES(DICTIONARY_ENTRY_UUID);
CREATE INDEX IDX_INDEXES_COMMIT ON INDEXES(COMMIT_UUID);
CREATE INDEX IDX_LOBS_COMMIT ON LOBS(COMMIT_UUID);