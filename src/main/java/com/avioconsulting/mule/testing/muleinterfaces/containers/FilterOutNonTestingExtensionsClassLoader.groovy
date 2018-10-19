package com.avioconsulting.mule.testing.muleinterfaces.containers

class FilterOutNonTestingExtensionsClassLoader extends
        ClassLoader {
    private static final List<String> filters = [
            'mule-module-cluster-ee', // no Hazelcast
            'mule-module-api-deployment', // don't seem to need this either
            'mule-module-analytics' // wouldn't need analytics for a test
    ]

    FilterOutNonTestingExtensionsClassLoader(ClassLoader parent) {
        super(parent)
    }

    @Override
    Enumeration<URL> getResources(String name) throws IOException {
        // basically we are intercepting the call from DefaultMuleCoreExtensionManagerServer
        // here and avoiding loading extensions we don't need in a unit test environment
        def raw = super.getResources(name)
        if (name == 'META-INF/org/mule/runtime/core/config/core-extensions.properties') {
            def filtered = raw.toList().findAll() { resource ->
                !filters.any { filter -> resource.toString().contains(filter) }
            }
            return Collections.enumeration(filtered)
        }
        return raw
    }
}
