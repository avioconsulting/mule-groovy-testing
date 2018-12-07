#!/bin/sh
set -e

echo "Pushing to general AVIO Nexus"
pushd implementation
mvn clean deploy -DskipTests
popd

echo "Updating AVIO DFW bitbucket repo"
# AVIO Nexus
git checkout mule4_1/dfw_outside_network
git rebase mule4_1/master
git push --force

echo "Pushing to AVIO Nexus DFW"
pushd implementation
mvn clean deploy -DskipTests
popd

echo "Now updating DFW customer code"
git checkout mule4_1/dfw
git rebase mule4_1/master
git push --force dfw mule4_1/dfw:mule4_1/master
git push --force origin mule4_1/dfw

echo Now you can push to DFW Artifactory via Maven on your VPN VM and then switch back...
echo Use the command 'mvn clean deploy -DskipTests'
echo NOTE: DFW/VPN for some reason can block the maven deploy plugin from pushing a new version out. You may need to change the URL in implementation/pom.xml/distributionManagement to use localhost and SSH port forward to aodbprdc.dfw.dfwairport.com to complete the deploy
