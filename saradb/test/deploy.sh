#!/bin/sh -ex
# this is the test instance deploy script, which lives in ~/deploy.sh on
# test.sara-service.org. for security reasons, however, it has to be updated
# manually. that is, if you edit it, don't forget to update it on the server
# as well!

case "$SSH_ORIGINAL_COMMAND" in
tomcat)
	sudo rm -rf /var/lib/tomcat8/webapps/SaraServer/ </dev/null
	sudo dd of=/var/lib/tomcat8/webapps/SaraServer.war
	sudo service tomcat8 restart </dev/null
	;;
postgres)
	sudo pg_dropcluster -p 5432 --stop 10 main </dev/null || true
	sudo pg_createcluster --start 10 main </dev/null
	sudo -u postgres -H createuser -l -D -R -S sara </dev/null
	sudo -u postgres -H createdb -E UTF8 -O sara sara </dev/null
	sudo -u postgres -H psql -d sara -c "ALTER USER sara WITH PASSWORD '6shnatBab_'" </dev/null
	sudo -u postgres -H psql -d sara
	;;
*)
	echo "error: »$SSH_ORIGINAL_COMMAND« not recognized"
	exit 1
	;;
esac
