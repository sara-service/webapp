# Getting a Certificate from the DFN CA

- generate entropy: `dd if=/dev/urandom of=random.bin bs=1b count=1k`
- create an openssl config file `$FQDN.conf`:
```
[req]
prompt = no
default_bits = 2048
RANDFILE = ./random.bin
encrypt_key = no
default_md = sha256
default_keyfile = $FQDN.key
distinguished_name = req_distinguished_name
req_extensions = req_extensions
string_mask = nombstr

[req_distinguished_name]
C=DE
ST=Baden-Wuerttemberg
L=Konstanz
O=Universitaet Konstanz
CN=$FQDN

[req_extensions]
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth, clientAuth # clientAuth is for Shib
subjectAltName = @alt_names

[alt_names]
DNS.1 = $FQDN
DNS.2 = www.$FQDN # useful for idiots who always add www, but needs to be in DNS too
# add more as needed
```
- generate an private key and CSR: `openssl req -new -config $FQDN.conf -out $FQDN.req`
- sanitize the entropy file: `shred -u random.bin` (it's trivial to recover the private key using that file!!)
- generate a random PIN for the certificate
```bash
dd if=/dev/urandom bs=6 count=1 status=none | hexdump | cut -d' ' -f2-4 | tr -d ' ' | head -1 | tee $FQDN.pin
```
- visit https://pki.pca.dfn.de/dfn-ca-global-g2/cgi-bin/pub/pki?cmd=pkcs10_req&id=1&menu_item=2&RA_ID=4580
- select correct certificate profile (*Shibboleth IdP SP* for Shib + Apache, or *Web Server* for Apache only)
- paste the PIN and preferably write it down somewhere
	- despite the warning, having it revoked manually doesn't need the PIN so it isn't really that important
- upload `$FQDN.req`, enter contact info, submit the request
- print and sign the form, and hand it to the local RA (registration operator)
- receive email containing the certificate. save it as `$FQDN.pem`

## Getting the Certificate Chain

see also: https://www.kim.uni-konstanz.de/services/administrieren-und-wartung/systemadministration/servercerts/wurzelzertifikate/

you can use the ready-made chain (https://pki.pca.dfn.de/dfn-ca-global-g2/pub/cacert/chain.txt)
but that will get you a "Contains anchor" warning from SSL Labs.
that's mostly harmless, but including the root is completely unnecessary:

- if the browser already has the root, you don't need to send it.
- if it doesn't have it, then it won't trust it anyway and sending it won't actually accomplish anything.

→ either remove the root from chain.txt or just concatenate the required certificates manually:
```bash
wget -O uni-konstanz.pem http://cdp.pca.dfn.de/dfn-ca-global-g2/pub/cacert/cacert.pem http://cdp.pca.dfn.de/global-root-g2-ca/pub/cacert/cacert.pem
```
