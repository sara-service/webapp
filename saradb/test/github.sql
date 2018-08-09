DO $$

DECLARE sRef UUID;

BEGIN

-- GitHub "development" app (return address: http://localhost:8080/)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('GitHub, Inc. (return to localhost:8080)', 'github@sara-service.org',
		'GitHubRESTv3', 'https://github.com', TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'oauthID', 'eed8cfac289b122c7e9e'),
	(sRef, 'oauthSecret', 'ba37452c50b23f6f11b8f09f28107c46c17c5286');

END $$;
