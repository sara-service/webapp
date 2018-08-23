CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE gitlab_testing text  := 'gitlab-testing.disy.inf.uni-konstanz.de';
DECLARE logo oid := lo_import(base_dir || '/uni-konstanz.svg');

DECLARE sRef UUID;

BEGIN

-- Matthias' bwCloud GitLab (arbeits-gitlab.unikn.netfuture.ch)
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('GitLab Test & Staging (Konstanz)', 'git+test@uni-konstanz.de',
		'GitLabRESTv4', 'https://' || gitlab_testing, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || gitlab_testing),
	(sRef, 'oauthID', '01d898c6af68b59cf51855e46c44fd08f520f8ebf497c600edebd89cf5d827f1'),
	(sRef, 'oauthSecret', '744526f5e41ed0c4afbab419f697ed2e9f16582bcc6b07a96f7c78788df77c3f'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
