#!/bin/bash

git submodule update saradb
cat saradb/create-sara-db.sql | sed 's/sara/test/g' > schema-pub.sql
rm -rf $TD

#sudo /etc/init.d/postgres-xc restart
sudo -u postgres-xc psql postgres -f schema-pub.sql
sudo -u postgres-xc psql test -c "alter user test password 'test'"
sudo -u postgres-xc psql test -f schema.sql
sudo -u postgres-xc psql test -f permissions.sql
