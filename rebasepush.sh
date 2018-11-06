#!/bin/sh
set -e

echo "Pushing to general AVIO Nexus"
mvn clean deploy -DskipTests

echo "Updating AVIO DFW bitbucket repo"
# AVIO Nexus
git checkout mule4_1/dfw_outside_network
git rebase mule4_1/master
git push --force

echo "Pushing to AVIO Nexus DFW"
mvn clean deploy -DskipTests

echo "Now updating DFW customer code"
git checkout mule4_1/dfw
git rebase mule4_1/master
git push --force dfw mule4_1/dfw:mule4_1/master
git push --force origin mule4_1/dfw

echo Now you can push to DFW Artifactory via Maven on your VPN VM and then switch back...
