-- permission config for database tables in PostgreSQL.

-- rule of thumb: 
--  all tables granted select only should reside
--  all tables granted select, insert, update, delete should eventually be moved into publication sara DB!

GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_metadata TO __USERNAME__;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_actions TO __USERNAME__;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_licenses TO __USERNAME__;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_pubmeta TO __USERNAME__;

GRANT SELECT ON supported_licenses TO __USERNAME__;
GRANT SELECT ON source TO __USERNAME__;
GRANT SELECT ON source_params TO __USERNAME__;
GRANT SELECT ON repository TO __USERNAME__;
GRANT SELECT ON repository_params TO __USERNAME__;
GRANT SELECT ON archive TO __USERNAME__;
GRANT SELECT ON archive_params TO __USERNAME__;
GRANT SELECT ON metadatamapping TO __USERNAME__;
GRANT SELECT, INSERT ON metadatavalue TO __USERNAME__;

GRANT SELECT, INSERT, UPDATE ON item TO __USERNAME__;
-- GRANT SELECT, INSERT, UPDATE ON metadatavalue TO __USERNAME__;
