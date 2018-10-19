CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE omi_gitlab text  := 'omi-gitlab.e-technik.uni-ulm.de';
DECLARE logo oid := lo_import(base_dir || '/gitlab.svg');

DECLARE sRef UUID;

BEGIN

-- IOMI GitLab
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('IOMI GitLab (Ulm)', 'git+test@uni-konstanz.de',
		'GitLabRESTv4', 'https://' || omi_gitlab, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
-- NOTE: these are credentials for a production service and should definitely NOT
-- be in any repository, even an access-controlled one!
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || omi_gitlab),
	(sRef, 'oauthID', 'xxx'),
	(sRef, 'oauthSecret', 'xxx'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
