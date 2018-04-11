-- adding some default entries

do $$

declare default_archive text          := 'Archive Gitlab Konstanz';
declare default_gitlab  text          := 'Work Gitlab Konstanz';
declare default_repository text       := 'OPARU Ulm';
declare default_repository_home text  := 'http://134.60.51.65:8080';
declare default_repository_sword text := 'swordv2';
declare default_repository_rest text  := 'rest';

declare pRef UUID;
declare aRef UUID;
declare sRef UUID;
declare rRef UUID;
declare rRef2 UUID;
declare iRef UUID;

begin

-- add a user
insert into eperson (contact_email, password, last_active) values('stefan.kombrink@uni-ulm.de', 'p4ssw0rd', now()) returning uuid into pRef;

-- and another user
insert into eperson (contact_email, password, last_active) values('anonymous_coward@void.com', 'pwd', now());

-- add default working gitlab repo as source
insert into source (display_name, contact_email, adapter, url, enabled) values(default_gitlab, 'devnull@unikn.netfuture.ch', 'GitLabRESTv4', 'https://arbeits-gitlab.unikn.netfuture.ch', true) returning uuid into sRef;
insert into source_params (id, param, value) values (sRef, 'url', 'https://arbeits-gitlab.unikn.netfuture.ch' );
insert into source_params (id, param, value) values (sRef, 'oauthID', 'd6f80baadb28e3d9d20b79f6a27c0747f6692a67321375e40be1a1b0fd8bb430');
insert into source_params (id, param, value) values (sRef, 'oauthSecret', '6df3596f15aaa0b4e1ecd4297c697e42632b2cab4c4224bbdd6a34b9ed0674f1');
insert into source (display_name, contact_email, adapter, url, enabled) values('IOMI GitLabs', 'devnull@unikn.netfuture.ch', 'GitLabRESTv4', 'https://omi-gitlab.e-technik.uni-ulm.de/', true) returning uuid into sRef;
insert into source_params (id, param, value) values (sRef, 'url', 'https://arbeits-gitlab.unikn.netfuture.ch' );
insert into source_params (id, param, value) values (sRef, 'oauthID', 'd6f80baadb28e3d9d20b79f6a27c0747f6692a67321375e40be1a1b0fd8bb430');
insert into source_params (id, param, value) values (sRef, 'oauthSecret', '6df3596f15aaa0b4e1ecd4297c697e42632b2cab4c4224bbdd6a34b9ed0674f1');

-- add github "development" app as source
insert into source (display_name, contact_email, adapter, url, enabled) values('GitHub, Inc. (return to localhost:8080)', 'devnull@unikn.netfuture.ch', 'GitHubRESTv3', 'https://github.com', true) returning uuid into sRef;
insert into source_params (id, param, value) values (sRef, 'oauthID', 'a71475f487dc3438b132');
insert into source_params (id, param, value) values (sRef, 'oauthSecret', 'f84451f01d60ea507955e3454526ea396a77b479');
-- add github "test" app as source
insert into source (display_name, contact_email, adapter, url, enabled) values('GitHub, Inc.', 'devnull@unikn.netfuture.ch', 'GitHubRESTv3', 'https://github.com', true) returning uuid into sRef;
insert into source_params (id, param, value) values (sRef, 'oauthID', '7a0fc631e187589a8d2a');
insert into source_params (id, param, value) values (sRef, 'oauthSecret', 'a0acf7b6eb2848e0df2a2efb8a135a97f0e65eb7');

