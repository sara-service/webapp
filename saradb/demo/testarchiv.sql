CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE testarchiv_privkey oid := lo_import(base_dir || '/testarchiv.key');
DECLARE testarchiv_pubkey oid  := lo_import(base_dir || '/testarchiv.key.pub');
DECLARE testarchiv_known oid   := lo_import(base_dir || '/testarchiv.known');

DECLARE testarchiv text     := 'testarchiv.sara-service.org';

DECLARE aRef UUID;

BEGIN

-- "Testarchiv" default git archive
INSERT INTO archive(display_name, contact_email, adapter, url, enabled) VALUES
	('Testarchiv', 'devnull@' || testarchiv, 'GitLabArchiveRESTv4',
		'https://' || testarchiv, TRUE)
	RETURNING uuid INTO aRef;
INSERT INTO archive_params(id, param, value) VALUES
	(aRef, 'url', 'https://' || testarchiv),
	(aRef, 'temp-namespace', 'temp-archive'),
	(aRef, 'main-namespace', 'archive'),
	(aRef, 'dark-namespace', 'dark-archive'),
	(aRef, 'committer-name', 'SARA Demo'),
	(aRef, 'committer-email', 'ingest@demo.sara-service.org'),
	(aRef, 'token', 'XXX'),
	(aRef, 'private-key', convert_from(lo_get(testarchiv_privkey), 'UTF-8')),
	(aRef, 'public-key', convert_from(lo_get(testarchiv_pubkey), 'UTF-8')),
	(aRef, 'known-hosts', convert_from(lo_get(testarchiv_known), 'UTF-8'));

-- erase the temporary large objects
PERFORM lo_unlink(testarchiv_privkey), lo_unlink(testarchiv_pubkey),
	lo_unlink(testarchiv_known);

END $$;
