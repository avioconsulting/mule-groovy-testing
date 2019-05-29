#!/bin/sh
set -e

echo "Pushing to general AVIO Nexus"
pushd implementation
mvn clean deploy -DskipTests
popd
git push
