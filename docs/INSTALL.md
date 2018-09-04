# Deploying the SARA Server

**This is untested** and probably doesn't completely work...

# Deploying the webapp

## Set up PostgreSQL

- install PostgreSQL (`sudo apt install postgresql`)
- create user: `sudo -u postgres createuser -l -D -R -S sarauser`
- set a password: `sudo -u postgres psql -d test -c "ALTER USER sarauser WITH PASSWORD 'sarapassword'"` (run `apg` to create a good one)
- create database: `sudo -u postgres createdb -E UTF8 -O sarauser saradb`
- enable `pgcrypto` extension: `sudo -u postgres psql -d satadb -f saradb/adminconfig.sql`
- create tables: `sudo -u postgres psql -d satadb -f saradb/schema.sql`
- edit `saradb/permissions.sql`, replacing `test` with `sarauser`
- grant permissions: `sudo -u postgres psql -d satadb -f saradb/permissions.sql`
- create at least one git repo (table `source`)
- create *exactly one* (right now) git archive (table `archive`)
	- FIXME: change once multiple archives supported
- create at least one institutional repositry (table `repository`)
- import licenses: `sudo -u postgres psql -d satadb -f saradb/licenses.sql`
	- FIXME: should be able to run the import tool instead

## Set up the Apache proxy

- install Apache (`sudo apt install apache2`)
- create config for redirect, eg. `/etc/apache2/sites-available/redirect.conf`:
```apache
<VirtualHost *:80>
	ServerName saradomain # change
	ServerAdmin webmaster@localhost # change

	Alias "/.well-known" "/var/www/letsencrypt/.well-known"
	<Directory /var/www/letsencrypt/.well-known>
		Options -MultiViews
		Require all granted
	</Directory>

	RedirectPermanent / "https://saradomain/" # change URL
</VirtualHost>
```
- create config for proxy to Tomcat, eg. `/etc/apache2/sites-available/proxy.conf`:
```apache
<VirtualHost *:443>
	ServerName saradomain # change
	ServerAdmin webmaster@localhost # change

	<Location />
		ProxyPass "ajp://localhost:8009/SaraServer/"
		ProxyPassReverseCookiePath "/SaraServer" "/"
	</Location>
	# handle Shibboleth locally
	<Location /Shibboleth.sso>
		ProxyPass !
		SetHandler shib
	</Location>
	<Location /api/auth/shibboleth>
		AuthType shibboleth
		ShibRequestSetting requireSession 1
		#ErrorDocument 401 "/shibboleth/get-lost.html"

		<RequireAll>
			# allow members (students, faculty, staff) and employees.
			# alumni and affiliates probably don't hurt either.
			# don't allow library-walk-ins, though (they don't have the required fields anyway).
			Require shib-attr affiliation ~ ^(member|faculty|student|staff|employee|alum|affiliate)@
		</RequireAll>

		# environment (the default) is better than headers, but cannot be proxied :(
		ShibUseEnvironment Off
		ShibUseHeaders On

		# forward the REMOTE_USER variable (subject or persistent-id from shib)
		#RequestHeader set eppn "expr=%{REMOTE_USER}"

		# disable <RequestMapper>-defined auth plugins (supposedly faster)
		ShibRequestMapperAuthz Off
	</Location>

	# Shibboleth test endpoint
	Alias /shibtest /var/www/html
	<Location /shibtest>
			ProxyPass !
			AuthType shibboleth
			Require shibboleth
			ShibRequestSetting requireSession 1
	</Location>

	Alias "/.well-known" "/var/www/letsencrypt/.well-known"
	<Location /.well-known>
		ProxyPass !
	</Location>
	<Directory /var/www/letsencrypt/.well-known>
		Options -MultiViews
		Require all granted
	</Directory>

	ErrorLog ${APACHE_LOG_DIR}/error.log
	CustomLog ${APACHE_LOG_DIR}/access.log combined

	SSLEngine on
	# use snakeoil certs for setup ONLY
	SSLCertificateFile	/etc/ssl/certs/ssl-cert-snakeoil.pem
	SSLCertificateKeyFile /etc/ssl/private/ssl-cert-snakeoil.key
</VirtualHost>
```
- enable sites and modules: `sudo a2dissite 000-default`, `sudo a2ensite redirect proxy`, `sudo a2enmod proxy_ajp ssl`, then restart Apache
- install Letsencrypt (`sudo apt install letsencrypt`), create webroot (`sudo mkdir -p /var/www/letsencrypt`)
- get SSL certificate: `sudo letsencrypt certonly --webroot -w /var/www/letsencrypt -d saradomain`
- edit `/etc/apache2/sites-available/proxy.conf`. delete the snakeoil lines and set
```apache
	SSLCertificateFile	/etc/letsencrypt/live/saradomain/fullchain.pem
	SSLCertificateKeyFile /etc/letsencrypt/live/saradomain/privkey.pem
```
- reload apache (`sudo service apache2 reload`)
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=saradomain` (replacing `saradomain` in URL!). if you don't get at least a "B", apply config from https://mozilla.github.io/server-side-tls/ssl-config-generator/ . if you do get a "B", apply that config anyway.
- set up periodic renewal: create `/etc/cron.d/letsencrypt` containing
```cron
15 3 * * * root /usr/bin/letsencrypt renew && service apache2 reload
```

## Build and deploy the actual webapp

- install Tomcat (`sudo apt install tomcat8`)
- enable an AJP listener on port 8009 in `/etc/tomcat8/server.xml`, or just replace the whole file with

```xml
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
      <!-- don't unpack or auto-deplay. unpacking tends to override newer WARs; auto-deploy
           is rumored to be slow. -->
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

- ⏵build the WAR: `mvn clean install`
- ⏵copy `target/SaraServer-*.war` to `/var/lib/tomcat8/webapps/SaraServer.war` on server
- copy `src/main/webapp/META-INF/context.xml` to `/etc/tomcat8/Catalina/localhost/SaraServer.xml` on server
- configure `SaraServer.xml` (database and `sara.webroot`)
- ⏵restart tomcat (`sudo service tomcat8 restart`) and check `/var/log/tomcat8/catalina.out` for error messages
- ⏵it should now be running (and working) at `https://saradomain/`

to redeploy, repeat only the steps marked ⏵

## Add a few IRs

this is trivial
