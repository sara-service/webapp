DO $$

DECLARE aRef UUID;

BEGIN

-- "a local directory" demo git archive
INSERT INTO archive(display_name, contact_email, adapter, url, enabled) VALUES
	('Demo Archive', 'devnull@demo.sara-service.org', 'LocalArchive',
		'https://demo.sara-service.org/archive/', TRUE)
	RETURNING uuid INTO aRef;
INSERT INTO archive_params(id, param, value) VALUES
	(aRef, 'public-root', '/srv/git/public'),
	(aRef, 'private-root', '/srv/git/private'),
	(aRef, 'temp-root', '/srv/git/temp'),
	(aRef, 'web-base', 'https://demo.sara-service.org/archive'),
	(aRef, 'committer-name', 'SARA Demo'),
	(aRef, 'committer-email', 'ingest@demo.sara-service.org');

END $$;
