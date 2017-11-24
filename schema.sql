-- database schema. needs to be manually imported into PostgeSQL.

create table fe_temp_metadata(
	repo text not null,
	project text not null,
	field text not null,
	value text not null,
	check (field in ('title', 'description', 'version', 'versionbranch')),
	primary key (repo, project, field)
);

create table fe_temp_licenses(
	repo text not null,
	project text not null,
	ref text not null,
	license text not null,
	primary key (repo, project, ref)
);

create table fe_temp_actions(
	repo text not null,
	project text not null,
	ref text not null,
	action text not null,
	start text not null,
	check (action in ('PUBLISH_FULL', 'PUBLISH_ABBREV', 'PUBLISH_LATEST',
			'ARCHIVE_PUBLIC', 'ARCHIVE_HIDDEN')),
	primary key (repo, project, ref)
);

create table fe_supported_licenses(
	id text not null,
	display_name text not null,
	info_url text,
	preference integer default 2147483647, -- lower is earlier in list
	hidden boolean default false,
	full_text text not null,
	-- disallow reserved names used by frontend
	check (id not in ('keep', 'other', 'multi', '')),
	primary key (id)
);
-- if the database is smart, it will store the (large) full_text column
-- out of the primary table. if it isn't, this index should speed up
-- FrontendDatabase.getLicenses():
create index on fe_supported_licenses(hidden asc, preference asc, id asc);
-- only allow the frontend to set known licenses. the fe_temp_licenses
-- table is only used during the publication workflow, so this cannot
-- cuse any long-term consistency problems.
alter table fe_temp_licenses add foreign key (license)
	references fe_supported_licenses(id) on delete cascade;

-- FIXME these should be populated from choosealicense.com...
insert into fe_supported_licenses(id, display_name, preference, info_url, full_text)
	values('GPL-3.0', 'GNU General Public License v3.0', 2000,
		'https://choosealicense.com/licenses/gpl-3.0/', 'Lots of Text');
insert into fe_supported_licenses(id, display_name, preference, info_url, full_text)
	values('LGPL-3.0', 'GNU Lesser General Public License v3.0', 3000,
		'https://choosealicense.com/licenses/lgpl-3.0/', 'Lost of Text as Well');
insert into fe_supported_licenses(id, display_name, preference, info_url, full_text)
	values('AGPL-3.0', 'GNU Affero General Public License v3.0', 1000,
		'https://choosealicense.com/licenses/agpl-3.0/', 'Even More Text');
insert into fe_supported_licenses(id, display_name, preference, info_url, full_text)
	values('MPL-2.0', 'Mozilla Public License 2.0', 4000,
		'https://choosealicense.com/licenses/mpl-2.0/', 'Also Quite Long');
insert into fe_supported_licenses(id, display_name, preference, info_url, full_text)
	values('Apache-2.0', 'Apache License 2.0', 5000,
		'https://choosealicense.com/licenses/apache-2.0/', 'Not That Long Any More');
insert into fe_supported_licenses(id, display_name, preference, info_url, full_text)
	values('MIT', 'MIT License', 6000,
		'https://choosealicense.com/licenses/mit/', 'Downright Short');
insert into fe_supported_licenses(id, display_name, preference, info_url, full_text)
	values('Unlicense', 'The Unlicense', 7000,
		'https://choosealicense.com/licenses/unlicense/', 'Actually Longer Than MIT?!');

create table fe_gitrepos(
	id text not null,
	display_name text not null,
	adapter text not null,
	primary key (id)
);
create table fe_gitrepo_params(
	id text not null references fe_gitrepos(id) on delete cascade,
	param text not null,
	value text,
	primary key (id, param)
);

insert into fe_gitrepos(id, display_name, adapter)
	values('arbeits-gitlab', 'Arbeits-GitLab (SARA Development)', 'GitLabRESTv4');
insert into fe_gitrepo_params(id, param, value)
	values('arbeits-gitlab', 'url', 'https://arbeits-gitlab.unikn.netfuture.ch');
insert into fe_gitrepo_params(id, param, value)
	values('arbeits-gitlab', 'oauthID', 'd6f80baadb28e3d9d20b79f6a27c0747f6692a67321375e40be1a1b0fd8bb430');
insert into fe_gitrepo_params(id, param, value)
	values('arbeits-gitlab', 'oauthSecret', '6df3596f15aaa0b4e1ecd4297c697e42632b2cab4c4224bbdd6a34b9ed0674f1');

create table fe_archives(
	id text not null,
	adapter text not null,
	primary key (id)
);
create table fe_archive_params(
	id text not null references fe_archives(id) on delete cascade,
	param text not null,
	value text,
	primary key (id, param)
);

insert into fe_archives(id, adapter) values('default', 'GitLabArchiveRESTv4');
insert into fe_archive_params(id, param, value)
	values('default', 'url', 'https://testarchiv.unikn.netfuture.ch');
