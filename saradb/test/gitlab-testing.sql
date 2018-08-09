DO $$

DECLARE gitlab_testing text  := 'gitlab-testing.disy.inf.uni-konstanz.de';

DECLARE sRef UUID;

BEGIN

-- Matthias' bwCloud GitLab (arbeits-gitlab.unikn.netfuture.ch)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('GitLab Test & Staging (Konstanz)', 'devnull@' || gitlab_testing,
		'GitLabRESTv4', 'https://' || gitlab_testing, TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://' || gitlab_testing),
	(sRef, 'oauthID', '01d898c6af68b59cf51855e46c44fd08f520f8ebf497c600edebd89cf5d827f1'),
	(sRef, 'oauthSecret', '744526f5e41ed0c4afbab419f697ed2e9f16582bcc6b07a96f7c78788df77c3f');

END $$;