-- add default gitlab archive
insert into archive (display_name, contact_email, adapter, url, enabled) values(default_archive, 'devnull@unikn.netfuture.ch', 'GitLabArchiveRESTv4', 'https://testarchiv.unikn.netfuture.ch', true) returning uuid into aRef;
insert into archive_params (id, param, value) values (aRef, 'url', 'https://testarchiv.unikn.netfuture.ch' );
insert into archive_params (id, param, value) values (aRef, 'temp-namespace', 'temp-archive' );
insert into archive_params (id, param, value) values (aRef, 'main-namespace', 'archive' );
insert into archive_params (id, param, value) values (aRef, 'dark-namespace', 'dark-archive' );
insert into archive_params (id, param, value) values (aRef, 'token', 'iGU48bY6ythKN9dmXwKV');
insert into archive_params (id, param, value) values (aRef, 'private-key', '-----BEGIN EC PRIVATE KEY-----' || chr(10) || 'MHcCAQEEIFCS1djlLXpPUyVEbuyhybZCPohmkNq1FHpMGDPbkR2BoAoGCCqGSM49' || chr(10) || 'AwEHoUQDQgAEYXueBAFBNvKheBV/CIYZ8Kgfyvk2L1UiDyrA7j49Gi6HpT2S0nq4' || chr(10) || '0JhH/XBFN5XrIj1Kurl39iZIPTB84whb6Q==' || chr(10) || '-----END EC PRIVATE KEY-----');
insert into archive_params (id, param, value) values (aRef, 'public-key', 'ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBGF7ngQBQTbyoXgVfwiGGfCoH8r5Ni9VIg8qwO4+PRouh6U9ktJ6uNCYR/1wRTeV6yI9Srq5d/YmSD0wfOMIW+k= matthias@hydra');
insert into archive_params (id, param, value) values (aRef, 'known-hosts', 'testarchiv.unikn.netfuture.ch ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNfwAQcp5oW/b0d/L9jcxIKYfYHmm3UU59s/MWryTPXiR2Q1tUlmMKcdgC/idLq/jverlf3/GTgrdDFcYYL1Blw= root@archiv-gitlab' || chr(10) || 'testarchiv.unikn.netfuture.ch ssh-dss AAAAB3NzaC1kc3MAAACBAJeGdQJiNxQH9U24DzBOZRRaCQnjokHmogH3BvhbqMP33JXJrbvxgCHXvu4edTRFTtqv82u5LlZH6I4IBSaucREOhCOTkijE9lEJGPdCuCrwm7nJqUR2Y62ciRkmUy1sU3trfHLx2CSvd2t3w8oHd9LmVJlTLPA/xowEzkPqtSmzAAAAFQDGWrLs8HWetRsgjZBNzlZrfVZUQQAAAIA9RXF8/cXJ8rTTIPv8ZmL6UczF99IIuxi6zK4YnBl8//P6YuEVWjyM28UxOspJe/WVng+TQhukB791eTvbXYNwITSfz1lb8/WnNUpXD67htlePOMjHOlJ2iG0leO58fM1jugQARv0tVKuyngwHjX6gYbJHrqLeRHlz1xr/be906wAAAIA5MzCV0ubccqzTei6MFmtnc4G1j8prUfKPEwIYt7ewt7Ru+6hlGyPLrqRcEdCfrl6szv8n6wKN6Ov8fMmCPPLNbiA3641CCRvglR7in+16OHZn+QJ3HFS7w7Jcy7lqzmVbk246iGmBJXrHh4+pA7XsDWnFIP/56ZeXmVMqZqOUHA== root@archiv-gitlab' || chr(10) || 'testarchiv.unikn.netfuture.ch ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDa6jZJ8l63KYa+6WSBNfuMdUAC0sPbup1OSEbJhzX6ACLvXOGFCW2CjQVYqez/F5KtwexEyYy0oW07XL3hlLbHZ1DXvZXBQUPDfo48XB6+FYJRlcW4CfDAqEJfQyDksu61BiUCCAnVL0TCtDvLKaauiDPEnZ/KBU4of5a0twpPRss6msOXz645S605l3eHDsFj0Sb98d2SB1fkqyGcxOpqesYaiEIkCVF2VxJGmz/QAvV4jKV255zKRodjDq8/QtPwPkZARvghKWydRK78gk2XJToUaRpyHCwAlHGwvNHnxn+Z4W2vqHXIbQeuVCjLyXA8FhnqccjUiz+5n1oYhMJJ root@archiv-gitlab');

