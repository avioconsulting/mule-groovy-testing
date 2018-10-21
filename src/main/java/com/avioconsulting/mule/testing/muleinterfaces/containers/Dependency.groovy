package com.avioconsulting.mule.testing.muleinterfaces.containers

import groovy.transform.Immutable

// mirrors JSON
@Immutable
class Dependency {
    String name, groupId, artifactId, version, filenameRelativeToRepo, scope

    URL getFullFilePath(File repoDirectory) {
        new File(repoDirectory, filenameRelativeToRepo).toURI().toURL()
    }

    static Dependency parse(Map stuff) {
        new Dependency(stuff['name'],
                       stuff['groupId'],
                       stuff['artifactId'],
                       stuff['version'],
                       stuff['filenameRelativeToRepo'],
                       stuff['scope'])
    }
}
