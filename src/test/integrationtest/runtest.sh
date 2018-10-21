#!/bin/bash
if [ "$VERSION" = "" ]
  then
    echo "Set the VERSION env variable to match the built version"
    exit 1
fi

set -ex

rm -rf temporary_repo
ARTIFACT_DIR=temporary_repo/com/avioconsulting/mule/testing/$VERSION
mkdir -p $ARTIFACT_DIR
cp -v ../../../target/testing-$VERSION.jar $ARTIFACT_DIR
cp -v ../../../pom.xml $ARTIFACT_DIR/testing-$VERSION.pom
MULE_VERSION=${VERSION:0:5}
mvn -Dmaven.repo.local=temporary_repo -Dapp.runtime=$MULE_VERSION -Dtest.version=$VERSION clean test