-- add default DSpace-compatible IR
insert into repository (display_name, adapter, url, contact_email, enabled, logo_base64 ) values (default_repository, 'DSpace_v6', default_repository_home, 'help@oparu.uni-ulm.de', true, 'DQoJPHBhdGggZmlsbD0iI0QzRDgwMCIgZD0iTTE2OC41OSwzNi45NTRjMC4zOTgtMC4xNDMsMS43NTItMC40NDcsMi4xMDYtMC4yOGMwLjM1NCwwLjE2NywwLjk4LDEuNDA2LDEuMTIzLDEuODAzDQoJCXMwLjQ0NywxLjc1MiwwLjI4LDIuMTA2Yy0wLjE2NywwLjM1NC0xLjQwNSwwLjk4LTEuODA0LDEuMTIzYy0wLjM5NywwLjE0My0xLjc1MiwwLjQ0Ny0yLjEwNCwwLjI4DQoJCWMtMC4zNTQtMC4xNjctMC45ODEtMS40MDYtMS4xMjUtMS44MDNjLTAuMTQyLTAuMzk3LTAuNDQ2LTEuNzUyLTAuMjc5LTIuMTA2UzE2OC4xOTMsMzcuMDk2LDE2OC41OSwzNi45NTRMMTY4LjU5LDM2Ljk1NHoiLz4NCgk8cGF0aCBmaWxsPSIjRDNEODAwIiBkPSJNMTcxLjMwMSw0NC41MTdjMC4yOTgtMC4xMDcsMS44NjEtMC41MzIsMi4xMjctMC40MDZjMC4yNjYsMC4xMjUsMC45MzIsMS42MDEsMS4wMzgsMS44OTgNCgkJYzAuMTA3LDAuMjk4LDAuNTMyLDEuODYsMC40MDcsMi4xMjZjLTAuMTI2LDAuMjY2LTEuNjAyLDAuOTMyLTEuOSwxLjAzOWMtMC4yOTgsMC4xMDctMS44NTksMC41MzEtMi4xMjUsMC40MDYNCgkJcy0wLjkzMi0xLjYwMi0xLjAzOS0xLjg5OWMtMC4xMDYtMC4yOTgtMC41MzEtMS44Ni0wLjQwNi0yLjEyNkMxNjkuNTI3LDQ1LjI5LDE3MS4wMDMsNDQuNjI0LDE3MS4zMDEsNDQuNTE3TDE3MS4zMDEsNDQuNTE3eiIvPg0KCTxwYXRoIGZpbGw9IiNEM0Q4MDAiIGQ9Ik0xNzQuMDEyLDUyLjA3OWMwLjE5OS0wLjA3MSwxLjk3LTAuNjE2LDIuMTQ3LTAuNTMzYzAuMTc3LDAuMDg0LDAuODgyLDEuNzk3LDAuOTU0LDEuOTk2DQoJCWMwLjA3LDAuMTk4LDAuNjE1LDEuOTY5LDAuNTMyLDIuMTQ2Yy0wLjA4NCwwLjE3Ny0xLjc5NywwLjg4My0xLjk5NSwwLjk1NHMtMS45NywwLjYxNi0yLjE0NiwwLjUzMw0KCQljLTAuMTc3LTAuMDg0LTAuODgyLTEuNzk3LTAuOTU0LTEuOTk1Yy0wLjA3MS0wLjE5OS0wLjYxNi0xLjk3LTAuNTMyLTIuMTQ2QzE3Mi4xMDEsNTIuODU2LDE3My44MTMsNTIuMTUsMTc0LjAxMiw1Mi4wNzkNCgkJTDE3NC4wMTIsNTIuMDc5eiIvPg0KCTxwYXRoIGZpbGw9IiNEM0Q4MDAiIGQ9Ik0xNzYuNzIzLDU5LjY0M2MwLjEwMS0wLjAzNiwyLjA3OS0wLjcwMSwyLjE2Ny0wLjY1OXMwLjgzNCwxLjk5MSwwLjg3LDIuMDkxDQoJCWMwLjAzNSwwLjA5OSwwLjcsMi4wNzgsMC42NTksMi4xNjZjLTAuMDQyLDAuMDg5LTEuOTkyLDAuODM1LTIuMDkxLDAuODdjLTAuMTAxLDAuMDM2LTIuMDc4LDAuNy0yLjE2NiwwLjY1OA0KCQljLTAuMDg5LTAuMDQxLTAuODM1LTEuOTkxLTAuODctMi4wOTFjLTAuMDM2LTAuMDk5LTAuNzAxLTIuMDc3LTAuNjU5LTIuMTY2UzE3Ni42MjQsNTkuNjc4LDE3Ni43MjMsNTkuNjQzTDE3Ni43MjMsNTkuNjQzeiIvPg0KCTxwb2x5Z29uIGZpbGw9IiNEM0Q4MDAiIHBvaW50cz0iMTgzLjE5MSw3MC43OTMgMTgxLjAwNSw3MS41NzggMTc4LjgxOCw3Mi4zNjMgMTc4LjAzMyw3MC4xNzcgMTc3LjI0OCw2Ny45OSAxNzkuNDM1LDY3LjIwNSANCgkJMTgxLjYyMSw2Ni40MiAxODIuNDA2LDY4LjYwNiAxODMuMTkxLDcwLjc5MyAJIi8' ) returning uuid into rRef;

