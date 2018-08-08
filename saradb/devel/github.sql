DO $$

DECLARE sRef UUID;

BEGIN

-- GitHub "development" app (return address: http://localhost:8080/)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('GitHub, Inc. (return to localhost:8080)', 'github@sara-service.org',
		'GitHubRESTv3', 'https://github.com', TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'oauthID', 'a71475f487dc3438b132'),
	(sRef, 'oauthSecret', 'f84451f01d60ea507955e3454526ea396a77b479');

END $$;
