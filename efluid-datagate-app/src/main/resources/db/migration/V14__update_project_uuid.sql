UPDATE APPLY_HISTORY
SET PROJECT_UUID = (SELECT UUID FROM PROJECTS WHERE ROWNUM = 1)
WHERE PROJECT_UUID IS NULL;