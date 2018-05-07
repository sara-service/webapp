# Deploying the SARA Server

**This is untested** and probably doesn't completely work...

# Deploying the webapp

## Set up PostgreSQL

- install PostgreSQL (`sudo apt install postgresql`)
- create user: `sudo -u postgres createuser -d -l -R -S sarauser`
	- FIXME: `-l -R -S` is the default; `-d` should be unnecessary (user doesn't need to create databases)
- set a password: `sudo -u postgres psql -d test -c "ALTER USER sarauser WITH PASSWORD 'sarapassword'"` (run `apg` to create a good one)
- create database: `sudo -u postgres createdb -E UTF8 -O sarauser saradb`
- create tables: undocumented (FIXME). best guess right now:
	- search `initdb.sh` for the line that does `cat saradb/… … ….sql >temp.sql`
	- remove the files that just create test data, then concatenate the rest *in the right order*
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
<VirtualHost *:443>
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
- enable sites: `sudo a2dissite 000-default`, `sudo a2ensite redirect`, `sudo a2ensite proxy`
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


# Setting up a GitLab

## Install the GitLab Omnibus package

follow https://about.gitlab.com/installation/#ubuntu
(or read the the script it wgets into a root shell.
 all it really does is add an apt repo and key...)

*make sure you install `gitlab-ce` (not `gitlab-ee`)!!*

omnibus will make itself at home on the system.
probably much more than you wanted it to.

## Set up SSL:

- in `/etc/gitlab/gitlab.rb`, set variables as follows (unless you need other things, that's actually the entire config!)
```ruby
external_url 'http://gitlabdomain' # note http://; no SSL yet!
nginx['custom_gitlab_server_config'] = "location ^~ /.well-known { root /var/www/letsencrypt; }"
```
- reconfigure and restart gitlab (`sudo gitlab-ctl reconfigure`)
- get SSL certificate: `sudo letsencrypt certonly --webroot -w /var/www/letsencrypt -d gitlabdomain`
- again in `/etc/gitlab/gitlab.rb`, set (again, that's usually the entire config)
```ruby
external_url 'https://gitlabdomain' # note https:// now!
nginx['redirect_http_to_https'] = true
nginx['ssl_certificate'] = "/etc/letsencrypt/live/gitlabdomain/fullchain.pem"
nginx['ssl_certificate_key'] = "/etc/letsencrypt/live/gitlabdomain/privkey.pem"
nginx['custom_gitlab_server_config'] = "location ^~ /.well-known { root /var/www/letsencrypt; }"
```
- reconfigure and restart gitlab again (`sudo gitlab-ctl reconfigure`)
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=gitlabdomain` (replacing `gitlabdomain` in URL!). if you don't get at least a "B", apply config from https://mozilla.github.io/server-side-tls/ssl-config-generator/
- set up periodic renewal: create `/etc/cron.d/letsencrypt` containing
```cron
15 3 * * * root /usr/bin/letsencrypt renew && gitlab-ctl restart nginx
```

# Setting up a Git Repository using GitLab

## Install GitLab

follow "Setting up a Git Repository using GitLab"

## Register OAuth Application

log in as admin and go to `https://gitlabdomain/admin/applications/new`.

- Name: "SARA-Server" (configurable, but will be shown to users)
- Callback url: `https://saradomain/api/auth/redirect` (for local development, add `http://localhost:8080/api/auth/redirect`)
- Trusted: NO (YES means "don't ask user for authorization on login". useful for development; probably illegal under GDPR in production)
- Scopes: `api`, `read_user` (?)

note "Application Id" and "Secret"

## Create Repo in Database

create a repo with parameters:

- Type: `GitLabRESTv4`
- `url`: `https://gitlabdomain` (no trailing slash!)
- `oauthID`: "Application Id" from GitLab
- `oauthSecret`: "Secret" from GitLab


# Setting up a Git Archive using GitLab

## Install GitLab

follow "Setting up a Git Repository using GitLab"

## Create Users and Groups

log in as admin:

- create a regular user for SARA
	- Name: "Software Archiving of Research Artefacts"
	- Username: "sara-user" (configurable)
	- Email: anything (it's irrelevant, but if deliverable will get mail)
	- Password: run `apg` to create a good one
	- Projects limit: 100000 (or more!)
	- Regular user, cannot create group, not external
- create a group for temporary archive
	- Path: "temp" (configurable)
	- Name: "Temporary Archive"
	- Description: "Stuff stored here will either be moved to the permanent archive or deleted, if the publication is accepted or rejected, respectively."
	- Visibility: Private, don't allow request access
- add `sara-user` to `temp` as **Owner**
- create a group for permanent archive
	- Path: "archive" (configurable)
	- Name: "Archive" (or something more useful)
	- Visibility: Public, don't allow request access
- add `sara-user` to `archive` as **Master** (not Owner – this way it cannot delete projects there!)

## Set Up SARA User

log in as `sara-user`:

- create an SSH key: `ssh-keygen -t ed25519 -f temp`
	- `ed25519` is good; so is `ecdsa`
	- `rsa` works but is huge
	- `dsa` and `rsa1` are *insecure*; never use these!
- on the server, run `sed 's/^/saradomain /' /etc/ssh/*.pub >known_hosts`
- in `https://gitlabdomain/profile/personal_access_tokens`, create a token with `api` rights. note "Your New Personal Access Token"
- in `https://gitlabdomain/profile/keys`, add the public key from `temp.pub`
- remember to `shred -u temp` after you've installed the key!

## Create Archive in Database

- Type: `GitLabArchiveRESTv4`
- `url`: `https://gitlabdomain` (no trailing slash!)
- `temp-namespace`: `temp` (or whatever you called the group)
- `main-namespace`: `archive` (or whatever you called the group)
- `dark-namespace`: currently unused; set to `dark-archive` for now
- `token`: the token GitLab generated for `sara-user`
- `private-key`: the private SSH key from `temp` (preserve the linebreaks)
- `public-key`: the public SSH key from `temp.pub` (should be a single line)
- `known-hosts`: the contents of `known_hosts` as created above (preserve the linebreaks)
