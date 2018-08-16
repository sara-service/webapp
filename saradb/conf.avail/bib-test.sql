CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE oparu_logo oid := lo_import(base_dir || '/oparu.svg');

DECLARE oparu_test text     := 'https://bib-test.rz.uni-ulm.de';

DECLARE oparu_demo_help text     := 'Thank you for your submission. Your publication will be reviewed by the OPARU team. You will soon receive an email informing you about its acceptance or rejection. Goodbye!';

DECLARE rRef UUID;

BEGIN

-- Official OPARU test system
INSERT INTO repository(display_name, adapter, url, contact_email, help, enabled, logo_url) VALUES
        ('OPARU Test', 'DSpace_v6', oparu_test, 'help@oparu.uni-ulm.de', 
        oparu_demo_help, TRUE, 'data:image/svg+xml;base64,' || encode(lo_get(oparu_logo), 'base64'))
        RETURNING uuid INTO rRef;

INSERT INTO repository_params(id, param, value) VALUES
       (rRef, 'rest_user', 'project-sara@uni-konstanz.de'),
       (rRef, 'rest_pwd', 'SaraTest'),
       (rRef, 'rest_api_endpoint', oparu_test || '/rest'),
       (rRef, 'sword_user', 'project-sara@uni-konstanz.de'),
       (rRef, 'sword_pwd', 'SaraTest'),
       (rRef, 'sword_api_endpoint', oparu_test || '/swordv2'),
       (rRef, 'deposit_type', 'workflow'),
       (rRef, 'name_regex', '^([^,]*)\p{Z}+([^\p{Z},]+)$'),
       (rRef, 'name_replace', '$2, $1');

PERFORM lo_unlink(oparu_logo);

END $$;