insert into repository (display_name, adapter, url, contact_email, enabled, logo_base64 ) values ('KOPS Konstanz', 'DSpace_v6', 'https://kops.uni-konstanz.de', 'help@oparu.uni-ulm.de', true, 'DQoJPHBhdGggZmlsbD0iI0QzRDgwMCIgZD0iTTE2OC41OSwzNi45NTRjMC4zOTgtMC4xNDMsMS43NTItMC40NDcsMi4xMDYtMC4yOGMwLjM1NCwwLjE2NywwLjk4LDEuNDA2LDEuMTIzLDEuODAzDQoJCXMwLjQ0NywxLjc1MiwwLjI4LDIuMTA2Yy0wLjE2NywwLjM1NC0xLjQwNSwwLjk4LTEuODA0LDEuMTIzYy0wLjM5NywwLjE0My0xLjc1MiwwLjQ0Ny0yLjEwNCwwLjI4DQoJCWMtMC4zNTQtMC4xNjctMC45ODEtMS40MDYtMS4xMjUtMS44MDNjLTAuMTQyLTAuMzk3LTAuNDQ2LTEuNzUyLTAuMjc5LTIuMTA2UzE2OC4xOTMsMzcuMDk2LDE2OC41OSwzNi45NTRMMTY4LjU5LDM2Ljk1NHoiLz4NCgk8cGF0aCBmaWxsPSIjRDNEODAwIiBkPSJNMTcxLjMwMSw0NC41MTdjMC4yOTgtMC4xMDcsMS44NjEtMC41MzIsMi4xMjctMC40MDZjMC4yNjYsMC4xMjUsMC45MzIsMS42MDEsMS4wMzgsMS44OTgNCgkJYzAuMTA3LDAuMjk4LDAuNTMyLDEuODYsMC40MDcsMi4xMjZjLTAuMTI2LDAuMjY2LTEuNjAyLDAuOTMyLTEuOSwxLjAzOWMtMC4yOTgsMC4xMDctMS44NTksMC41MzEtMi4xMjUsMC40MDYNCgkJcy0wLjkzMi0xLjYwMi0xLjAzOS0xLjg5OWMtMC4xMDYtMC4yOTgtMC41MzEtMS44Ni0wLjQwNi0yLjEyNkMxNjkuNTI3LDQ1LjI5LDE3MS4wMDMsNDQuNjI0LDE3MS4zMDEsNDQuNTE3TDE3MS4zMDEsNDQuNTE3eiIvPg0KCTxwYXRoIGZpbGw9IiNEM0Q4MDAiIGQ9Ik0xNzQuMDEyLDUyLjA3OWMwLjE5OS0wLjA3MSwxLjk3LTAuNjE2LDIuMTQ3LTAuNTMzYzAuMTc3LDAuMDg0LDAuODgyLDEuNzk3LDAuOTU0LDEuOTk2DQoJCWMwLjA3LDAuMTk4LDAuNjE1LDEuOTY5LDAuNTMyLDIuMTQ2Yy0wLjA4NCwwLjE3Ny0xLjc5NywwLjg4My0xLjk5NSwwLjk1NHMtMS45NywwLjYxNi0yLjE0NiwwLjUzMw0KCQljLTAuMTc3LTAuMDg0LTAuODgyLTEuNzk3LTAuOTU0LTEuOTk1Yy0wLjA3MS0wLjE5OS0wLjYxNi0xLjk3LTAuNTMyLTIuMTQ2QzE3Mi4xMDEsNTIuODU2LDE3My44MTMsNTIuMTUsMTc0LjAxMiw1Mi4wNzkNCgkJTDE3NC4wMTIsNTIuMDc5eiIvPg0KCTxwYXRoIGZpbGw9IiNEM0Q4MDAiIGQ9Ik0xNzYuNzIzLDU5LjY0M2MwLjEwMS0wLjAzNiwyLjA3OS0wLjcwMSwyLjE2Ny0wLjY1OXMwLjgzNCwxLjk5MSwwLjg3LDIuMDkxDQoJCWMwLjAzNSwwLjA5OSwwLjcsMi4wNzgsMC42NTksMi4xNjZjLTAuMDQyLDAuMDg5LTEuOTkyLDAuODM1LTIuMDkxLDAuODdjLTAuMTAxLDAuMDM2LTIuMDc4LDAuNy0yLjE2NiwwLjY1OA0KCQljLTAuMDg5LTAuMDQxLTAuODM1LTEuOTkxLTAuODctMi4wOTFjLTAuMDM2LTAuMDk5LTAuNzAxLTIuMDc3LTAuNjU5LTIuMTY2UzE3Ni42MjQsNTkuNjc4LDE3Ni43MjMsNTkuNjQzTDE3Ni43MjMsNTkuNjQzeiIvPg0KCTxwb2x5Z29uIGZpbGw9IiNEM0Q4MDAiIHBvaW50cz0iMTgzLjE5MSw3MC43OTMgMTgxLjAwNSw3MS41NzggMTc4LjgxOCw3Mi4zNjMgMTc4LjAzMyw3MC4xNzcgMTc3LjI0OCw2Ny45OSAxNzkuNDM1LDY3LjIwNSANCgkJMTgxLjYyMSw2Ni40MiAxODIuNDA2LDY4LjYwNiAxODMuMTkxLDcwLjc5MyAJIi8' ) returning uuid into rRef2;

