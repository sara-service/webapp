CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE testarchiv_privkey oid := lo_import(base_dir || '/credentials/testarchiv/test.key');
DECLARE testarchiv_pubkey oid  := lo_import(base_dir || '/credentials/testarchiv/test.key.pub');
DECLARE testarchiv_known oid   := lo_import(base_dir || '/credentials/testarchiv/known_hosts');
DECLARE license_html oid       := lo_import(base_dir || '/test/license.html');

DECLARE testarchiv text     := 'testarchiv.sara-service.org';

DECLARE aRef UUID;

BEGIN

-- "Testarchiv" default git archive
INSERT INTO archive(display_name, contact_email, adapter, url, enabled, license) VALUES
	('Testarchiv', 'project-sara+testarchiv@uni-konstanz.de', 'GitLabArchiveRESTv4',
		'https://' || testarchiv, TRUE,  convert_from(lo_get(license_html), 'UTF-8'))
	RETURNING uuid INTO aRef;
INSERT INTO archive_params(id, param, value) VALUES
	(aRef, 'url', 'https://' || testarchiv),
	(aRef, 'namespace', 'archive-test'),
	(aRef, 'committer-name', 'SARA-test'),
	(aRef, 'committer-email', 'ingest@test.sara-service.org'),
	(aRef, 'token', '__TESTARCHIV_TOKEN__'),
	(aRef, 'private-key', convert_from(lo_get(testarchiv_privkey), 'UTF-8')),
	(aRef, 'public-key', convert_from(lo_get(testarchiv_pubkey), 'UTF-8')),
	(aRef, 'known-hosts', convert_from(lo_get(testarchiv_known), 'UTF-8'));

-- erase the temporary large objects
PERFORM lo_unlink(testarchiv_privkey), lo_unlink(testarchiv_pubkey),
	lo_unlink(testarchiv_known), lo_unlink(license_html);

END $$;
