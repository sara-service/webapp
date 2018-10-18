CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE logo oid := lo_import(base_dir || '/github.svg');

DECLARE sRef UUID;

BEGIN

-- GitHub "development" app (return address: http://localhost:8080/)
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('GitHub, Inc. (without Shibboleth)', 'project-sara+github@uni-konstanz.de',
		'GitHubRESTv3WithoutShib', 'https://github.com', TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'oauthID', 'f900e5206f9ea1137c2c'),
	(sRef, 'oauthSecret', '7429e8858c3debe11661a59d9cc919f0617a6d8f'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
