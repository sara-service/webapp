-- Naming
--
--  eperson 
--    a person using SARA (e.g. for submission of publications)
--
--  source 
--    a gitlab a user has been working on
--
--  archive 
--    a gitlab a project is being archived to
--
--  repository 
--    an institutional repository (dspace based) to be published/recorded to
--
--  collection
--
--  metadata
--
--  item 
--    submission by an eperson; can be either 
--    * archive: just being stored permanently in an archive (URL)
--    * record: archive URL + metadata (email, names, links)
--    * publication: record + arbitrary data + corresponding license
	
-- Table: public.eperson
CREATE TABLE public.eperson
(
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_email text NOT NULL,
    password text, -- NULL: normal user, not NULL: admin user
    last_active timestamp without time zone
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Table: public.archive
-- e.g. archive gitlab Konstanz
CREATE TABLE public.archive
(
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name text NOT NULL,
    url          text NOT NULL,
    contact_email text NOT NULL,   -- an email address to contact the archive
    adapter      text NOT NULL,
    logo_base64   text,            -- base64-encoded "data:" url for logo (optional)
    enabled boolean NOT NULL
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Table: public.archive_params
-- e.g. oauth_id, oauth_secret ...
CREATE TABLE public.archive_params
(
    id UUID REFERENCES public.archive(uuid) on delete cascade,
    param   text NOT NULL,
    value   text NOT NULL,
    PRIMARY KEY (id, param)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Table: public.source
CREATE TABLE public.source
(
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name text NOT NULL,
    url          text NOT NULL,
    contact_email text NOT NULL,   -- an email address to contact the source
    adapter      text NOT NULL,
    logo_base64   text,            -- base64-encoded "data:" url for logo (optional)
    enabled boolean NOT NULL
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Table: public.source_params
-- e.g. api_endpoint, oauth_id, oauth_secret ...
CREATE TABLE public.source_params
(
    id UUID REFERENCES public.source(uuid) on delete cascade,
    param   text NOT NULL,
    value   text NOT NULL,
    PRIMARY KEY (id, param)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Table: public.repository
CREATE TABLE public.repository
(
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name  text NOT NULL,
    url           text NOT NULL,
    contact_email text NOT NULL,   -- an email address to contact the repository
    adapter       text NOT NULL,
    logo_base64   text,            -- logo in base64 encoding for direct usage in <img> element
    enabled boolean NOT NULL
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Table: public.repository_params
-- e.g. version {query,submit}_{api_endpoint,user,pwd}, default_collection ...
CREATE TABLE public.repository_params
(
    id UUID REFERENCES public.repository(uuid) ON DELETE cascade,
    param   text NOT NULL,
    value   text NOT NULL,
    PRIMARY KEY (id, param)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


-- Table: public.collection
--CREATE TABLE public.collection
--(
--    id UUID REFERENCES public.repository(uuid) ON DELETE cascade,
--    foreign_collection_uuid text NOT NULL, -- foreign collection uuid of the item submitted to a dspace repository
--    enabled boolean NOT NULL,
--    display_name text,
--    PRIMARY KEY (id, foreign_collection_uuid)
--)
--WITH (
--    OIDS = FALSE
--)
--TABLESPACE pg_default;


-- Table: public.item
CREATE TABLE public.item
(
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    eperson_uuid    UUID NOT NULL REFERENCES public.eperson(uuid)    ON DELETE restrict,
    source_uuid     UUID NOT NULL REFERENCES public.source(uuid)     ON DELETE restrict,
    archive_uuid    UUID NOT NULL REFERENCES public.archive(uuid)    ON DELETE restrict,

    repository_uuid UUID          REFERENCES public.repository(uuid) ON DELETE restrict,

    foreign_collection_uuid text,    -- foreign collection uuid of the item submitted to a dspace repository
    foreign_item_uuid       text,    -- foreign item uuid of the item submitted to a dspace repository

    date_created timestamp with time zone NOT NULL,
    date_last_modified timestamp with time zone NOT NULL,

    item_type  text NOT NULL,
    item_state text NOT NULL,

    in_archive boolean NOT NULL,
    email_verified boolean NOT NULL,

    persistent_identifier text -- DOI, URN, HDL, ...
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;


-- Table: public.metadatamapping
CREATE TABLE public.metadatamapping
(
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repository_uuid UUID REFERENCES public.repository(uuid) ON DELETE cascade,
    display_name text NOT NULL,
    map_from     text NOT NULL,
    map_to       text NOT NULL, -- foreign uuid
    remark       text,
    enabled      boolean NOT NULL,
    UNIQUE (repository_uuid, map_from, map_to)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

-- Table: public.metadatavalue
CREATE TABLE public.metadatavalue
(
    item_uuid UUID REFERENCES public.item ON DELETE cascade,
    metadatamapping_uuid UUID REFERENCES public.metadatamapping(uuid) ON DELETE SET NULL,
    map_from text NOT NULL,
    data text     NOT NULL
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

