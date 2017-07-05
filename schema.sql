-- database schema. auto-loaded for HSQL; for everything else, run it
-- manually or have the DBA run it.

-- note: lowest common denominator of PostgreSQL, MySQL and HSQL in
-- terms of data types is INTEGER (not INT!), VARCHAR (no TEXT!) and
-- BOOLEAN.

create table frontend_metadata(
	repo varchar(32) not null,
	project varchar(256) not null,
	field varchar(11) not null,
	value varchar(10000),
	auto boolean not null,
	check (field in ('title', 'description', 'version', 'license',
			'source-ref')),
	primary key (repo, project, field)
);

create table frontend_actions(
	repo varchar(32) not null,
	project varchar(256) not null,
	ref varchar(256) not null,
	action varchar(14) not null,
	start varchar(40) not null,
	check (action in ('PUBLISH_FULL', 'PUBLISH_ABBREV',
			'PUBLISH_LATEST', 'ARCHIVE_PUBLIC', 'ARCHIVE_HIDDEN')),
	primary key (repo, project, ref)
);
