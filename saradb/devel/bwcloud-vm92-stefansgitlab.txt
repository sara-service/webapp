DO $$

DECLARE stefansgitlab text  := 'bwcloud-vm92.rz.uni-ulm.de';

DECLARE sRef UUID;

BEGIN

-- Stefan's bwCloud GitLab (bwcloud-vm92.rz.uni-ulm.de)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('Ulmer GitLab', 'stefan.kombrink@uni-ulm.de', 'GitLabRESTv4',
		'https://' || stefansgitlab, TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || stefansgitlab),
	(sRef, 'oauthID', '2ab7716f633dc3147272e2ac95b630caaadab2f08b4761d2a2f1cc20756717bc'),
	(sRef, 'oauthSecret', '7326d31de2a072aa558c14d4e57fe4d09ff4120548052b6b4f024250fa35e9da');

END $$;
