-- pg_dump -a -h localhost -d test -U test -t fe_archives -t fe_archive_params -t fe_gitrepos -t fe_gitrepo_params -f test.sql
SET client_encoding = 'UTF8';

COPY fe_gitrepos (id, display_name, adapter) FROM stdin;
arbeits-gitlab	Arbeits-GitLab (SARA Development)	GitLabRESTv4
\.
COPY fe_gitrepo_params (id, param, value) FROM stdin;
arbeits-gitlab	url	https://arbeits-gitlab.unikn.netfuture.ch
arbeits-gitlab	oauthID	d6f80baadb28e3d9d20b79f6a27c0747f6692a67321375e40be1a1b0fd8bb430
arbeits-gitlab	oauthSecret	6df3596f15aaa0b4e1ecd4297c697e42632b2cab4c4224bbdd6a34b9ed0674f1
\.

COPY fe_archives (id, adapter) FROM stdin;
default	GitLabArchiveRESTv4
\.
COPY fe_archive_params (id, param, value) FROM stdin;
default	url	https://testarchiv.unikn.netfuture.ch
default	temp-namespace	temp
default	main-namespace	archive
default	dark-namespace	dark-archive
default	token	9SMsxR11E3_fLWWwuKeY
default	private-key	-----BEGIN EC PRIVATE KEY-----\nMHcCAQEEIFCS1djlLXpPUyVEbuyhybZCPohmkNq1FHpMGDPbkR2BoAoGCCqGSM49\nAwEHoUQDQgAEYXueBAFBNvKheBV/CIYZ8Kgfyvk2L1UiDyrA7j49Gi6HpT2S0nq4\n0JhH/XBFN5XrIj1Kurl39iZIPTB84whb6Q==\n-----END EC PRIVATE KEY-----
default	public-key	ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBGF7ngQBQTbyoXgVfwiGGfCoH8r5Ni9VIg8qwO4+PRouh6U9ktJ6uNCYR/1wRTeV6yI9Srq5d/YmSD0wfOMIW+k= matthias@hydra
default	known-hosts	testarchiv.unikn.netfuture.ch ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNfwAQcp5oW/b0d/L9jcxIKYfYHmm3UU59s/MWryTPXiR2Q1tUlmMKcdgC/idLq/jverlf3/GTgrdDFcYYL1Blw= root@archiv-gitlab\ntestarchiv.unikn.netfuture.ch ssh-dss AAAAB3NzaC1kc3MAAACBAJeGdQJiNxQH9U24DzBOZRRaCQnjokHmogH3BvhbqMP33JXJrbvxgCHXvu4edTRFTtqv82u5LlZH6I4IBSaucREOhCOTkijE9lEJGPdCuCrwm7nJqUR2Y62ciRkmUy1sU3trfHLx2CSvd2t3w8oHd9LmVJlTLPA/xowEzkPqtSmzAAAAFQDGWrLs8HWetRsgjZBNzlZrfVZUQQAAAIA9RXF8/cXJ8rTTIPv8ZmL6UczF99IIuxi6zK4YnBl8//P6YuEVWjyM28UxOspJe/WVng+TQhukB791eTvbXYNwITSfz1lb8/WnNUpXD67htlePOMjHOlJ2iG0leO58fM1jugQARv0tVKuyngwHjX6gYbJHrqLeRHlz1xr/be906wAAAIA5MzCV0ubccqzTei6MFmtnc4G1j8prUfKPEwIYt7ewt7Ru+6hlGyPLrqRcEdCfrl6szv8n6wKN6Ov8fMmCPPLNbiA3641CCRvglR7in+16OHZn+QJ3HFS7w7Jcy7lqzmVbk246iGmBJXrHh4+pA7XsDWnFIP/56ZeXmVMqZqOUHA== root@archiv-gitlab\ntestarchiv.unikn.netfuture.ch ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDa6jZJ8l63KYa+6WSBNfuMdUAC0sPbup1OSEbJhzX6ACLvXOGFCW2CjQVYqez/F5KtwexEyYy0oW07XL3hlLbHZ1DXvZXBQUPDfo48XB6+FYJRlcW4CfDAqEJfQyDksu61BiUCCAnVL0TCtDvLKaauiDPEnZ/KBU4of5a0twpPRss6msOXz645S605l3eHDsFj0Sb98d2SB1fkqyGcxOpqesYaiEIkCVF2VxJGmz/QAvV4jKV255zKRodjDq8/QtPwPkZARvghKWydRK78gk2XJToUaRpyHCwAlHGwvNHnxn+Z4W2vqHXIbQeuVCjLyXA8FhnqccjUiz+5n1oYhMJJ root@archiv-gitlab
\.
