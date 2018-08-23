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
	('Generisches Arbeits-GitLab (bwCloud Konstanz)', 'devnull@' || arbeitsgitlab,
		'GitLabRESTv4', 'https://' || arbeitsgitlab, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'))
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || arbeitsgitlab),
	(sRef, 'oauthID', '0a622046d40a9a543845c8b7101122b2acce81c539fb390b7081e859b70db671'),
	(sRef, 'oauthSecret', '7aeda662c68a3111ab6f7c41f80ea5ef5c75807a0d4a4e8058831f3762532230'),
	(sRef, 'nameRegex', 'western');

-- erase the temporary large object
PERFORM lo_unlink(logo);

END $$;
