-- Naming
--
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
	logo_base64 text, -- base64-encoded "data:" url for logo (optional)
	enabled boolean NOT NULL
);

-- Table: archive_params
-- e.g. oauth_id, oauth_secret ...
CREATE TABLE archive_params(
	id UUID REFERENCES archive(uuid) on delete cascade,
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
	logo_base64 text, -- base64-encoded "data:" url for logo (optional)
	enabled boolean NOT NULL
);

-- Table: source_params
-- e.g. api_endpoint, oauth_id, oauth_secret ...
CREATE TABLE source_params(
	id UUID REFERENCES source(uuid) on delete cascade,
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
	logo_base64 text, -- logo in base64 encoding for direct usage in <img> element
	enabled boolean NOT NULL
);

-- Table: repository_params
-- e.g. version {query,submit}_{api_endpoint,user,pwd}, default_collection ...
CREATE TABLE repository_params(
	id UUID REFERENCES repository(uuid) ON DELETE cascade,
	param text NOT NULL,
	value text NOT NULL,
	PRIMARY KEY (id, param)
);

-- Table: item
CREATE TABLE item(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),

	source_uuid UUID NOT NULL REFERENCES source(uuid) ON DELETE restrict,
	archive_uuid UUID NOT NULL REFERENCES archive(uuid) ON DELETE restrict,
	repository_uuid UUID REFERENCES repository(uuid) ON DELETE restrict,

	contact_email text NOT NULL,

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

-- Table: metadatamapping (WIP)
CREATE TABLE metadatamapping(
	uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	repository_uuid UUID REFERENCES repository(uuid) ON DELETE cascade,
	display_name text NOT NULL,
	map_from text NOT NULL,
	map_to text NOT NULL, -- foreign uuid
	remark text,
	enabled boolean NOT NULL,
	UNIQUE (repository_uuid, map_from, map_to)
);

-- Table: metadatavalue (WIP)
CREATE TABLE metadatavalue(
	item_uuid UUID REFERENCES item ON DELETE cascade,
	metadatamapping_uuid UUID REFERENCES metadatamapping(uuid) ON DELETE SET NULL,
	map_from text NOT NULL,
	data text NOT NULL
);
