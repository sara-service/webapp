CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE logo oid := lo_import(base_dir || '/github.svg');

DECLARE sRef UUID;

BEGIN

-- GitHub "development" app (return address: http://localhost:8080/)
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('GitHub, Inc.', 'project-sara+github@uni-konstanz.de',
		'GitHubRESTv3WithoutShib', 'https://github.com', TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'oauthID', 'eed8cfac289b122c7e9e'),
	(sRef, 'oauthSecret', 'ba37452c50b23f6f11b8f09f28107c46c17c5286'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
