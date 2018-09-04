# Setting up a Shibboleth SP

## Install shibd

(shortened from https://www.switch.ch/aai/guides/sp/installation/?os=ubuntu)

- create `/etc/apt/sources.list.d/SWITCHaai-swdistrib.list` with contents (replace `bionic` if not using Ubuntu 18.04)
```
deb http://pkg.switch.ch/switchaai/ubuntu bionic main
```
- add the PGP key from http://pkg.switch.ch/switchaai/SWITCHaai-swdistrib.asc
```bash
curl http://pkg.switch.ch/switchaai/SWITCHaai-swdistrib.asc | sudo apt-key add
```
- check `apt-key list` to verify it's the right key (`294E 37D1 5415 6E00 FB96  D7AA 26C3 C469 15B7 6742` right now)
- install `libapache2-mod-shib2`: `sudo apt update && sudo apt install libapache2-mod-shib2`

## Configure Apache with a Real Certificate

Let's Encrypt certificates work, but you'll have to manually replace them in the federation metadata every 3 months.
this will almost certainly annoy your metadata admin so don't do that.

- place private key in `/etc/ssl/private/saradomain.key`, PEM format
- place certificate in `/etc/ssl/certs/saradomain.pem`, PEM format
	- for DFN CA, select certificate profile "Shibboleth IdP SP"
- place certificate chain in `/etc/ssl/certs/uni-konstanz.pem`, PEM format, in reverse order of certificate chain, without root
	- certificate chain *must not* be in the same file as the server certificate (shibd doesn't like it)
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

		# disable <RequestMapper>-defined auth plugins (supposedly faster)
		ShibRequestMapperAuthz Off
	</Location>
```
- enable all the modules: `sudo a2enmod headers ssl auth_shib`
- restart apache and hope that there are no syntax errors: `sudo service apache2 restart`
	- sure you can do `sudo apache2ctl configtest` first, but where's the fun in that?
- visit `https://www.ssllabs.com/ssltest/analyze.html?d=saradomain` (replacing `saradomain` in URL!) and verify you get an A or A+

## Configure shibd

- place federation signing certificate (here: DFN AAI cert) in `/etc/ssl/certs/dfn-aai.pem`:
```bash
sudo wget -O /etc/ssl/certs/dfn-aai.pem https://www.aai.dfn.de/fileadmin/metadata/dfn-aai.g2.pem
```
- create `/etc/shibboleth/shibboleth2.xml`:
```xml
<SPConfig xmlns="urn:mace:shibboleth:2.0:native:sp:config"
		xmlns:conf="urn:mace:shibboleth:2.0:native:sp:config"
		xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
		xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"	
		xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata"
		clockSkew="180">
	<ApplicationDefaults entityID="https://saradomain/">
		<!-- deliberately disable all IP-based checks. with SSL, stealing cookies or assertions is
		     next to impossible, but IPs changing in wifi or due to multihoming are common. -->
		<Sessions lifetime="28800" timeout="3600" relayState="ss:mem"
				checkAddress="false" consistentAddress="false"
				handlerSSL="true" cookieProps="https">
			<SSO discoveryProtocol="SAMLDS" discoveryURL="https://wayf.aai.dfn.de/DFN-AAI-Test/wayf">
				SAML2
			</SSO>

			<Logout>SAML2 Local</Logout>

			<Handler type="MetadataGenerator" Location="/Metadata" signing="false"/>
			<Handler type="Session" Location="/Session" showAttributeValues="false"/>
			<Handler type="DiscoveryFeed" Location="/DiscoFeed"/>
		</Sessions>

		<!-- helpLocation is where Shib will redirect in "hopeless" situations -->
		<!-- shib logo taken from discovery right at the moment -->
		<Errors supportContact="sara@example.org" helpLocation="/" />

		<MetadataProvider type="Chaining">
			<!-- DFN-AAI-Test metadata, as per https://wiki.aai.dfn.de/de:shibsp -->
			<MetadataProvider type="XML" backingFilePath="metadata.xml"
					uri="https://www.aai.dfn.de/fileadmin/metadata/dfn-aai-test-metadata.xml"
					reloadInterval="3600" legacyOrgNames="true">
				<MetadataFilter type="RequireValidUntil" maxValidityInterval="2419200"/>
				<MetadataFilter type="Signature" certificate="/etc/ssl/certs/dfn-aai.pem"/>
			</MetadataProvider>
		</MetadataProvider>

		<!-- this maps SAML attributes to mod_shib attribute names, and must thus match the attribute
		     names in gitlab.rb. -->
		<AttributeExtractor type="XML" validate="true" reloadChanges="false" path="attribute-map.xml"/>

		<!-- AttributeQuery is required for DFN AAI -->
		<AttributeResolver type="Query" subjectMatch="true"/>

		<!-- this is just the default one that comes with the Ubuntu package. -->
		<AttributeFilter type="XML" validate="true" path="attribute-policy.xml"/>

		<!-- certificates (plural!). to seamlessly migrate to a new certificate, just create two
		     CredentialResolver entries here: one for the new certificate, and one for the old one.
		     make sure it can reads both (grep SecurityHelper /var/log/shibboleth/shibd.log)!
		     change certificates in metadata, then, after a few metadata reload cycles, remove
		     the old certificate here. as long as Shibboleth has the matching private key here,
		     it doesn't care which one is used for encrypting assertions. -->
		<CredentialResolver type="File"
				key="/etc/ssl/private/saradomain.key"
				certificate="/etc/ssl/certs/saradomain.pem"/>
	</ApplicationDefaults>

	<!-- these are just the default ones that come with the Ubuntu package. -->
	<SecurityPolicyProvider type="XML" validate="true" path="security-policy.xml"/>
	<ProtocolProvider type="XML" validate="true" reloadChanges="false" path="protocols.xml"/>
</SPConfig>
```
- replace the following placeholders with real values
	- `entityID` in `<ApplicationDefaults>`: home URL including `https://` (usually)
		- **entityID is basically unchangeable once set!**
	- `discoveryURL` in `<SSO discoveryProtocol="SAMLDS">`: the federation discovery / WAYF service
		- `https://wayf.aai.dfn.de/DFN-AAI-Test/wayf` for DFN AAI Test
		- `https://wayf.aai.dfn.de/DFN-AAI/wayf` for DFN AAI Advanced (ie. production)
	- `supportContact` in `<Errors>` (very rarely shown but should be correct)
	- `uri` in `<MetadataProvider>`: the federation metadata file
		- `https://www.aai.dfn.de/fileadmin/metadata/dfn-aai-test-metadata.xml` for DFN AAI Test
		- `https://www.aai.dfn.de/fileadmin/metadata/dfn-aai-metadata.xml` for DFN AAI Advanced (ie. production)
	- `key` and `certificate` in `<CredentialResolver>`
	- you can place additional filters in the `MetadataProvider type="Chaining">`
- create `/etc/shibboleth/attribute-map.xml`:
```xml
<Attributes xmlns="urn:mace:shibboleth:2.0:attribute-map"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<!-- EPPN -->
	<Attribute name="urn:oid:1.3.6.1.4.1.5923.1.1.1.6" id="eppn">
		<AttributeDecoder xsi:type="ScopedAttributeDecoder"/>
	</Attribute>

	<!-- scoped affiliation, used for denying library-walk-in. -->
	<Attribute name="urn:oid:1.3.6.1.4.1.5923.1.1.1.9" id="affiliation">
		<AttributeDecoder xsi:type="ScopedAttributeDecoder" caseSensitive="false"/>
	</Attribute>

	<!-- firstname + lastname, for display purposes -->
	<Attribute name="urn:oid:2.5.4.42" id="givenName"/>
	<Attribute name="urn:oid:2.5.4.4" id="sn"/>
	<!-- display name, theoretically an alternative to firstname + lastname,
	     but nobody seems to use it. -->
	<Attribute name="urn:oid:2.16.840.1.113730.3.1.241" id="displayName"/>

	<!-- email address(es) -->
	<Attribute name="urn:oid:0.9.2342.19200300.100.1.3" id="mail"/>
</Attributes>
```
- allow shibd to access the certificate: `sudo adduser _shibd ssl-cert`
- restart shibd: `sudo service shibd restart`
- check `/var/log/shibboleth/shibd.log` for errors
- visit `https://saradomain/Shibboleth.sso/Metadata` and check that
	- there's a certificate in there (else shibd cannot read the private key)
	- the `entityID` is right (you cannot easily change that one later on)
	- the hostname is right (major PITA to change all the endpoints)
- visit `https://saradomain/Shibboleth.sso/DiscoFeed` and check that
	- you get a long list of JSON-encoded stuff
	- the list contains all IdPs you want to allow for login
	- the list *doesn't* contains any IdPs you *don't* want to allow
	- FIXME maybe this only works after the SP can find itself in the federation metadata?

## Set up a Test Endpoint (optional)

- get rid of the default homepage: `sudo rm /var/www/html/index.html`
- replace with something more useful: `sudo vi /var/www/html/index.php`
```php
<!DOCTYPE html>
<html>
<head>
	<title>Shibboleth Attributes</title>
	<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
	<META HTTP-EQUIV="Expires" CONTENT="-1">
	<style>
	input, textarea {
		display: block;
	}
	input {
		width: 90%;
	}
	textarea {
		height: 30em;
		width: 100%;
	}
	td {
		vertical-align: top;
	}
	</style>
</head>
<body>
	<h1>Shibboleth Attributes</h1>

<h2>Attributes</h2>
<table>
<?php
$vars = array("persistent-id", "eppn", "mail", "givenName", "sn",
	"affiliation", "unscoped-affiliation", "entitlement", "org",
	"org-unit", "member", "member-of");
$plain = array("&", "<", ">", "\"", ";");
$html = array("&amp;", "&lt;", "&gt;", "&quot;", ";<br />");
foreach ($vars as $var) {
	echo "<tr><td>$var</td><td>".str_replace($plain, $html, $_SERVER[$var])."</td></tr>";
}
$vars = array("Shib-Session-ID", "Shib-Identity-Provider", "Shib-Authentication-Instant", "Shib-Application-ID");
foreach ($vars as $var) {
	echo "<tr><td>$var</td><td>".htmlspecialchars($_SERVER[$var])."</td></tr>";
}
?>
</table>

<h2>Assertions</h2>
<?php
$n = intval($_SERVER["Shib-Assertion-Count"]);
for ($i = 1; $i <= $n; $i++) {
	$name = sprintf("Shib-Assertion-%02d", $i);
	$url = $_SERVER[$name];
	$ass = file_get_contents($url);
	echo "<h3>$name</h3><input type='text' readonly='readonly' value='$url' /><textarea readonly='readonly'>".htmlspecialchars($ass)."</textarea>";
}
?>

<h2>Variables</h2>
<?php
foreach (array('$_SERVER' => $_SERVER, '$_REQUEST' => $_REQUEST, '$_SESSION' => $_SESSION) as $name => $array) {
	echo "<h3>$name</h3><table>";
	foreach ($array as $key => $value) {
		echo "<tr><td>".htmlspecialchars($key)."</td><td>".htmlspecialchars($value)."</td></tr>";
	}
	echo "</table>";
}
?>
</body>
</html>
```
- install php: `sudo apt install libapache2-mod-php`, `sudo a2enmod php`
- in your site config file:
```apache
	Alias /shibtest /var/www/html
	<Location /shibtest>
		ProxyPass !
		AuthType shibboleth
		Require shibboleth
		ShibRequestSetting requireSession 1
	</Location>
```
- restart Apache (`sudo service apache2 restart`)
- visit `https://saradomain/shibtest` and make sure you get some error message from Shibboleth (if SP not yet registered)

## Register your SP

- make sure SP contract (with DFN) is signed
- ask your IT department who is authorized to edit federation metadata for your SPs
- provide that person with `https://saradomain/Shibboleth.sso/Metadata` and ask them to enter your SP
- request attributes `sn`, `givenName`, `mail`, `eduPersonPrincipalName` (all required) and `cn` (display name, recommended)
	- if necessary, quote the `urn:oid:*` names from ´attribute-map.xml`
- wait 1-2 hours (or overnight) until SP shows up in federation metadata
- follow any orders and/or liberally apply bribes
- rinse and repeat for production – you cannot join production directly, you have to be in the test federation first

Konstanz: fill https://www.kim.uni-konstanz.de/services/administrieren-und-wartung/shibboleth-service-provider/shibboleth-sp-beantragen/
(in the end it's still a manual process though)

## Test your SP

- visit `https://saradomain/shibtest`
- pick correct IdP
	- if not in list, double-check metadata provider and filters, and/or restart shibd
- login
- check that attribute list is complete
	- if attributes missing: check federation metadata
		- search for your entityID
		- check all are listed as `<RequestedAttribute>` within `<AttributeConsumingService>`
	- if requested attributes still missing, contact the IdP's technical contact
- check that all attributes are listed in the PHP page
	- if attributes missing, check `attribute-map.xml`
	- if `attribute-map.xml` looks ok, check whether the attributes are in the assertion (your config is at fault) or not (the IdP or SP is at fault)
- expect users to complain that it isn't working for them. resolve problems with their IdP's technical contact.
