#!/bin/bash

if [ "$1" = "" ]
  then
    echo "Usage: set_version.sh <version>"
    exit 1
fi

set -ex

mvn org.codehaus.mojo:versions-maven-plugin:2.7:set -DnewVersion=$1 -DgenerateBackupPoms=false -DprocessAllModules=true
