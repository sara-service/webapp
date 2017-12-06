#!/bin/bash -ex
if [ $0 != /init.sh ]; then
	echo you should be running this inside the container...
	echo try ./initdb.sh '!'
	exit 1
fi
trap "service postgresql stop" ERR HUP INT QUIT TERM

service postgresql start
createuser -d -l -R -S test
createdb -E UTF8 -O test test
psql -d test -f /init.sql
psql -d test
service postgresql stop
