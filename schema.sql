-- database schema. needs to be manually imported into PostgeSQL.

CREATE TABLE fe_temp_metadata(
	repo text NOT NULL,
	project text NOT NULL,
	field text NOT NULL,
	value text NOT NULL,
	CHECK (field in ('title', 'description', 'version', 'versionbranch',
		'pubrepo', 'collection', 'email')),
	PRIMARY KEY (repo, project, field)
);

CREATE TABLE fe_temp_licenses(
	repo text NOT NULL,
	project text NOT NULL,
	ref text NOT NULL,
	license text NOT NULL,
	PRIMARY KEY (repo, project, ref)
);

CREATE TABLE fe_temp_actions(
	repo text NOT NULL,
	project text NOT NULL,
	ref text NOT NULL,
	action text NOT NULL,
	start text NOT NULL,
	CHECK (action IN ('PUBLISH_FULL', 'PUBLISH_ABBREV', 'PUBLISH_LATEST',
			'ARCHIVE_PUBLIC', 'ARCHIVE_HIDDEN')),
	PRIMARY KEY (repo, project, ref)
);

CREATE TABLE supported_licenses(
	id text PRIMARY KEY,
	display_name text NOT NULL,
	info_url text,
	preference integer NOT NULL DEFAULT 2147483647, -- lower is earlier in list
	hidden boolean NOT NULL DEFAULT FALSE,
	full_text text NOT NULL,
	-- disallow reserved names used by frontend
	CHECK (id NOT IN ('keep', 'other', 'multi', ''))
);
-- if the database is smart, it will store the (large) full_text column
-- out of the primary table. if it isn't, this index should speed up
-- FrontendDatabase.getLicenses():
CREATE INDEX ON supported_licenses(hidden ASC, preference ASC, id ASC);
-- only allow the frontend to set known licenses. the fe_temp_licenses
-- table is only used during the publication workflow, so this cannot
-- cause any long-term consistency problems.
-- "ON DELETE NO ACTION" and "DEFERRABLE" are required to allow this table to
-- be updated with delete + insert without wiping the fe_temp_licenses. it
-- also means that "DELETE FROM fe_supported_licenses WHERE ..." will fail
-- unless the license is unused – but licenses should never be deleted anyway,
-- only set as hidden.
ALTER TABLE fe_temp_licenses ADD CONSTRAINT fe_temp_valid_license
	FOREIGN KEY (license) REFERENCES supported_licenses(id)
	ON DELETE NO ACTION ON UPDATE CASCADE DEFERRABLE;
