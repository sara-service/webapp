# Setting up GitLab for use with SARA (also works for GitLab.com)

## Install GitLab (if necessary)

follow [Setting up a Git Repository using GitLab](gitlab.md)

## Register OAuth Application

log in as admin and go to `https://gitlabdomain/admin/applications/new`.

if you don't have admin access (eg. on [GitLab.com](https://gitlab.com)), go to https://gitlabdomain/profile/applications instead.
the rest of the procedure is identical, but *the app will be deleted if your user gets deleted.* ie. creating the app as an admin is much preferrable whenever possible.

- Name: "SARA-Server" (configurable, but will be shown to users)
- Callback url: `https://saradomain/api/auth/redirect` (for local development, add `http://localhost:8080/api/auth/redirect`)
- Trusted: NO (YES means "don't ask user for authorization on login". useful for development; probably illegal under GDPR in production)
- Scopes: `api`, `read_user`, `read_repository`
	- note: SARA does need `api`! `read_repository` only grants `git pull` access, but SARA also needs to list projects and project contents using the API.

note "Application Id" and "Secret". they're inaccessible afterwards!

## Create Repo in Database

create a repo with parameters:

- Type: `GitLabRESTv4`
- `url`: `https://gitlabdomain` (no trailing slash!)
- `oauthID`: "Application Id" from GitLab
- `oauthSecret`: "Secret" from GitLab
- `nameRegex`: `western` (or `eastern` if a majority of your users has surname first)


# Setting up a GitHub Application for SARA

## Register OAuth Application

go to https://github.com/settings/developers and create a new OAuth app.

- Application name: "SARA-Server" (configurable, but will be shown to users)
- Homepage URL: `https://saradomain/`
- Application description: Some blah blah about the service, or just "Software Archiving of Research Artefacts"
- Authorization callback URL: `https://saradomain/api/auth/shibboleth` (or `https://saradomain/api/auth/redirect` when not using Shibboleth – not recommended)

note "Client ID" and "Client Secret"

## Create Repo in Database

create a repo with parameters:

- Type: `GitHubRESTv3` (`GitHubRESTv3WithoutShib` when not using Shibboleth – not recommended)
- `url`: `https://gitlabdomain` (no trailing slash!)
- `oauthID`: "Client ID" from GitHub
- `oauthSecret`: "Client Secret" from GitHub
- `shibSurname`: `sn`
- `shibGivenName`: `givenName`
- `shibEmail`: `mail`
- `shibID`: `eppn`
- `shibDisplayName`: `displayName` (optional but recommended)
- `nameRegex`:
	- with Shib: recommended `western`. required iff `shibDisplayName` is set. doesn't really matter much because Shib usually provides separate surname and given name attributes anyway.
	- without Shib: `western` will probably fail least spectacularly, but GitHub often doesn't even have real names for users.

the `shib*` parameters obviously have to match your `attribute-map.xml`, see [Shibboleth instructions](shib.md).
