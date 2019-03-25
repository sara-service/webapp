CREATE TEMP TABLE args(basedir text);
INSERT INTO args (basedir) VALUES (:'basedir');

DO $$

DECLARE base_dir text := (SELECT basedir from args LIMIT 1);

DECLARE logo oid := lo_import(base_dir || '/tldr.svg');

DECLARE hostname text     := 'https://tldr.sara-service.org';
DECLARE fullname text     := 'Totally Legitimate Data Repository (TL;DR)';

DECLARE demo_dspace_help text := 'Your publication has been submitted to the ' || fullname || '. It will be automatically accepted and should show up on ' || hostname || ' in a few seconds.' || chr(10) || chr(10) || 'Would you consider using this system, with a real archive and a real institutional repository, for real research software? Please get in touch at git+tldr@uni-konstanz.de and let us know what you think!';

DECLARE userhint text := 'You need to be registered to submit to TL;DR or use one of the following demo accounts: wsdemo1@sara-service.org wsdemo2@sara-service.org wsdemo3@sara-service . Your submission will be approved automatically.';

DECLARE rRef UUID;

BEGIN

-- Stefan's OPARU Stand-In in bwCloud 
INSERT INTO repository(display_name, adapter, url, contact_email, help, enabled, logo_url, user_hint) VALUES
	(fullname, 'DSpace_v6', hostname || '/xmlui', 'git+tldr@uni-konstanz.de', demo_dspace_help, TRUE,
		'data:image/svg+xml;base64,' || encode(lo_get(logo), 'base64'), userhint)
	RETURNING uuid INTO rRef;
INSERT INTO repository_params(id, param, value) VALUES
	(rRef, 'rest_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'rest_pwd', '__TLDR_PASSWORD__'),
	(rRef, 'rest_api_endpoint', hostname || '/rest'),
	(rRef, 'sword_user', 'project-sara@uni-konstanz.de'),
	(rRef, 'sword_pwd', '__TLDR_PASSWORD__'),
	(rRef, 'sword_api_endpoint', hostname || '/swordv2'),
	(rRef, 'deposit_type', 'workflow'),
        (rRef, 'check_license', 'true'),
	(rRef, 'publication_type', 'Software');

-- erase the temporary large objects
PERFORM lo_unlink(logo);

END $$;
