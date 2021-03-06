-- permission config for database tables in PostgreSQL.

GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_metadata TO __USERNAME__;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_authors TO __USERNAME__;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_actions TO __USERNAME__;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_licenses TO __USERNAME__;
GRANT SELECT, INSERT, UPDATE, DELETE ON fe_temp_archive TO __USERNAME__;

GRANT SELECT ON supported_licenses TO __USERNAME__;
GRANT SELECT ON source TO __USERNAME__;
GRANT SELECT ON source_params TO __USERNAME__;
GRANT SELECT ON repository TO __USERNAME__;
GRANT SELECT ON repository_params TO __USERNAME__;
GRANT SELECT ON archive TO __USERNAME__;
GRANT SELECT ON archive_params TO __USERNAME__;
GRANT SELECT ON metadatamapping TO __USERNAME__;
GRANT SELECT, INSERT ON metadatavalue TO __USERNAME__;

-- note: no UPDATE or DELETE on item and item_authors! they are append-only
GRANT SELECT, INSERT ON item TO __USERNAME__;
GRANT SELECT, INSERT ON item_authors TO __USERNAME__;
GRANT SELECT, INSERT, UPDATE ON item_publication TO __USERNAME__;
