-- permission config for database tables in PostgreSQL.

-- rule of thumb: 
--  all tables granted select only should reside
--  all tables granted select, insert, update, delete should eventually be moved into publication sara DB!

GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_metadata TO test;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_actions TO test;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_licenses TO test;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_pubmeta TO test;

GRANT SELECT ON supported_licenses TO test;
GRANT SELECT ON source TO test;
GRANT SELECT ON source_params TO test;
GRANT SELECT ON repository TO test;
GRANT SELECT ON repository_params TO test;
GRANT SELECT ON archive TO test;
GRANT SELECT ON archive_params TO test;
GRANT SELECT ON metadatamapping TO test;

GRANT SELECT, INSERT, UPDATE ON item TO test;
-- GRANT SELECT, INSERT, UPDATE ON metadatavalue TO test;
