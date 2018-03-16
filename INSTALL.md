# Deploying the SARA Server

# Deploying the webapp

**This is untested** and probably doesn't completely work...

## Set up PostgreSQL

- install PostgreSQL (`sudo apt install postgresql`)
- create user: `sudo -u postgres createuser -d -l -R -S sarauser`
	- FIXME: `-l -R -S` is the default; `-d` should be unnecessary (user doesn't need to create databases)
- set a password: `sudo -u postgres psql -d test -c "ALTER USER sarauser WITH PASSWORD 'sarapassword'"`
- create database: `sudo -u postgres createdb -E UTF8 -O seruser saradb`
- create tables: undocumented. best guess right now:
	- search `initdb.sh` for the line that does `cat saradb/… … ….sql >temp.sql`
	- remove the files that just create test data, then concatenate the rest
	- run `sudo -u postgres psql -d saradb -f temp.sql`

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
<VirtualHost _default_:443>
	ServerName saradomain # change
	ServerAdmin webmaster@localhost # change

	<Location />
		ProxyPass "ajp://localhost:8009/SaraServer/"
		ProxyPassReverseCookiePath "/SaraServer/" "/"
	</Location>
	# handle Shibboleth locally
	# FIXME we'll probably not need this
	<Location /Shibboleth.sso>
		ProxyPass !
		SetHandler shib
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

- install Letsencrypt (`sudo apt install letsencrypt`)
- get SSL certificate: `sudo letsencrypt certonly --webroot -w /var/www/letsencrypt -d saradomain`
- edit `/etc/apache2/sites-available/proxy.conf`. delete the snakeoil lines and set
```apache
	SSLCertificateFile	/etc/letsencrypt/live/saradomain/fullchain.pem
	SSLCertificateKeyFile /etc/letsencrypt/live/saradomain/privkey.pem
```
- reload apache (`sudo service apache2 reload`)
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=saradomain` (replacing `saradomain` in URL!). if you don't get at least a "B", apply config from https://mozilla.github.io/server-side-tls/ssl-config-generator/
- set up periodic renewal: create `/etc/cron.d/letsencrypt` containing
```cron
15 3 * * * root /usr/bin/letsencrypt renew && service apache2 reload
```

## Build and deploy the actual webapp

- install Tomcat (`sudo apt install tomcat7`)
- build the WAR: `mvn clean install`
- copy `target/SaraServer-1.0-SNAPSHOT.war` to `/var/lib/tomcat7/webapps/SaraServer.war` on server
- copy `src/main/webapp/META-INF/context.xml` to `/etc/tomcat7/Catalina/localhost/SaraServer.xml` on server
- configure `SaraServer.xml` (database and `sara.webroot`)
- restart tomcat (`sudo service tomcat7 restart`) and check `/var/log/tomcat7/catalina.out` for error messages
- it should now be running (and working) at `https://saradomain/`

## Add a few IRs

this is trivial

