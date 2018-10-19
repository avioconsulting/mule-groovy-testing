package com.avioconsulting.mule.testing.muleinterfaces.containers

import groovy.util.logging.Log4j2

@Log4j2
class FilterOutNonTestingExtensionsClassLoader extends
        ClassLoader {
    private final List<String> filters

    // the parent classloader will take care of most of the work. We just need to intercept and
    // tweak a little bit (see getResources below)
    FilterOutNonTestingExtensionsClassLoader(ClassLoader parent,
                                             List<String> filters) {
        super(parent)
        this.filters = filters
    }

    @Override
    Enumeration<URL> getResources(String name) throws IOException {
        // basically we are intercepting the call from DefaultMuleCoreExtensionManagerServer's
        // this.coreExtensionDiscoverer.discover() call here and avoiding loading
        // extensions we don't need in a unit test environment
        def raw = super.getResources(name)
        if (name == 'META-INF/org/mule/runtime/core/config/core-extensions.properties') {
            def all = raw.toList()
            def stuffToRemove = all.findAll() { resource ->
                filters.any { filter -> resource.toString().contains(filter) }
            }
            def prettyDisplay = stuffToRemove.collect { url ->
                def filePortion = url.file
                // these are resources being used to locate extensions in JARs, make our log message
                // more concise by just showing the JAR filename
                def fullPath = filePortion[0..filePortion.indexOf('!/META-INF')-1]
                new File(fullPath).name
            }
            log.info 'Filtering out the following Mule extensions because we should not need them for unit tests {}',
                     prettyDisplay
            return Collections.enumeration(all - stuffToRemove)
        }
        return raw
    }
}
