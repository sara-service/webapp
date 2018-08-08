#!/bin/bash -ex
BASEDIR=$(readlink -f $(dirname "$0"))
cd "$BASEDIR"
DB=$1
USER=$2

psql -d $DB -f ./adminconfig.sql
psql -d $DB -f ./schema.sql
sed "s/__USERNAME__/$USER/g" permissions.sql | psql -d $DB
# FIXME should just run the license updater instead!
psql -d $DB -f ./licenses.sql
