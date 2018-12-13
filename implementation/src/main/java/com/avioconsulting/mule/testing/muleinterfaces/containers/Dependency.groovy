package com.avioconsulting.mule.testing.muleinterfaces.containers

import groovy.transform.Immutable

// mirrors JSON
@Immutable
class Dependency {
    String name, groupId, artifactId, version, filename

    URL getURL() {
        new File(filename).toURI().toURL()
    }

    static Dependency parse(Map stuff) {
        new Dependency(stuff['name'],
                       stuff['groupId'],
                       stuff['artifactId'],
                       stuff['version'],
                       stuff['filename'])
    }
}
