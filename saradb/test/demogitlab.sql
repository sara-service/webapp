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
	('Generisches Arbeits-GitLab (bwCloud Konstanz)', 'project-sara+demogitlab@uni-konstanz.de',
		'GitLabRESTv4', 'https://' || arbeitsgitlab, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || arbeitsgitlab),
	(sRef, 'oauthID', 'e452591162ee15f4807f0634fc5c056bd6ea8a4e51e5cea792c7cbfea757cbfe'),
	(sRef, 'oauthSecret', 'c43e89cd992d32fc318608369ab64e5bec74fb207debe38d030e004b4075b1b3'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
