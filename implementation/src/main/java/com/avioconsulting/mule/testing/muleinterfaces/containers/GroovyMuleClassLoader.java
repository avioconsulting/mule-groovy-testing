package com.avioconsulting.mule.testing.muleinterfaces.containers;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class GroovyMuleClassLoader extends URLClassLoader {
    public GroovyMuleClassLoader(URL[] urls,
                                 ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return name.equals("META-INF/services/org.mule.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator")
                ? super.findResources("META-INF/ourschemagenoverride") :
                super.findResources(name);
    }
}
