-- permission config for database tables in PostgreSQL.

-- rule of thumb: 
--  all tables granted select only should reside
--  all tables granted select, insert, update, delete should eventually be moved into publication sara DB!

ALTER USER test WITH PASSWORD 'test';

grant select, insert, update, delete on fe_temp_metadata to test;
grant select, insert, update, delete on fe_temp_actions to test;
grant select, insert, update, delete on fe_temp_licenses to test;
grant select, insert, update, delete on fe_supported_licenses to test;

grant select on public.source to test;
grant select on public.source_params to test;
grant select on public.repository to test;
grant select on public.repository_params to test;
grant select on public.archive to test;
grant select on public.archive_params to test;
--grant select on public.collection to test;
grant select on public.metadatamapping to test;

grant select, insert, update on public.eperson to test;
grant select, insert, update on public.item to test;
grant select, insert, update on public.metadatavalue to test;