insert into repository_params(id, param, value) values (rRef, 'rest_user', 'project-sara@uni-konstanz.de');
insert into repository_params(id, param, value) values (rRef, 'rest_pwd', 'SaraTest');
insert into repository_params(id, param, value) values (rRef, 'rest_api_endpoint', 'rest');
insert into repository_params(id, param, value) values (rRef, 'sword_user', 'project-sara@uni-konstanz.de');
insert into repository_params(id, param, value) values (rRef, 'sword_pwd', 'SaraTest');
insert into repository_params(id, param, value) values (rRef, 'sword_api_endpoint', 'swordv2');
--insert into repository_params(id, param, value) values (rRef, 'force_onbehalf', '1');
--insert into repository_params(id, param, value) values (rRef, 'workflow_type', 'login_required');

insert into repository_params(id, param, value) values (rRef2, 'rest_user', 'project-sara@uni-konstanz.de');
insert into repository_params(id, param, value) values (rRef2, 'rest_pwd', 'SaraTest');
insert into repository_params(id, param, value) values (rRef2, 'rest_api_endpoint', 'rest');
insert into repository_params(id, param, value) values (rRef2, 'sword_user', 'project-sara@uni-konstanz.de');
insert into repository_params(id, param, value) values (rRef2, 'sword_pwd', 'SaraTest');
insert into repository_params(id, param, value) values (rRef2, 'sword_api_endpoint', 'swordv2');
--insert into repository_params(id, param, value) values (rRef2, 'force_onbehalf', '1');
--insert into repository_params(id, param, value) values (rRef2, 'workflow_type', 'login_required');

--insert into collection(id, display_name, foreign_collection_uuid, enabled) values (rRef, 'coffee management', '0815', true);  -- coffee management
--insert into collection(id, display_name, foreign_collection_uuid, enabled) values (rRef, 'milk sciences', '0914', false); -- milk sciences

insert into metadatamapping(repository_uuid, display_name, map_from, map_to, enabled) values (rRef, 'publication title', 'dc.title', '0714', true);
insert into metadatamapping(repository_uuid, display_name, map_from, map_to, enabled) values (rRef, 'email adress', 'dc.contact', '0698', false);
insert into metadatamapping(repository_uuid, display_name, map_from, map_to, enabled) values (rRef, 'author', 'dc.author', '0567', true);
insert into metadatamapping(repository_uuid, display_name, map_from, map_to, enabled) values (rRef, 'second author', 'dc.other_author', '0568', true);
insert into metadatamapping(repository_uuid, display_name, map_from, map_to, enabled) values (rRef, 'archive link', 'dc.archive_link', '1045', true);

insert into item (eperson_uuid, source_uuid, archive_uuid, repository_uuid, item_type, item_state, in_archive, email_verified, date_created, date_last_modified)
  values (pRef, sRef, aRef, rRef, 'publication', 'created', false, false, now(), now()) returning uuid into iRef;

end $$; 
