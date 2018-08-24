CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);
DECLARE arbeitsgitlab text  := 'demogitlab.sara-service.org';

DECLARE logo oid := lo_import(base_dir || '/gitlab.svg');

DECLARE sRef UUID;

BEGIN

-- Matthias' bwCloud GitLab (arbeits-gitlab.unikn.netfuture.ch)
INSERT INTO source(display_name, contact_email, adapter, url, enabled, logo_url) VALUES
	('Generisches Arbeits-GitLab', 'project-sara+demogitlab@uni-konstanz.de', 'GitLabRESTv4',
		'https://' || arbeitsgitlab, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || arbeitsgitlab),
	(sRef, 'oauthID', 'XXX'),
	(sRef, 'oauthSecret', 'XXX'),
	(sRef, 'nameRegex', 'western');

END $$;
