#!/bin/bash -ex

BASEDIR=$(readlink -f $(dirname "$0"))
cd $BASEDIR

cd $BASEDIR

pg_dropcluster --stop 10 main
service postgresql restart
pg_createcluster -p 5432 --start 10 main -- --nosync

./createdb.sh test test devel

psql -d test -c "ALTER USER test WITH PASSWORD 'test'"
createuser -l -D -R -S admin
psql -d test -c "ALTER USER admin WITH PASSWORD 'admin'"
psql -d test -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO admin"

psql -d test
service postgresql stop
