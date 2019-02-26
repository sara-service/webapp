# Setting up a GitLab

## Install the GitLab Omnibus package

follow https://about.gitlab.com/installation/#ubuntu,
*making sure you install `gitlab-ce` (not `gitlab-ee`)!!*

**or:** read the the script it wgets into a root shell.
all it really does is add an apt repo and key:

- add the GPG key: `wget -O- https://packages.gitlab.com/gitlab/gitlab-ce/gpgkey | sudo apt-key add`
- do `sudo apt-key list` and verify that this added key `E15E78F4` (last 8 digits of key hash)
- add the repo: create `/etc/apt/sources.list.d/gitlab-ce.list` containing
	```
	deb https://packages.gitlab.com/gitlab/gitlab-ce/ubuntu/ bionic main
	deb-src https://packages.gitlab.com/gitlab/gitlab-ce/ubuntu/ bionic main
	```
- `sudo apt update` and watch out for key-related errors
- install GitLab CE: `sudo EXTERNAL_URL=http://gitlab.example.com apt install gitlab-ce`

omnibus will make itself at home on the system.
probably much more than you wanted it to.

## set admin password

- visit http://gitlab.example.com
- enter the password
- hope that nobody else is faster than you

yes that really is the official method :(

## Set up SSL

### using a real certificate

- get a certificate (see [Getting a DFN Certificate](dfn-cert.md))
- place private key in `/etc/ssl/private/gitlabdomain.key`, PEM format
- place public key *with entire certificate chain* in `/etc/ssl/certs/gitlabdomain.pem`, PEM format
- in `/etc/gitlab/gitlab.rb`, set variables as follows (unless you need other things, that's actually the entire config!)
```ruby
external_url 'https://demogitlab.sara-service.org'
nginx['redirect_http_to_https'] = true
nginx['ssl_certificate'] = "/etc/ssl/certs/gitlabdomain.pem"
nginx['ssl_certificate_key'] = "/etc/ssl/private/gitlabdomain.key"
```
- highly recommended: go to https://mozilla.github.io/server-side-tls/ssl-config-generator/ and add something like this as well:
```ruby
nginx['ssl_protocols'] = "TLSv1.2";
nginx['ssl_ciphers'] = "CIPHER-LIST:GOES-HERE"
nginx['ssl_prefer_server_ciphers'] = "on"
nginx['hsts_max_age'] = 15768000
```
- reconfigure and restart gitlab (`sudo gitlab-ctl reconfigure && sudo gitlab-ctl restart`)
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=gitlabdomain` (replacing `gitlabdomain` in URL!) and verify you get an A or A+
- watch out for "certificate about to expire" email and occasionally re-check the config generator

### using Let's Encrypt

- in `/etc/gitlab/gitlab.rb`, set variables as follows (unless you need other things, that's actually the entire config!)
```ruby
external_url 'http://gitlabdomain' # note http://; no SSL yet!
nginx['custom_gitlab_server_config'] = "location ^~ /.well-known { root /var/www/letsencrypt; }"
```
- reconfigure and restart gitlab (`sudo gitlab-ctl reconfigure && sudo gitlab-ctl restart`)
- get SSL certificate: `sudo letsencrypt certonly --webroot -w /var/www/letsencrypt -d gitlabdomain`
- again in `/etc/gitlab/gitlab.rb`, set (again, that's usually the entire config)
```ruby
external_url 'https://gitlabdomain' # note https:// now!
nginx['redirect_http_to_https'] = true
nginx['ssl_certificate'] = "/etc/letsencrypt/live/gitlabdomain/fullchain.pem"
nginx['ssl_certificate_key'] = "/etc/letsencrypt/live/gitlabdomain/privkey.pem"
nginx['custom_gitlab_server_config'] = "location ^~ /.well-known { root /var/www/letsencrypt; }"
```
- highly recommended: go to https://mozilla.github.io/server-side-tls/ssl-config-generator/ and add something like this as well:
```ruby
nginx['ssl_protocols'] = "TLSv1.2";
nginx['ssl_ciphers'] = "CIPHER-LIST:GOES-HERE"
nginx['ssl_prefer_server_ciphers'] = "on"
nginx['hsts_max_age'] = 15768000
```
- reconfigure and restart gitlab again (`sudo gitlab-ctl reconfigure && sudo gitlab-ctl restart`)
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=gitlabdomain` (replacing `gitlabdomain` in URL!) and verify you get an A or A+
- set up periodic renewal: create `/etc/cron.d/letsencrypt` containing
```cron
15 3 * * * root /usr/bin/letsencrypt renew && gitlab-ctl restart nginx
```
- occasionally re-check the config generator
