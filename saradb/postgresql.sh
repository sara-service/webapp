#!/bin/bash -ex
if [ $0 != /init.sh ]; then
	echo you should be running this inside the container...
	echo try ./initdb.sh '!'
	exit 1
fi
trap "service postgresql stop" ERR HUP INT QUIT TERM

service postgresql start
createuser -l -D -R -S test
createdb -E UTF8 -O test test
psql -d test -c "ALTER USER test WITH PASSWORD 'test'"
echo 'localhost:*:test:test:test' >~/.pgpass
chmod 600 ~/.pgpass

psql -d test -f /saradb/adminconfig.sql
psql -d test -f /saradb/schema.sql
psql -d test -f /saradb/permissions.sql
psql -d test -f /saradb/config.sql
psql -d test -f /saradb/licenses.sql

psql -d test
service postgresql stop
