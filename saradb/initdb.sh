#!/bin/bash

env|grep SINGULARITY_CONTAINER && 
  RD="/udocker" ||
  RD="$HOME/.udocker" 

mkdir -p $(readlink -f $RD/containers)

if [ -f "udocker.py" ]; then
  echo "UDocker v1.1.1 is downloaded already"
else
  wget https://raw.githubusercontent.com/indigo-dc/udocker/v1.1.1/udocker.py
  chmod u+x udocker.py
fi

echo -n "check whether running instances exist before we restart the database..."
if [ "$(./udocker.py ps | grep c1t4r/sara-server-vre)" != "" ]; then
  echo "YES...killing them..."
  ./udocker.py --repo=$RD ps | awk '/c1t4r.sara-server-vre/{print $1}' | xargs ./udocker.py rm
else
  echo "NOPE"
fi

echo -n "check whether any postgres instances are running..."
if [ "$(ps aux | grep post[g]res)" != "" ]; then
  echo "YES...killing them..."
  echo "ps aux | grep pos[t]gres | awk '{print \$2}' | xargs kill"
  ps aux | grep pos[t]gres | awk '{print $2}' | xargs kill
else
  echo "NOPE"
fi

echo "starting database..."
exec ./udocker.py --repo=$RD run \
  -v $PWD:/saradb \
  -v $PWD/postgresql.sh:/init.sh \
  -i -t --rm --user postgres \
  c1t4r/sara-server-vre \
  /init.sh
