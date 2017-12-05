#!/bin/bash

if [ -f "udocker.py" ]; then
  echo "UDocker v1.1.1 is downloaded already"
else
  wget https://raw.githubusercontent.com/indigo-dc/udocker/v1.1.1/udocker.py
  chmod u+x udocker.py
fi

echo -n "check whether running instances exist before we restart the database..."
if [ "$(./udocker.py ps | grep c1t4r/sara-server-vre)" != "" ]; then
  echo "YES...killing them..."
  ./udocker.py ps | awk '/c1t4r.sara-server-vre/{print $1}' | xargs ./udocker.py rm
else
  echo "NOPE"
fi

echo -n "check whether no postgres instances are running..."
if [ "$(ps aux | grep post[g]res)" != "" ]; then
  echo "YES...killing them..."
  echo "ps aux | grep pos[t]gres | awk '{print \$2}' | xargs kill"
  ps aux | grep pos[t]gres | awk '{print $2}' | xargs kill
else
  echo "NOPE"
fi

echo "starting database..."

# TODO mv all sql files into saradb git repo!
./udocker.py \
  run \
  -v $PWD/saradb/crypto.sql:/crypto.sql \
  -v $PWD/saradb/schema.sql:/schema.sql \
  -v $PWD/saradb/config.sql:/config.sql \
  -v $PWD/schema.sql:/schema-kn.sql \
  -v $PWD/permissions.sql:/permissions.sql \
  -i -t --rm --user postgres \
  c1t4r/sara-server-vre \
  sh -c '/etc/init.d/postgresql start && createuser -d -l -R -S test && createdb -E UTF8 -O test test && psql -d test -f /crypto.sql && psql -d test -f /schema.sql && psql -d test -f /config.sql && psql -d test -f /schema-kn.sql && psql -f /permissions.sql && psql -d test && /etc/init.d/postgresql stop'


#./udocker.py \
# run \
# -v /tmp/sara:/home/postgres \
# -v $PWD/schema-pub.sql:/home/postgres/schema-pub.sql \
# -v $PWD/schema.sql:/home/postgres/schema.sql \
# -v $PWD/permissions.sql:/home/postgres/permissions.sql \
# -i -t --rm --user postgres \
# c1t4r/sara-server-vre \
# sh -c '/etc/init.d/postgresql start && /usr/bin/createuser -d -l -R -P -S test && /usr/bin/createdb -E UTF8 -O test test && psql && /etc/init.d/postgresql stop'

#./udocker.py  run  -v /tmp/sara:/home/postgres  -v $PWD/schema-pub.sql:/home/postgres/schema-pub.sql  -v $PWD/schema.sql:/home/postgres/schema.sql  -v $PWD/permissions.sql:/home/postgres/permissions.sql  -i -t --rm --user postgres  c1t4r/sara-server-vre  sh -c '/etc/init.d/postgresql start && /usr/bin/createuser -d -l -R -P -S test && /usr/bin/createdb -E UTF8 -O test test && psql -f /home/postgres/schema-pub.sql && psql -f /home/postgres/schema.sql && psql && /etc/init.d/postgresql stop'

#sudo -u postgres psql postgres -f schema-pub.sql
#sudo -u postgres psql test -c "alter user test password 'test'"
#sudo -u postgres psql test -f schema.sql
#sudo -u postgres psql test -f permissions.sql

#./udocker.py run -i -t --hostenv --user 1000:1000 --rm katakombi/sara-server-dev sh -c '/opt/eclipse/eclipse -configuration /tmp/sara/eclipseconfig -data /tmp/sara'
