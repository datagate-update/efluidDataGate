-- Restore configuration for efluid to default

UPDATE "TRANSFORMERS" SET "CONFIGURATION" = '{"tablePattern":".*","appliedKeyPatterns":["TRA$.*"],"appliedValueFilterPatterns":{"ETATOBJET":"0"},"dateUpdates":{"DATESUPPRESSION":"current_date","DATEMODIFICATION":"current_date","DATECREATION":"current_date"},"actorUpdates":{"ACTEURMODIFICATION":"evt 154654","ACTEURCREATION":"evt 156444","ACTEURSUPPRESSION":"evt 189445"}}' WHERE "TYPE" = 'EfluidAuditDataTransformer';