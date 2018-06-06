-- adding some default entries

DO $$

DECLARE kops_logo oid  := lo_import('/saradb/config/kops.svg');
DECLARE oparu_logo oid := lo_import('/saradb/config/oparu.svg');
DECLARE testarchiv_privkey oid := lo_import('/saradb/config/testarchiv.key');
DECLARE testarchiv_pubkey oid := lo_import('/saradb/config/testarchiv.key.pub');
DECLARE testarchiv_known oid := lo_import('/saradb/config/testarchiv.known');

DECLARE aRef UUID;
DECLARE sRef UUID;
DECLARE rRef UUID;
DECLARE rRef2 UUID;
DECLARE rRef3 UUID;
DECLARE iRef UUID;

BEGIN

-- Matthias' bwCloud GitLab (arbeits-gitlab.unikn.netfuture.ch)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('Generisches Arbeits-GitLab (bwCloud Konstanz)',
		'devnull@arbeits-gitlab.unikn.netfuture.ch',
		'GitLabRESTv4', 'https://arbeits-gitlab.unikn.netfuture.ch', TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://arbeits-gitlab.unikn.netfuture.ch'),
	(sRef, 'oauthID', 'd6f80baadb28e3d9d20b79f6a27c0747f6692a67321375e40be1a1b0fd8bb430'),
	(sRef, 'oauthSecret', '6df3596f15aaa0b4e1ecd4297c697e42632b2cab4c4224bbdd6a34b9ed0674f1');
-- Stefan's bwCloud GitLab (bwcloud-vm92.rz.uni-ulm.de)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('Ulmer GitLab', 'stefan.kombrink@uni-ulm.de', 'GitLabRESTv4',
		'https://bwcloud-vm92.rz.uni-ulm.de', TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'url', 'https://bwcloud-vm92.rz.uni-ulm.de'),
	(sRef, 'oauthID', '2ab7716f633dc3147272e2ac95b630caaadab2f08b4761d2a2f1cc20756717bc'),
	(sRef, 'oauthSecret', '7326d31de2a072aa558c14d4e57fe4d09ff4120548052b6b4f024250fa35e9da');

-- GitHub "development" app (return address: http://localhost:8080/)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('GitHub, Inc. (return to localhost:8080)', 'devnull@unikn.netfuture.ch',
		'GitHubRESTv3', 'https://github.com', TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'oauthID', 'a71475f487dc3438b132'),
	(sRef, 'oauthSecret', 'f84451f01d60ea507955e3454526ea396a77b479');
-- GitHub "test" app (return address: https://sara.unikn.netfuture.ch/)
INSERT INTO source(display_name, contact_email, adapter, url, enabled) VALUES
	('GitHub, Inc.', 'devnull@unikn.netfuture.ch', 'GitHubRESTv3',
		'https://github.com', TRUE)
	RETURNING uuid INTO sRef;
INSERT INTO source_params(id, param, value) VALUES
	(sRef, 'oauthID', '7a0fc631e187589a8d2a'),
	(sRef, 'oauthSecret', 'a0acf7b6eb2848e0df2a2efb8a135a97f0e65eb7');

-- "Testarchiv" default git archive
INSERT INTO archive(display_name, contact_email, adapter, url, enabled) VALUES
	('Testarchiv', 'devnull@testarchiv.unikn.netfuture.ch', 'GitLabArchiveRESTv4',
		'https://testarchiv.unikn.netfuture.ch', TRUE)
	RETURNING uuid INTO aRef;
INSERT INTO archive_params(id, param, value) VALUES
	(aRef, 'url', 'https://testarchiv.unikn.netfuture.ch'),
	(aRef, 'temp-namespace', 'temp-archive'),
	(aRef, 'main-namespace', 'archive'),
	(aRef, 'dark-namespace', 'dark-archive'),
	(aRef, 'token', 'iGU48bY6ythKN9dmXwKV'),
	(aRef, 'private-key', convert_from(lo_get(testarchiv_privkey), 'UTF-8')),
	(aRef, 'public-key', convert_from(lo_get(testarchiv_pubkey), 'UTF-8')),
	(aRef, 'known-hosts', convert_from(lo_get(testarchiv_known), 'UTF-8'));

-- Stefan's OPARU Stand-In in bwCloud (https://bwcloud-vm65.rz.uni-ulm.de:8080/xmlui/)
INSERT INTO repository(display_name, adapter, url, contact_email, enabled, logo_url) VALUES
	('OPARU Ulm', 'DSpace_v6', 'https://bwcloud-vm65.rz.uni-ulm.de:8080',
		'help@oparu.uni-ulm.de', TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(oparu_logo), 'base64'))
	RETURNING uuid INTO rRef;
