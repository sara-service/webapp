#!/bin/bash -ex
BASEDIR=$(readlink -f $(dirname "$0"))
cd $BASEDIR

createuser -l -D -R -S sara
createdb -E UTF8 -O sara sara
psql -d sara -c "ALTER USER sara WITH PASSWORD '6shnatBab_'"
createuser -l -D -R -S admin
psql -d sara -c "ALTER USER admin WITH PASSWORD 'Tirk)que9quis'"

./schema.sh sara sara
psql -d sara -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO admin"
./config.sh sara test
