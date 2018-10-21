package com.avioconsulting.mule.testing.muleinterfaces.containers

import groovy.transform.Immutable

@Immutable
class Dependency {
    String name, artifactId, groupId, version, filename, scope
    List<String> dependencies

    static Dependency parse(String name,
                            Map stuff) {
        new Dependency(name,
                       stuff['artifactId'],
                       stuff['groupId'],
                       stuff['version'],
                       stuff['filename'],
                       stuff['scope'],
                       stuff['dependencies'])
    }

    URL getFullFilePath(File repoDirectory) {
        // TODO: OS/join
        def groupDirs = groupId.replaceAll('\\.', '/')
        def file = new File(repoDirectory, "${groupDirs}/${artifactId}/${version}/${filename}")
        assert file.exists()
        file.toURI().toURL()
    }
}
