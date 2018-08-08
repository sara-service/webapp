DO $$

DECLARE arbeitsgitlab text  := 'demogitlab.sara-service.org';

DECLARE sRef UUID;

BEGIN

-- Matthias' bwCloud GitLab (arbeits-gitlab.unikn.netfuture.ch)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('Generisches Arbeits-GitLab (bwCloud Konstanz)', 'devnull@' || arbeitsgitlab,
		'GitLabRESTv4', 'https://' || arbeitsgitlab, TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || arbeitsgitlab),
	(sRef, 'oauthID', '0a622046d40a9a543845c8b7101122b2acce81c539fb390b7081e859b70db671'),
	(sRef, 'oauthSecret', '7aeda662c68a3111ab6f7c41f80ea5ef5c75807a0d4a4e8058831f3762532230');

END $$;
