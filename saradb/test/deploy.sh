#!/bin/sh -ex
# this is the test instance deploy script, which lives in ~/deploy.sh on
# test.sara-service.org. for security reasons, however, it has to be updated
# manually. that is, if you edit it, don't forget to update it on the server
# as well!

rm -rf ~/SARA-server
git clone git@git.uni-konstanz.de:sara/SARA-server.git ~/SARA-server
cd ~/SARA-server
git checkout -f test
git submodule init
git submodule update

sudo service tomcat8 stop
sudo pg_dropcluster --stop 10 main || true
sudo pg_createcluster --start 10 main

sudo -u postgres -H saradb/testdb.sh
mvn clean install -DskipTests
sudo rm -rf /var/lib/tomcat8/work/Catalina/localhost/SaraServer/
sudo mv target/SaraServer-*.war /var/lib/tomcat8/webapps/SaraServer.war
sudo service tomcat8 restart
