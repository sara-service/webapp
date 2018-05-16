# SARA DB Setup (for production)

- create a user for SARA-server: `createuser -l -D -R -S test`
- create a database for SARA-server: `createdb -E UTF8 -O test test`
- in the database, run `adminconfig.sql` as PostgreSQL admin: `psql -d test -f /saradb/adminconfig.sql`
- in the database, run `schema.sql` as a user with DDL rights (such as the PostgreSQL admin): `psql -d test -f /saradb/schema.sql`
- edit `permissions.sql` and change the user name `test` to the user SARA-server is connecting as
- in the database, run the modified `permissions.sql` as PostgreSQL admin: `psql -d test -f /saradb/permissions.sql`
- temporarily set `supported_licenses` to be writeable: `psql -d test -c 'GRANT INSERT, DELETE ON supported_licenses TO test'`
- run `bwfdm.sara.db.util.UpdateLicenses` to update the license list
- restore `supported_licenses` to correct permissions: `psql -d test -c 'REVOKE INSERT, DELETE ON supported_licenses FROM test'`
- configure a few git repos, git archives and institutional repositories

# SARA DB Setup (for development)

- run `./initdb.sh`
- if anything goes wrong, ask a developer
