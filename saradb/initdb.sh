#!/bin/bash -ex
BASEDIR=$(readlink -f $(dirname "$0"))
cd $BASEDIR

pg_dropcluster --stop 10 main
service postgresql restart
pg_createcluster --start 10 main

createuser -l -D -R -S test
createdb -E UTF8 -O test test
psql -d test -c "ALTER USER test WITH PASSWORD 'test'"
createuser -l -D -R -S admin
psql -d test -c "ALTER USER admin WITH PASSWORD 'admin'"

./schema.sh test test
psql -d test -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO admin"
./config.sh test devel

psql -d test
service postgresql stop
