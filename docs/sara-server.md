# Deploying the SARA Server Webapp

## Clone source code
```bash
    cd $HOME
    git clone https://github.com/sara-service/webapp SARA-server
    cd ~/SARA-server

```
## Set up PostgreSQL

- install PostgreSQL (`sudo apt install postgresql`)
- create user: 
```bash
SARADB_USER="sara"
sudo -u postgres createuser -l -D -R -S $SARADB_USER

```
- set a password: `sudo -u postgres psql -c "ALTER USER $SARADB_USER WITH PASSWORD 'sarapassword'"` (run `apg` to create a good one)
- create database: 
```bash
SARADB="sara"
sudo -u postgres createdb -E UTF8 -O $SARADB_USER $SARADB

```
- enable `pgcrypto` extension: `sudo -u postgres psql -d $SARADB -f saradb/adminconfig.sql`
- create tables: `sudo -u postgres psql -d $SARADB -f saradb/schema.sql`
- grant permissions: `sed "s/__USERNAME__/$SARADB_USER/g" saradb/permissions.sql | sudo -u postgres psql -d $SARADB`
- import licenses: `sudo -u postgres psql -d $SARADB -f saradb/licenses.sql`
	- FIXME: should be able to run the import tool instead
- create at least one git repo (table `source`)
- create *exactly one* (right now) git archive (table `archive`)
	- FIXME: support multiple archives
- create at least one institutional repositry (table `repository`)
- recomended way ATM is to create SQL files for each of them

## Set up the Apache proxy

