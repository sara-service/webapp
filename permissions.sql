-- permission config for database tables in PostgreSQL.

-- database setup for PostgreSQL:
-- sudo mkdir -p /var/lib/tomcat7/lib
-- sudo mv postgresql-42.1.1.jar /var/lib/tomcat7/lib
-- sudo -u postgres createuser -D -I -S sara
-- sudo -u postgres createdb sara
-- sudo -u postgres psql sara -c "alter user sara password 'secret'"
-- sudo -u postgres psql sara -f schema.sql
-- sudo -u postgres psql sara -f permissions.sql

-- frontend only needs CRUD
grant select, insert, update, delete on frontend_metadata to sara;
grant select, insert, update, delete on frontend_actions to sara;
