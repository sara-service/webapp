CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE oparu_logo oid := lo_import(base_dir || '/oparu.svg');

DECLARE demo_dspace text    := 'https://demo-dspace.sara-service.org';

DECLARE demo_dspace_help text := 'Sie werden nun direkt zu OPARU weitergeleitet. Loggen Sie sich ein, klicken Sie auf "Veröffentlichung aufnehmen", ergänzen Sie die Metadaten und schicken Sie die Veröffentlichung ab. Wir prüfen Ihre Einreichung und melden uns ggf. bei Ihnen. Ist alles in Ordnung, wird Ihre Veröffentlichung in OPARU freigeschaltet und Sie erhalten eine Nachricht von uns.';

DECLARE rRef UUID;

BEGIN

-- DEMO DSpace sara-service.org
INSERT INTO repository(display_name, adapter, url, contact_email, help, enabled) VALUES
	('DEMO DSPACE', 'DSpace_v6', demo_dspace, 'help@oparu.de', demo_dspace_help, TRUE)
	RETURNING uuid INTO rRef;
INSERT INTO repository_params(id, param, value) VALUES
	(rRef, 'rest_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'rest_pwd', 'SaraTest'),
	(rRef, 'rest_api_endpoint', demo_dspace || '/rest'),
	(rRef, 'sword_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'sword_pwd', 'SaraTest'),
	(rRef, 'sword_api_endpoint', demo_dspace || '/swordv2');

-- erase the temporary large objects
PERFORM lo_unlink(oparu_logo);

END $$;
