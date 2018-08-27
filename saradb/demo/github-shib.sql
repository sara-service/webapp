CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE logo oid := lo_import(base_dir || '/github.svg');

DECLARE sRef UUID;

BEGIN

-- GitHub "demo" app, *with* Shib
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('GitHub, Inc.', 'project-sara+github@uni-konstanz.de',
		'GitHubRESTv3', 'https://github.com', TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'oauthID', 'XXX'),
	(sRef, 'oauthSecret', 'XXX'),
	(sRef, 'shibSurname', 'sn'),
	(sRef, 'shibGivenName', 'givenName'),
	(sRef, 'shibEmail', 'mail'),
	(sRef, 'shibID', 'persistent-id'),
	(sRef, 'shibDisplayName', 'displayName'),
	(sRef, 'nameRegex', 'western');

END $$;
