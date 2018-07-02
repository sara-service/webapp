-- Naming
--
-- fe_temp_*: used for semi-temporary storage of information entered in frontend
-- source: a gitlab a user has been working on
-- archive: a gitlab a project is being archived to
-- repository: an institutional repository (dspace based) to be published/recorded to
-- metadata*: TBD (to be documented)
-- item: submitted item in IR; can be either
--   * archive: just being stored permanently in an archive (URL)
--   * record: archive URL + metadata (email, names, links)
--   * publication: record + arbitrary data + corresponding license

-- Table: archive
-- e.g. archive gitlab Konstanz
CREATE TABLE archive(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	display_name text NOT NULL, -- user-visible name of the repository
	url text NOT NULL, -- user-visible URL of repository
	contact_email text NOT NULL, -- an email address to contact the archive
	adapter text NOT NULL,
	logo_url text, -- URL of logo, either https:// or data: (optional)
	enabled boolean NOT NULL
);

-- Table: archive_params
-- e.g. oauth_id, oauth_secret ...
CREATE TABLE archive_params(
	id UUID REFERENCES archive(uuid) ON DELETE CASCADE,
	param text NOT NULL,
	value text NOT NULL,
	PRIMARY KEY (id, param)
);

-- Table: source
CREATE TABLE source(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	display_name text NOT NULL, -- user-visible name of the repository
	url text NOT NULL, -- user-visible URL of repository
	contact_email text NOT NULL, -- an email address to contact the source
	adapter text NOT NULL,
	logo_url text, -- URL of logo, either https:// or data: (optional)
	enabled boolean NOT NULL
);

-- Table: source_params
-- e.g. api_endpoint, oauth_id, oauth_secret ...
CREATE TABLE source_params(
	id UUID REFERENCES source(uuid) ON DELETE CASCADE,
	param text NOT NULL,
	value text NOT NULL,
	PRIMARY KEY (id, param)
);

-- Table: repository
CREATE TABLE repository(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	display_name text NOT NULL, -- user-visible name of the repository
	url text NOT NULL, -- user-visible URL of repository
	contact_email text NOT NULL, -- an email address to contact the repository
	adapter text NOT NULL,
	logo_url text, -- URL of logo, either https:// or data: (optional)
        help text, -- help for users how publication can be finished
	enabled boolean NOT NULL
);

-- Table: repository_params
-- e.g. version {query,submit}_{api_endpoint,user,pwd}, default_collection ...
CREATE TABLE repository_params(
	id UUID REFERENCES repository(uuid) ON DELETE CASCADE,
	param text NOT NULL,
	value text NOT NULL,
	PRIMARY KEY (id, param)
);

-- Table: item
CREATE TABLE item(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

	source_uuid UUID NOT NULL REFERENCES source(uuid) ON DELETE RESTRICT,
	archive_uuid UUID NOT NULL REFERENCES archive(uuid) ON DELETE RESTRICT,
	repository_uuid UUID REFERENCES repository(uuid) ON DELETE RESTRICT,

	source_user_id text NOT NULL, -- unique user ID in source
        repository_login_id text,
        meta_title text,
        meta_description text,
        meta_version text,
        meta_submitter text,
        archive_url text,
        repository_url text,
	contact_email text NOT NULL, -- email address to be used for contacting the user

	collection_id text, -- ID of collection in institutional repository
	item_id text, -- ID of submitted item in institutional repository

	date_created timestamp with time zone NOT NULL,
	date_last_modified timestamp with time zone NOT NULL,

	item_type text NOT NULL,
	item_state text NOT NULL,
	item_state_sent text NOT NULL, -- last state the user was informed about

	persistent_identifier text, -- DOI, URN, HDL, ...

	CHECK (item_type IN ('PUBLISH', 'ARCHIVE_PUBLIC', 'ARCHIVE_HIDDEN')),
	CHECK (item_state IN ('CREATED', 'VERIFIED', 'SUBMITTED', 'ACCEPTED', 'DONE', 'REJECTED', 'DELETED')),
	CHECK (item_state_sent IN ('CREATED', 'VERIFIED', 'SUBMITTED', 'ACCEPTED', 'DONE', 'REJECTED', 'DELETED'))
);
-- index to speed up the query for the artefacts archived by a particular user
CREATE INDEX ON item(source_uuid, source_user_id);

-- Table: metadatamapping (WIP)
CREATE TABLE metadatamapping(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	repository_uuid UUID REFERENCES repository(uuid) ON DELETE CASCADE,
	display_name text NOT NULL,
	map_from text NOT NULL,
	map_to text NOT NULL, -- foreign uuid
	remark text,
	enabled boolean NOT NULL,
	UNIQUE (repository_uuid, map_from, map_to)
);

-- Table: metadatavalue (WIP)
CREATE TABLE metadatavalue(
	item_uuid UUID REFERENCES item(uuid) ON DELETE CASCADE,
	metadatamapping_uuid UUID REFERENCES metadatamapping(uuid) ON DELETE SET NULL,
	map_from text NOT NULL,
	data text NOT NULL
);

-- temp storage of publication metadata fields for publication frontend
CREATE TABLE fe_temp_pubmeta(
	item_uuid UUID REFERENCES item(uuid) ON DELETE CASCADE,
	field text NOT NULL,
	value text NOT NULL,
	CHECK (field in ('title', 'description', 'version', 'pubrepo', 'pubrepo_displayname', 'collection', 'collection_displayname', 'email', 'submitter', 'repository_url', 'archive_url')),
	PRIMARY KEY (item_uuid, field)
);

-- branch selection
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

-- selected licenses per branch
CREATE TABLE fe_temp_licenses(
	repo text NOT NULL,
	project text NOT NULL,
	ref text NOT NULL,
	license text NOT NULL,
	PRIMARY KEY (repo, project, ref)
);

-- archiving metadata fields (ie. basic metadata only)
CREATE TABLE fe_temp_metadata(
	repo text NOT NULL,
	project text NOT NULL,
	field text NOT NULL,
	value text NOT NULL,
	CHECK (field in ('title', 'description', 'version', 'master', 'submitter')),
	PRIMARY KEY (repo, project, field)
);

-- list of licenses shown on license selection pages
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
-- "ON DELETE/UPDATE RESTRICT" effectively forbid removing or renaming licenses
-- that are references from fe_temp_licenses – but licenses should never be
-- deleted anyway, only set as hidden.
ALTER TABLE fe_temp_licenses ADD CONSTRAINT fe_temp_valid_license
	FOREIGN KEY (license) REFERENCES supported_licenses(id)
	ON DELETE RESTRICT ON UPDATE RESTRICT DEFERRABLE;
