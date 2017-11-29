#!/bin/bash

# requires on ubuntu 16.10: apt-get install pgcrypto postgresql-9.5

git submodule update saradb
cat saradb/create-sara-db.sql | sed 's/sara/test/g' > schema-pub.sql

sudo -u postgres psql postgres -f schema-pub.sql
sudo -u postgres psql test -c "alter user test password 'test'"
sudo -u postgres psql test -f schema.sql
sudo -u postgres psql test -f permissions.sql
