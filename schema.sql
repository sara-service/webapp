-- database schema. needs to be manually imported into PostgeSQL.

create table fe_temp_metadata(
	repo text not null,
	project text not null,
	field text not null,
	value text not null,
	check (field in ('title', 'description', 'version', 'versionbranch',
		'pubrepo', 'collection', 'email')),
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
	preference integer not null default 2147483647, -- lower is earlier in list
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
-- cause any long-term consistency problems.
-- "on delete no action" is required to allow fe_supported_licenses to be
-- updated with delete + insert without wiping the fe_temp_licenses. it also
-- means that "delete from fe_supported_licenses where ..." will fail unless
-- the license is unused – but licenses should never be deleted anyway, only
-- set as hidden.
alter table fe_temp_licenses add constraint fe_temp_valid_license
	foreign key (license) references fe_supported_licenses(id)
	on delete no action on update cascade deferrable;
