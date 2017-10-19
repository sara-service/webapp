-- database schema. auto-loaded for HSQL; for everything else, run it
-- manually or have the DBA run it.

SET DATABASE SQL SYNTAX PGS TRUE;

create table frontend_metadata(
	repo text not null,
	project text not null,
	field text not null,
	value text,
	autodetected text,
	check (field in ('title', 'description', 'version',
			'versionbranch')),
	primary key (repo, project, field)
);

create table frontend_actions(
	repo text not null,
	project text not null,
	ref text not null,
	action text not null,
	start text not null,
	check (action in ('PUBLISH_FULL', 'PUBLISH_ABBREV',
			'PUBLISH_LATEST', 'ARCHIVE_PUBLIC', 'ARCHIVE_HIDDEN')),
	primary key (repo, project, ref)
);
