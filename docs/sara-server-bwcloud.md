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
sudo -u postgres psql -c "ALTER USER sara WITH PASSWORD 'secret';"
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
    sed -f $DBBASEDIR/credentials/ulm.sed "$file" | sudo -u postgres psql -v ON_ERROR_STOP=on -d saradb -v "basedir=$DBBASEDIR";
done
```

### Apache
```
sudo apt install apache2 letsencrypt
sudo a2dissite 000-default
sudo a2enmod proxy_ajp ssl headers
```
Install TomCat Proxy
```
cat << EOF | sudo tee /etc/apache2/sites-available/proxy.conf
<VirtualHost *:80>
	ServerName ulm.sara-service.org # change
	ServerAdmin webmaster@localhost # change

	<Location />
		ProxyPass "ajp://localhost:8009/SaraServer/"
		ProxyPassReverseCookiePath "/SaraServer" "/"
	</Location>
	Alias "/.well-known" "/var/www/letsencrypt/.well-known"
	<Location /.well-known>
		ProxyPass !
	</Location>
	<Directory /var/www/letsencrypt/.well-known>
		Options -MultiViews
		Require all granted
	</Directory>

	# limit scripts, styles and fonts to same server only.
	# for images, allow local images, and https:* and data:* for logos.
	# disallow everything else.
	Header always set Content-Security-Policy "default-src 'none'; \
		script-src 'self'; style-src 'self' 'unsafe-inline'; \
		img-src 'self' https: data:; connect-src 'self'; font-src 'self'"
	# disallow frames (anti-clickjacking)
	Header always set X-Frame-Options deny
	# make sure XSS protection doesn't mess up ("sanitize") URLs
	Header always set X-Xss-Protection "1; mode=block"
	# turn of content type autodetection misfeature (major security risk)
	Header always set X-Content-Type-Options nosniff
	# turn of referrer for privacy
	Header always set Referrer-Policy no-referrer

	ErrorLog ${APACHE_LOG_DIR}/error.log
	CustomLog ${APACHE_LOG_DIR}/access.log combined
</VirtualHost>

EOF
```

### Tomcat
```
sudo apt-get install tomcat8 maven

cat << EOF | sudo tee /etc/tomcat8/server.xml
<?xml version='1.0' encoding='utf-8'?>
<Server port="8005" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
  <!-- Prevent memory leaks due to use of particular java/javax APIs-->
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <Service name="Catalina">
    <!-- AJP Connector on port 8009, configured analogous to the default HTTP connector. -->
    <Connector port="8009" address="127.0.0.1" protocol="AJP/1.3"
		connectionTimeout="20000" URIEncoding="UTF-8"
		redirectPort="8443" />
    <Engine name="Catalina" defaultHost="localhost">
      <!-- unpackWARs=true is needed on tomcat8; unpackWARs=false is ~50x slower -->
      <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true">
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access" suffix=".log" fileDateFormat=""
               pattern="%h %l %u %t &quot;%r&quot; %s %b"
               rotatable="false" checkExists="true" />
      </Host>
    </Engine>
  </Service>
</Server>
EOF
```

### SARA Server
```
cd ~/SARA-server
mvn clean package -DskipTests
sudo -u tomcat8 cp target/SaraServer-*.war /var/lib/tomcat8/webapps/SaraServer.war
sudo -u tomcat8 cp ~/.m2/repository/org/postgresql/postgresql/42.1.4/postgresql-42.1.4.jar /var/lib/tomcat8/lib
sudo -u tomcat8 cp ~/.m2/repository/org/apache/geronimo/specs/geronimo-javamail_1.4_spec/1.6/geronimo-javamail_1.4_spec-1.6.jar /var/lib/tomcat8/lib
sudo  cp src/main/webapp/META-INF/context.xml /etc/tomcat8/Catalina/localhost/SaraServer.xml
sudo sed -i 's/demo.sara-service.org/ulm.sara-service.org' /etc/tomcat8/Catalina/localhost/SaraServer.xml
/etc/tomcat8/Catalina/localhost/SaraServer.xml
```