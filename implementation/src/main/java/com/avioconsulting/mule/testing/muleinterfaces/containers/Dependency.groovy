package com.avioconsulting.mule.testing.muleinterfaces.containers

import groovy.transform.Immutable

// mirrors JSON
@Immutable
class Dependency {
    String groupId, artifactId, version, filename

    URL getURL() {
        new File(filename).toURI().toURL()
    }

    static Dependency parse(Map stuff) {
        new Dependency(stuff['groupId'],
                       stuff['artifactId'],
                       stuff['version'],
                       stuff['filename'])
    }
}
