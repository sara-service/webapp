# Setting up a Git Archive using GitLab

## Install GitLab

follow [Setting up a Git Repository using GitLab](gitlab.md)

## Create Users and Groups

log in as admin:

- create a regular user for SARA
	- Name: "Software Archiving of Research Artefacts"
	- Username: "sara-user" (configurable)
	- Email: anything (it's irrelevant, but if deliverable will get mail)
	- Password: run `apg` to create a good one
	- Projects limit: 100000 (or more!)
	- Regular user, cannot create group, not external
- create a group for permanent archive
	- Path: "archive" (configurable)
	- Name: "Archive" (or something more useful)
	- Visibility: Public, don't allow request access
- add `sara-user` to `archive` as **Maintainer** (not Owner – this way it cannot delete projects there!)
- in GitLab settings (https://gitlabdomain/admin/application_settings):
	- in *Sign-in restrictions*, set *Home page URL* to `https://gitlabdomain/explore/projects`
	- in *Repository maintenance*, make sure housekeeping is enabled
		- FIXME we'll eventually want tho have these disabled and run them ourselves

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
- `namespace`: `archive` (or whatever you called the group)
- `token`: the token GitLab generated for `sara-user`
- `private-key`: the private SSH key from `temp` (preserve the linebreaks)
- `public-key`: the public SSH key from `temp.pub` (should be a single line)
- `known-hosts`: the contents of `known_hosts` as created above (preserve the linebreaks)
- `commiter-name`: name to use when SARA commits its metadata
- `commiter-email`: email address to use when SARA commits its metadata