insert into fe_archive_params(id, param, value)
	values('default', 'temp-namespace', 'temp');
insert into fe_archive_params(id, param, value)
	values('default', 'main-namespace', 'archive');
insert into fe_archive_params(id, param, value)
	values('default', 'dark-namespace', 'dark-archive');
insert into fe_archive_params(id, param, value)
	values('default', 'token', '9SMsxR11E3_fLWWwuKeY');
insert into fe_archive_params(id, param, value)
	values('default', 'private-key', '-----BEGIN EC PRIVATE KEY-----' || chr(10) ||
		'MHcCAQEEIFCS1djlLXpPUyVEbuyhybZCPohmkNq1FHpMGDPbkR2BoAoGCCqGSM49' || chr(10) ||
		'AwEHoUQDQgAEYXueBAFBNvKheBV/CIYZ8Kgfyvk2L1UiDyrA7j49Gi6HpT2S0nq4' || chr(10) ||
		'0JhH/XBFN5XrIj1Kurl39iZIPTB84whb6Q==' || chr(10) ||
		'-----END EC PRIVATE KEY-----');
insert into fe_archive_params(id, param, value)
	values('default', 'public-key', 'ecdsa-sha2-nistp256 ' ||
		'AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBGF7ngQBQTby' ||
		'oXgVfwiGGfCoH8r5Ni9VIg8qwO4+PRouh6U9ktJ6uNCYR/1wRTeV6yI9Srq5d/Ym' ||
		'SD0wfOMIW+k= matthias@hydra');
insert into fe_archive_params(id, param, value)
	values('default', 'known-hosts',
		'testarchiv.unikn.netfuture.ch ecdsa-sha2-nistp256 ' ||
		'AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNfwAQcp5oW/' ||
		'b0d/L9jcxIKYfYHmm3UU59s/MWryTPXiR2Q1tUlmMKcdgC/idLq/jverlf3/GTgr' ||
		'dDFcYYL1Blw= root@archiv-gitlab' || chr(10) ||
		'testarchiv.unikn.netfuture.ch ssh-dss ' ||
		'AAAAB3NzaC1kc3MAAACBAJeGdQJiNxQH9U24DzBOZRRaCQnjokHmogH3BvhbqMP3' ||
		'3JXJrbvxgCHXvu4edTRFTtqv82u5LlZH6I4IBSaucREOhCOTkijE9lEJGPdCuCrw' ||
		'm7nJqUR2Y62ciRkmUy1sU3trfHLx2CSvd2t3w8oHd9LmVJlTLPA/xowEzkPqtSmz' ||
		'AAAAFQDGWrLs8HWetRsgjZBNzlZrfVZUQQAAAIA9RXF8/cXJ8rTTIPv8ZmL6UczF' ||
		'99IIuxi6zK4YnBl8//P6YuEVWjyM28UxOspJe/WVng+TQhukB791eTvbXYNwITSf' ||
		'z1lb8/WnNUpXD67htlePOMjHOlJ2iG0leO58fM1jugQARv0tVKuyngwHjX6gYbJH' ||
		'rqLeRHlz1xr/be906wAAAIA5MzCV0ubccqzTei6MFmtnc4G1j8prUfKPEwIYt7ew' ||
		't7Ru+6hlGyPLrqRcEdCfrl6szv8n6wKN6Ov8fMmCPPLNbiA3641CCRvglR7in+16' ||
		'OHZn+QJ3HFS7w7Jcy7lqzmVbk246iGmBJXrHh4+pA7XsDWnFIP/56ZeXmVMqZqOU' ||
		'HA== root@archiv-gitlab' || chr(10) ||
		'testarchiv.unikn.netfuture.ch ssh-rsa ' ||
		'AAAAB3NzaC1yc2EAAAADAQABAAABAQDa6jZJ8l63KYa+6WSBNfuMdUAC0sPbup1O' ||
		'SEbJhzX6ACLvXOGFCW2CjQVYqez/F5KtwexEyYy0oW07XL3hlLbHZ1DXvZXBQUPD' ||
		'fo48XB6+FYJRlcW4CfDAqEJfQyDksu61BiUCCAnVL0TCtDvLKaauiDPEnZ/KBU4o' ||
		'f5a0twpPRss6msOXz645S605l3eHDsFj0Sb98d2SB1fkqyGcxOpqesYaiEIkCVF2' ||
		'VxJGmz/QAvV4jKV255zKRodjDq8/QtPwPkZARvghKWydRK78gk2XJToUaRpyHCwA' ||
		'lHGwvNHnxn+Z4W2vqHXIbQeuVCjLyXA8FhnqccjUiz+5n1oYhMJJ ' ||
		'root@archiv-gitlab');
