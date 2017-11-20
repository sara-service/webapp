-- database schema. needs to be manually imported into PostgeSQL.

create table frontend_metadata(
	repo text not null,
	project text not null,
	field text not null,
	value text not null,
	check (field in ('title', 'description', 'version', 'versionbranch')),
	primary key (repo, project, field)
);

create table frontend_licenses(
	repo text not null,
	project text not null,
	ref text not null,
	license text not null,
	primary key (repo, project, ref)
);

create table frontend_actions(
	repo text not null,
	project text not null,
	ref text not null,
	action text not null,
	start text not null,
	check (action in ('PUBLISH_FULL', 'PUBLISH_ABBREV', 'PUBLISH_LATEST',
			'ARCHIVE_PUBLIC', 'ARCHIVE_HIDDEN')),
	primary key (repo, project, ref)
);

create table supported_licenses(
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
create index on supported_licenses(hidden asc, preference asc, id asc);
-- only allow the frontend to set known licenses. the frontend_licenses
-- table is only used during the publication workflow, so this cannot
-- cuse any long-term consistency problems.
alter table frontend_licenses add foreign key (license)
	references supported_licenses(id) on delete cascade;

-- FIXME these should be populated from choosealicense.com...
insert into supported_licenses(id, display_name, preference, info_url, full_text)
	values('GPL-3.0', 'GNU General Public License v3.0', 2000,
		'https://choosealicense.com/licenses/gpl-3.0/', 'Lots of Text');
insert into supported_licenses(id, display_name, preference, info_url, full_text)
	values('LGPL-3.0', 'GNU Lesser General Public License v3.0', 3000,
		'https://choosealicense.com/licenses/lgpl-3.0/', 'Lost of Text as Well');
insert into supported_licenses(id, display_name, preference, info_url, full_text)
	values('AGPL-3.0', 'GNU Affero General Public License v3.0', 1000,
		'https://choosealicense.com/licenses/agpl-3.0/', 'Even More Text');
insert into supported_licenses(id, display_name, preference, info_url, full_text)
	values('MPL-2.0', 'Mozilla Public License 2.0', 4000,
		'https://choosealicense.com/licenses/mpl-2.0/', 'Also Quite Long');
insert into supported_licenses(id, display_name, preference, info_url, full_text)
	values('Apache-2.0', 'Apache License 2.0', 5000,
		'https://choosealicense.com/licenses/apache-2.0/', 'Not That Long Any More');
insert into supported_licenses(id, display_name, preference, info_url, full_text)
	values('MIT', 'MIT License', 6000,
		'https://choosealicense.com/licenses/mit/', 'Downright Short');
insert into supported_licenses(id, display_name, preference, info_url, full_text)
	values('Unlicense', 'The Unlicense', 7000,
		'https://choosealicense.com/licenses/unlicense/', 'Actually Longer Than MIT?!');
