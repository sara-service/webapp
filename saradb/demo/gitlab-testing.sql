CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE gitlab_testing text  := 'gitlab-testing.disy.inf.uni-konstanz.de';
DECLARE logo oid := lo_import(base_dir || '/uni-konstanz.svg');

DECLARE sRef UUID;

BEGIN

-- GitLab Test & Staging (Konstanz)
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('GitLab Test & Staging (Konstanz)', 'git+test@uni-konstanz.de',
		'GitLabRESTv4', 'https://' || gitlab_testing, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || gitlab_testing),
	(sRef, 'oauthID', 'XXX'),
	(sRef, 'oauthSecret', 'XXX'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
