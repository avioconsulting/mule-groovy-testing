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

    private static boolean shouldInterceptExtensionSchemaGen(String name) {
        String schemaGenProperty = System.getProperty("avio.groovy.test.generate.xml.schemas");
        if (schemaGenProperty == null || schemaGenProperty.equals("false")) {
            return false;
        }
        return name.equals("META-INF/services/org.mule.runtime.extension.api.dsl.syntax.resources.spi.ExtensionSchemaGenerator");
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        // allows us to influence how ServiceLoader finds the DefaultExtensionSchemaGenerator class
        // we can put our own implementation in (if we want) and get access to the schemas
        return shouldInterceptExtensionSchemaGen(name)
                ? super.findResources("META-INF/ourschemagenoverride") :
                super.findResources(name);
    }
}
