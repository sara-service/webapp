DO $$

DECLARE arbeitsgitlab text  := 'demogitlab.sara-service.org';

DECLARE sRef UUID;

BEGIN

-- Matthias' bwCloud GitLab (arbeits-gitlab.unikn.netfuture.ch)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('Generisches Arbeits-GitLab (bwCloud Konstanz)', 'project-sara+demogitlab@uni-konstanz.de',
		'GitLabRESTv4', 'https://' || arbeitsgitlab, TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || arbeitsgitlab),
	(sRef, 'oauthID', 'e452591162ee15f4807f0634fc5c056bd6ea8a4e51e5cea792c7cbfea757cbfe'),
	(sRef, 'oauthSecret', 'c43e89cd992d32fc318608369ab64e5bec74fb207debe38d030e004b4075b1b3');

END $$;