INSERT INTO repository_params(id, param, value) VALUES
	(rRef, 'rest_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'rest_pwd', 'SaraTest'),
	(rRef, 'rest_api_endpoint', 'rest'),
	(rRef, 'sword_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'sword_pwd', 'SaraTest'),
	(rRef, 'sword_api_endpoint', 'swordv2');
--	(rRef, 'force_onbehalf', '1'),
--	(rRef, 'workflow_type', 'login_required');

-- Fake KOPS (only for show; this doesn't work)
INSERT INTO repository(display_name, adapter, url, contact_email, enabled, logo_url) VALUES
	('KOPS Konstanz', 'DSpace_v6', 'https://kops.uni-konstanz.de',
		'kops.kim@uni-konstanz.de', TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(kops_logo), 'base64'))
	RETURNING uuid INTO rRef2;
INSERT INTO repository_params(id, param, value) VALUES
	(rRef2, 'rest_user', 'project-sara@uni-konstanz.de'),
	(rRef2, 'rest_pwd', 'SaraTest'),
	(rRef2, 'rest_api_endpoint', 'rest'),
	(rRef2, 'sword_user', 'project-sara@uni-konstanz.de'),
	(rRef2, 'sword_pwd', 'SaraTest'),
	(rRef2, 'sword_api_endpoint', 'swordv2');
--	(rRef2, 'force_onbehalf', '1'),
--	(rRef2, 'workflow_type', 'login_required');

-- Official OPARU Test System?
INSERT INTO repository(display_name, adapter, url, contact_email, enabled) VALUES
	('OPARU TEST', 'DSpace_v6', 'http://bib-test.rz.uni-ulm.de',
		'help@oparu.uni-ulm.de', TRUE)
	RETURNING uuid INTO rRef3;
INSERT INTO repository_params(id, param, value) VALUES
	(rRef3, 'rest_user', 'project-sara@uni-konstanz.de'),
	(rRef3, 'rest_pwd', 'SaraTest'),
	(rRef3, 'rest_api_endpoint', 'rest'),
	(rRef3, 'sword_user', 'project-sara@uni-konstanz.de'),
	(rRef3, 'sword_pwd', 'SaraTest'),
	(rRef3, 'sword_api_endpoint', 'swordv2');

--INSERT INTO collection(id, display_name, foreign_collection_uuid, enabled) VALUES(rRef, 'coffee management', '0815', TRUE);  -- coffee management
--INSERT INTO collection(id, display_name, foreign_collection_uuid, enabled) VALUES(rRef, 'milk sciences', '0914', FALSE); -- milk sciences

INSERT INTO metadatamapping(repository_uuid, display_name, map_from, map_to, enabled) VALUES
	(rRef, 'publication title', 'dc.title', '0714', TRUE),
	(rRef, 'email adress', 'dc.contact', '0698', FALSE),
	(rRef, 'author', 'dc.author', '0567', TRUE),
	(rRef, 'second author', 'dc.other_author', '0568', TRUE),
	(rRef, 'archive link', 'dc.archive_link', '1045', TRUE);

INSERT INTO item(source_uuid, archive_uuid, repository_uuid, contact_email, item_type,
		item_state, item_state_sent, date_created, date_last_modified) VALUES
	(sRef, aRef, rRef, 'stefan.kombrink@uni-ulm.de', 'PUBLISH', 'CREATED', 'CREATED',
		now(), now())
	RETURNING uuid INTO iRef;

-- FIXME temporary fix to fulfil constraints until items are created for real!
UPDATE item SET uuid = 'deadbeef-dead-dead-dead-beeeeeeeeeef';

-- erase the temporary large objects
PERFORM lo_unlink(kops_logo), lo_unlink(oparu_logo), lo_unlink(testarchiv_privkey),
	lo_unlink(testarchiv_pubkey), lo_unlink(testarchiv_known);

END $$;
