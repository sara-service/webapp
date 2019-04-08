CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE arbeitsgitlab text  := 'gitlab.com';
DECLARE logo oid := lo_import(base_dir || '/gitlab.svg');

DECLARE sRef UUID;

BEGIN

-- GitLab.com, app registered by user sara-service (gitlab.com@sara-service.org)
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('GitLab.com (GitLab, Inc.)', 'gitlab.com@sara-service.org',
		'GitLabRESTv4', 'https://' || arbeitsgitlab, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || arbeitsgitlab),
	(sRef, 'oauthID', '__GITLABCOM_ID__'),
	(sRef, 'oauthSecret', '__GITLABCOM_SECRET__'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
