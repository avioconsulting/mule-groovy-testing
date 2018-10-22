#!/bin/bash
if [ "$VERSION" = "" ]
  then
    echo "Set the VERSION env variable to match the built version"
    exit 1
fi

set -e

rm -rf temporary_repo
ARTIFACT_DIR=com/avioconsulting/mule/testing/$VERSION
LOCAL_REPO=~/.m2/repository
mkdir -pv temporary_repo/$ARTIFACT_DIR
cp -v $LOCAL_REPO/$ARTIFACT_DIR/testing-${VERSION}* temporary_repo/$ARTIFACT_DIR

RESOLVER_ARTIFACT_DIR=com/avioconsulting/mule/depresolver/1.0.0
mkdir -pv temporary_repo/$RESOLVER_ARTIFACT_DIR
cp -v $LOCAL_REPO/$RESOLVER_ARTIFACT_DIR/depresolver-1.0.0* temporary_repo/$RESOLVER_ARTIFACT_DIR

set -x
mvn -Dmaven.repo.local=temporary_repo -Dtest.version=$VERSION clean test
