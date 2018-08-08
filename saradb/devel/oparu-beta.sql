CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE oparu_logo oid := lo_import(base_dir || '/oparu.svg');

DECLARE oparu_demo text     := 'https://oparu-beta.sara-service.org';

DECLARE demo_dspace_help text := 'Sie werden nun direkt zu OPARU weitergeleitet. Loggen Sie sich ein, klicken Sie auf "Veröffentlichung aufnehmen", ergänzen Sie die Metadaten und schicken Sie die Veröffentlichung ab. Wir prüfen Ihre Einreichung und melden uns ggf. bei Ihnen. Ist alles in Ordnung, wird Ihre Veröffentlichung in OPARU freigeschaltet und Sie erhalten eine Nachricht von uns.';

DECLARE rRef UUID;

BEGIN

-- Stefan's OPARU Stand-In in bwCloud 
INSERT INTO repository(display_name, adapter, url, contact_email, help, enabled, logo_url) VALUES
	('OPARU Devel', 'DSpace_v6', oparu_demo || '/xmlui', 'help@oparu.uni-ulm.de', demo_dspace_help, TRUE,
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