- install Apache (`sudo apt install apache2`)
- create config for redirect, eg. `/etc/apache2/sites-available/redirect.conf`:
```apache
<VirtualHost *:80>
	ServerName saradomain # change and remove this comment from here.
	ServerAdmin webmaster@localhost # change and remove this comment from here

	Alias "/.well-known" "/var/www/letsencrypt/.well-known"
	<Directory /var/www/letsencrypt/.well-known>
		Options -MultiViews
		Require all granted
	</Directory>

	RedirectPermanent / "https://saradomain/" # change URL and remove this comment from here
</VirtualHost>
```
  - INFO: to get a "saradomain" value you can use "nslookup IP-address-of-machine" command and get the "name" parameter from output, without the last dot, e.g. "vm-XXX-XXX.bwcloud.uni-ulm.de" if your SARA-server is running on bwcloud service (https://www.bw-cloud.org/). 

- create config for proxy to Tomcat, eg. `/etc/apache2/sites-available/proxy.conf`:
```apache
<VirtualHost *:443>
	ServerName saradomain # change and remove this comment from here
	ServerAdmin webmaster@localhost # change and remove this comment from here

	<Location />
		ProxyPass "ajp://localhost:8009/SaraServer/"
		ProxyPassReverseCookiePath "/SaraServer" "/"
	</Location>
	## handle Shibboleth locally
	#<Location /Shibboleth.sso>
	#	ProxyPass !
	#	SetHandler shib
	#</Location>
	#<Location /api/auth/shibboleth>
	#	AuthType shibboleth
	#	ShibRequestSetting requireSession 1
	#	#ErrorDocument 401 "/shibboleth/get-lost.html"
	#
	#	<RequireAll>
	#		# allow members (students, faculty, staff) and employees.
	#		# alumni and affiliates probably don't hurt either.
	#		# don't allow library-walk-ins, though (they don't have the required fields anyway).
	#		Require shib-attr affiliation ~ ^(member|faculty|student|staff|employee|alum|affiliate)@
	#	</RequireAll>
	#
	#	# environment (the default) is better than headers, but cannot be proxied :(
	#	ShibUseEnvironment Off
	#	ShibUseHeaders On
	#
	#	# disable <RequestMapper>-defined auth plugins (supposedly faster)
	#	ShibRequestMapperAuthz Off
	#</Location>
	#
	## Shibboleth test endpoint
	#Alias /shibtest /var/www/html
	#<Location /shibtest>
	#		ProxyPass !
	#		AuthType shibboleth
	#		Require shibboleth
	#		ShibRequestSetting requireSession 1
	#</Location>

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

	### see below for SSL config ###
</VirtualHost>
```
- enable sites and modules: `sudo a2dissite 000-default`, `sudo a2ensite redirect proxy`, `sudo a2enmod proxy_ajp ssl headers`, then restart Apache: `systemctl restart apache2`


## Set up SSL

### using a real certificate

if you need Shibboleth, follow the [Shibboleth instructions](shib.md) instead.
the config here is a subset of the Shibboleth config.

- get a certificate (see [Getting a DFN Certificate](dfn-cert.md))
- place private key in `/etc/ssl/private/saradomain.key`, PEM format
- place certificate in `/etc/ssl/certs/saradomain.pem`, PEM format
	- for DFN CA, select certificate profile "Shibboleth IdP SP" for Shibboleth compatibility
- place certificate chain in `/etc/ssl/certs/uni-konstanz.pem`, PEM format, in reverse order of certificate chain, without root
	- separate file for Shibboleth compatibility
- go to https://mozilla.github.io/server-side-tls/ssl-config-generator/
- create `/etc/apache2/mods-available/ssl.conf` with contents from the config generator:
```apache
<IfModule mod_ssl.c>
	SSLRandomSeed startup builtin
	SSLRandomSeed startup file:/dev/urandom 512
	SSLRandomSeed connect builtin
	SSLRandomSeed connect file:/dev/urandom 512

	#   Inter-Process Session Cache:
	#   Configure the SSL Session Cache: First the mechanism 
	#   to use and second the expiring timeout (in seconds).
	#   (The mechanism dbm has known memory leaks and should not be used).
	#SSLSessionCache		 dbm:${APACHE_RUN_DIR}/ssl_scache
	SSLSessionCache		shmcb:${APACHE_RUN_DIR}/ssl_scache(512000)
	SSLSessionCacheTimeout  300

	# from: https://mozilla.github.io/server-side-tls/ssl-config-generator/
	### insert config here ###
</IfModule>
```
- in your site config, set
```apache
	SSLEngine on
	SSLCertificateFile  /etc/ssl/certs/saradomain.pem
	SSLCertificateKeyFile /etc/ssl/private/saradomain.key
	SSLCertificateChainFile /etc/ssl/certs/uni-konstanz.pem
	Header always set Strict-Transport-Security "max-age=15768000"
```
- enable all the modules: `sudo a2enmod headers ssl`
- restart apache and hope that there are no syntax errors: `sudo service apache2 restart`
	- sure you can do `sudo apache2ctl configtest` first, but where's the fun in that?
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=saradomain` (replacing `saradomain` in URL!) and verify you get an A or A+
- watch out for "certificate about to expire" email and occasionally re-check the config generator

### using Let's Encrypt

- in your SSL site config (e.g. `/etc/apache2/sites-available/proxy.conf`), make sure you have something like
```apache
	Alias "/.well-known" "/var/www/letsencrypt/.well-known"
	<Location /.well-known>
		ProxyPass !
	</Location>
	<Directory /var/www/letsencrypt/.well-known>
		Options -MultiViews
		Require all granted
	</Directory>

	SSLEngine on
	# use the snakeoil certs for setup ONLY
	SSLCertificateFile	/etc/ssl/certs/ssl-cert-snakeoil.pem
	SSLCertificateKeyFile /etc/ssl/private/ssl-cert-snakeoil.key
```
- reload or restart apache (`sudo service apache2 restart`)
- install Letsencrypt: `sudo apt install letsencrypt`
- create webroot: `sudo mkdir -p /var/www/letsencrypt`
- get SSL certificate (replace "saradomain" with real URL): `sudo letsencrypt certonly --webroot -w /var/www/letsencrypt -d saradomain`
- edit site SSL config (e.g. `/etc/apache2/sites-available/proxy.conf`). Delete the snakeoil lines and set (see the comment on line)
```apache
	SSLCertificateFile	/etc/letsencrypt/live/saradomain/fullchain.pem # replace "saradomain" with real URL and remove this comment from here
	SSLCertificateKeyFile /etc/letsencrypt/live/saradomain/privkey.pem # replace "saradomain" with real URL and remove this comment from here
```
- create `/etc/apache2/mods-available/ssl.conf` with contents from the config generator:
```apache
<IfModule mod_ssl.c>
	SSLRandomSeed startup builtin
	SSLRandomSeed startup file:/dev/urandom 512
	SSLRandomSeed connect builtin
	SSLRandomSeed connect file:/dev/urandom 512

	#   Inter-Process Session Cache:
	#   Configure the SSL Session Cache: First the mechanism 
	#   to use and second the expiring timeout (in seconds).
	#   (The mechanism dbm has known memory leaks and should not be used).
	#SSLSessionCache		 dbm:${APACHE_RUN_DIR}/ssl_scache
	SSLSessionCache		shmcb:${APACHE_RUN_DIR}/ssl_scache(512000)
	SSLSessionCacheTimeout  300

	# from: https://mozilla.github.io/server-side-tls/ssl-config-generator/
	### insert config here ###
</IfModule>
```
- reload apache (`sudo service apache2 reload`)
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=saradomain` (replacing `saradomain` in URL!) and verify you get an A or A+
- set up periodic renewal: create `/etc/cron.d/letsencrypt` containing
```cron
15 3 * * * root /usr/bin/letsencrypt renew && service apache2 reload
```
- occasionally re-check the config generator

## Build and deploy the actual webapp

- install Tomcat (`sudo apt install tomcat8`)
- enable an AJP listener on port 8009 in `/etc/tomcat8/server.xml`, or just replace the whole file with
```
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
```
- install Java and maven
```
sudo apt-mark hold openjdk-11-jre-headless
sudo apt-get -y install openjdk-8-jdk maven

```
- build the WAR:
```bash
cd ~/SARA-server
mvn clean package -DskipTests

```
- copy `target/SaraServer-*.war` to `/var/lib/tomcat8/webapps/SaraServer.war` on server
```bash
sudo -u tomcat8 cp target/SaraServer-*.war /var/lib/tomcat8/webapps/SaraServer.war

```
- copy `postgresql-42.1.4.jar` and `geronimo-javamail_1.4_spec-1.6.jar` to `/var/lib/tomcat8/lib` on server
	- they're needed while parsing `SaraServer.xml`, before the WAR is loaded, so they must be in `lib`
	- before you start searching the web, they're somewhere in `~/.m2/repository/` after building...
	- e.g.:
```bash
sudo -u tomcat8 cp ~/.m2/repository/org/postgresql/postgresql/42.1.4/postgresql-42.1.4.jar /var/lib/tomcat8/lib
sudo -u tomcat8 cp ~/.m2/repository/org/apache/geronimo/specs/geronimo-javamail_1.4_spec/1.6/geronimo-javamail_1.4_spec-1.6.jar /var/lib/tomcat8/lib
sudo -u tomcat8 cp ~/.m2/repository/org/apache/geronimo/specs/geronimo-activation_1.0.2_spec/1.1/geronimo-activation_1.0.2_spec-1.1.jar /var/lib/tomcat8/lib/
sudo -u tomcat8 cp ~/.m2/repository/org/apache/geronimo/javamail/geronimo-javamail_1.4_provider/1.6/geronimo-javamail_1.4_provider-1.6.jar /var/lib/tomcat8/lib

```
- copy `src/main/webapp/META-INF/context.xml` to `/etc/tomcat8/Catalina/localhost/SaraServer.xml` on server
```bash
sudo cp src/main/webapp/META-INF/context.xml /etc/tomcat8/Catalina/localhost/SaraServer.xml

```
- configure `/etc/tomcat8/Catalina/localhost/SaraServer.xml`
	- `password` in `jdbc.database` resource: PostgreSQL database password
	- `sara.webroot` parameter: `https://saradomain/`
- restart tomcat (`sudo service tomcat8 restart`)
- check `/var/log/tomcat8/catalina.out` for error messages
- it should now be running (and working) at `https://saradomain/`
    - INFO: SARA is not configured yet, that's why some error message could appear on the page, e.g. "Failed to load git repos!"
    - Configuration page you can find here - "TODO: put a link to the page with a configuration manual!"  

# Updating the SARA Server Webapp

## Update the Actual Webapp

- build the WAR: `mvn clean package`
- delete the unpacked webapp (otherwise it can end up taking precedence over the WAR): `sudo rm -rf /var/lib/tomcat8/webapps/SaraServer/`
- copy `target/SaraServer-*.war` to `/var/lib/tomcat8/webapps/SaraServer.war` on server
- restart tomcat (`sudo service tomcat8 restart`)
	- if this takes forever and `cat /proc/sys/kernel/random/entropy_avail` indicates low entropy, try `sudo ping -f localhost` (or even better, flood-ping it across the network for real entropy)
- check `/var/log/tomcat8/catalina.out` for error messages
- it should now be running (and working) at `https://saradomain/`

## Update the Database

- send `ALTER TABLE`s until the database schema matches `schema.sql`
- `UPDATE` table contents where necessary
- restart tomcat and check whether the webapp runs correctly
- repeat until it does
- hope that you didn't miss anything
- avoid running `DELETE FROM table` without a `WHERE` clause
