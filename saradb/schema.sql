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
	user_hint text, -- shown on publish page where user selects the IR
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

	-- source stuff
	source_uuid UUID NOT NULL REFERENCES source(uuid) ON DELETE RESTRICT,
	source_user_id text NOT NULL, -- unique user ID in source
	contact_email text NOT NULL, -- email address from source

	-- metadata
	title text NOT NULL,
	description text NOT NULL,
	version text NOT NULL,
	master text NOT NULL,
	submitter_surname text NOT NULL,
	submitter_givenname text NOT NULL,

	-- archive stuff
	archive_uuid UUID NOT NULL REFERENCES archive(uuid) ON DELETE RESTRICT,
	archive_url text,
	is_public boolean NOT NULL,
	token text,
	date_created timestamp with time zone NOT NULL,
	CHECK (is_public OR token IS NOT NULL)
);
CREATE TABLE item_authors(
	item_uuid UUID NOT NULL REFERENCES item(uuid) ON DELETE RESTRICT,
	seq integer NOT NULL, -- sequence number to preserve order of authors
	surname text,
	givenname text,
	CHECK (seq >= 0),
	PRIMARY KEY (item_uuid, seq) -- keep "seq" last so query can use the index!
);
-- index to speed up the query for all artefacts archived by a particular user
CREATE INDEX ON item(source_uuid, source_user_id);

CREATE TABLE item_publication(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	item_uuid UUID NOT NULL REFERENCES item(uuid) ON DELETE RESTRICT,

	-- stuff entered on publication page
	repository_uuid UUID NOT NULL REFERENCES repository(uuid) ON DELETE RESTRICT,
	repository_url text NOT NULL,
	repository_login_id text NOT NULL,
	collection_id text NOT NULL, -- ID of collection in institutional repository

	-- stuff populated after triggering submission
	item_id text, -- ID of submitted item in institutional repository
	persistent_identifier text, -- DOI, URN, HDL, ...

	-- state tracking
	date_created timestamp with time zone NOT NULL,
	date_last_modified timestamp with time zone NOT NULL,
	item_state text NOT NULL,

	CHECK (item_state IN ('CREATED', 'VERIFIED', 'SUBMITTED', 'ACCEPTED', 'DONE', 'REJECTED', 'DELETED'))
	-- TODO: CHECK ((state = not yet submitted) OR (repository_uuid IS NOT NULL AND repository_url IS NOT NULL AND repository_login_id IS NOT NULL AND collection_id IS NOT NULL AND item_id IS NOT NULL))
	-- TODO: CHECK ((state = identifier not yet assigned) OR (persistent_identifier IS NOT NULL))
);
-- index to allow fast join by item_uuid when listing publication per user
CREATE INDEX ON item_publication(item_uuid);

-- Table: metadatamapping
CREATE TABLE metadatamapping(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	repository_uuid UUID REFERENCES repository(uuid) ON DELETE CASCADE,
	display_name text NOT NULL,
	map_from text NOT NULL,
	map_to text NOT NULL,
	remark text,
	enabled boolean NOT NULL,
	CHECK ( map_from IN ('sara-author', 'sara-submitter', 'sara-abstract', 'sara-dateArchived', 'sara-version', 'sara-title', 'sara-archiveUrl', 'sara-publisher', 'sara-type') ),
	UNIQUE (repository_uuid, map_from, map_to)
);

-- Table: metadatavalue (WIP)
CREATE TABLE metadatavalue(
	item_uuid UUID REFERENCES item(uuid) ON DELETE CASCADE,
	metadatamapping_uuid UUID REFERENCES metadatamapping(uuid) ON DELETE SET NULL,
	map_from text NOT NULL,
	value text NOT NULL,
	UNIQUE (item_uuid, metadatamapping_uuid, map_from, value)
);

-- branch selection
CREATE TABLE fe_temp_actions(
	repo text NOT NULL,
	project text NOT NULL,
	uid text NOT NULL,
	ref text NOT NULL,
	action text NOT NULL,
	start text NOT NULL,
	CHECK (action IN ('FULL', 'ABBREV', 'LATEST')),
	PRIMARY KEY (repo, project, uid, ref)
);

-- selected licenses per branch
CREATE TABLE fe_temp_licenses(
	repo text NOT NULL,
	project text NOT NULL,
	uid text NOT NULL,
	ref text NOT NULL,
	license text NOT NULL,
	PRIMARY KEY (repo, project, uid, ref)
);

-- archiving metadata fields (ie. basic metadata only)
CREATE TABLE fe_temp_metadata(
	repo text NOT NULL,
	project text NOT NULL,
	uid text NOT NULL,
	title text,
	description text,
	version text,
	master text,
	submitter_surname text,
	submitter_givenname text,
	PRIMARY KEY (repo, project, uid)
);
CREATE TABLE fe_temp_authors(
	repo text NOT NULL,
	project text NOT NULL,
	uid text NOT NULL,
	seq integer NOT NULL, -- sequence number to preserve order of authors
	surname text,
	givenname text,
	CHECK (seq >= 0),
	FOREIGN KEY (repo, project, uid) REFERENCES fe_temp_metadata(repo, project, uid)
		ON DELETE CASCADE ON UPDATE CASCADE, -- no zombie authors please!
	PRIMARY KEY (repo, project, uid, seq) -- keep "seq" last for fast query!
);

-- archive access (and archive selection, at some point)
CREATE TABLE fe_temp_archive(
	repo text NOT NULL,
	project text NOT NULL,
	uid text NOT NULL,
	access text NOT NULL,
	CHECK (access in ('public', 'private')),
	PRIMARY KEY (repo, project, uid)
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
