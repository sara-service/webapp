DO $$

DECLARE oparu_test text     := 'https://bib-test.rz.uni-ulm.de';

DECLARE rRef UUID;

BEGIN

-- Official OPARU test system
INSERT INTO repository(display_name, adapter, url, contact_email, enabled) VALUES
        ('OPARU Test', 'DSpace_v6', oparu_test, 'help@oparu.uni-ulm.de', TRUE)
        RETURNING uuid INTO rRef;
INSERT INTO repository_params(id, param, value) VALUES
       (rRef, 'rest_user', 'project-sara@uni-konstanz.de'),
       (rRef, 'rest_pwd', 'SaraTest'),
       (rRef, 'rest_api_endpoint', oparu_test || '/rest'),
       (rRef, 'sword_user', 'project-sara@uni-konstanz.de'),
       (rRef, 'sword_pwd', 'SaraTest'),
       (rRef, 'sword_api_endpoint', oparu_test || '/swordv2'),
       (rRef, 'deposit_type', 'workspace'),
       (rRef, 'name_regex', '^([^,]*)\p{Z}+([^\p{Z},]+)$'),
       (rRef, 'name_replace', '$2, $1');

END $$;
