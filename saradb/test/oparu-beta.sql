CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE oparu_logo oid := lo_import(base_dir || '/oparu.svg');

DECLARE oparu_demo text     := 'https://oparu-beta.sara-service.org';

DECLARE demo_dspace_help text := 'You will now be redirected to the Institutional repository of Demo University. Please login, click "Resume" and submit the publication. You can edit metadata if necessary. Your submission will then be reviewed, and you will be notified as soon as it has been approved.';

DECLARE rRef UUID;

BEGIN

-- Stefan's OPARU Stand-In in bwCloud 
INSERT INTO repository(display_name, adapter, url, contact_email, help, enabled, logo_url) VALUES
	('Demo University DSpace', 'DSpace_v6', oparu_demo || '/xmlui',
		'project-sara+oparu-beta@uni-konstanz.de', demo_dspace_help, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(oparu_logo), 'base64'))
	RETURNING uuid INTO rRef;
INSERT INTO repository_params(id, param, value) VALUES
	(rRef, 'rest_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'rest_pwd', 'SaraTest'),
	(rRef, 'rest_api_endpoint', oparu_demo || '/rest'),
	(rRef, 'sword_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'sword_pwd', 'SaraTest'),
	(rRef, 'sword_api_endpoint', oparu_demo || '/swordv2'),
	(rRef, 'deposit_type', 'workspace'),
	(rRef, 'name_regex', '^([^,]*)\p{Z}+([^\p{Z},]+)$'),
	(rRef, 'name_replace', '$2, $1'),
	(rRef, 'publication_type', 'Software');
--	(rRef, 'force_onbehalf', '1'),
--	(rRef, 'workflow_type', 'login_required');

-- erase the temporary large objects
PERFORM lo_unlink(oparu_logo);

END $$;
