#!/bin/bash -ex
BASEDIR=$(readlink -f $(dirname $0))
cd $BASEDIR
DB=$1
ENV=$2

for file in "$BASEDIR/$ENV"/*.sql; do
	psql -d $DB -v "basedir=$BASEDIR/$ENV" -f "$file"
done
