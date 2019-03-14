# How to Install Sara Server on bwCloud Scope

## Intro

This manual provides a step-by-step setup for a fully configured instance Sara server. 
It is advised to walk through this manual without interruptions or intermediate reboots.

About SARA:
https://sara-service.org

In case of questions please contact:
* Stefan Kombrink, Ulm University, Germany / e-mail: stefan.kombrink[at]uni-ulm.de
* Matthias Fratz, University of Constance / email: matthias.fratz[at]uni-konstanz.de
* Franziska Rapp, Ulm University, Germany / e-mail: franziska.rapp[at]uni-ulm.de

## Requirements

You will need
* a Source GitLab (or GitHub)
* a DSpace-Repository
* an Archive GitLab

## Setup 

### Create a virtual machine (e.g. an instance on the bwCloud):

  * https://portal.bw-cloud.org
  * Compute -> Instances -> Start new instance
  * Use "Ubuntu Server 18.04 Minimal" image
  * Use flavor "m1.medium" with 12GB disk space and 4GB RAM
  * Enable port 8080 egress/ingress by creating and enabling a new Security Group 'tomcat'
  * Enable port 80/443 egress/ingress by creating and enabling a new Security Group 'apache'

### In case you have an running instance already which you would like to replace

 * https://portal.bw-cloud.org
 * Compute -> Instances -> "sara-server" -> [Rebuild Instance]
 * You might need to remove your old SSH key from ~/.ssh/known_hosts
 
 ### Setup DNS
 * create a DNS record for your machine, here: `ulm.sara-service.org`

### Connect to the machine
```bash
ssh -A ubuntu@ulm.sara-service.org
```

## Fix hostname
```
# Enable history search (pgdn/pgup)
sudo sed -i.orig '41,+1s/^# //' /etc/inputrc

# Adapt host name
sudo hostname ulm.sara-service.org

# Log off to apply new host name
exit
```

## Prerequisites
```
ssh ubuntu@ulm.sara-service.org

# Fetch latest updates
sudo apt-get update

# Install some important dependencies
sudo apt-get -y install vim git locales rsync

# Fix locales
sudo locale-gen de_DE.UTF-8 en_US.UTF-8
sudo localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8

# Fix timezone
sudo sh -c 'echo "Europe/Berlin" > /etc/timezone'
sudo DEBIAN_FRONTEND=noninteractive DEBCONF_NONINTERACTIVE_SEEN=true apt-get install tzdata

# Upgrade all packages
sudo apt-get -y upgrade
```
```
# Clone Sara Server code from git
# FIXME this has to work with https:// and gain access to the private repo credentials
git clone -b master git@git.uni-konstanz.de:sara/SARA-server.git
cd SARA-server && git submodule update --init
```

## Installation
### Postgres
```
sudo apt-get install postgresql
sudo systemctl start postgresql
sudo -u postgres createuser -l -D -R -S sara
sudo -u postgres psql -c "ALTER USER sara WITH PASSWORD 'sara';"
sudo -u postgres createdb -E UTF8 -O sara saradb
sudo -u postgres psql -d saradb -f ~/SARA-server/saradb/adminconfig.sql
sudo -u postgres psql -d saradb -f ~/SARA-server/saradb/schema.sql
sed "s/__USERNAME__/sara/g" ~/SARA-server/saradb/permissions.sql | sudo -u postgres psql -d saradb
sudo -u postgres psql -d saradb -f ~/SARA-server/saradb/licenses.sql
```
Create configuration according to `saradb/ulm` subdirectory
```
DBBASEDIR="$HOME/SARA-server/saradb"
for file in $DBBASEDIR/ulm/*.sql; do
    sed -f $DBBASEDIR/credentials/ulm.sed "$file" | psql -v ON_ERROR_STOP=on -d saradb -v "basedir=$DBBASEDIR"
done
```