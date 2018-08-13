package com.avioconsulting.mule.testing.mulereplacements.namespacefix

import org.springframework.beans.factory.xml.NamespaceHandler
import org.springframework.beans.factory.xml.NamespaceHandlerResolver

// solely here to insert WrappedNamespaceHandler into the chain
class AnnotatedNamespaceHandlerResolver implements NamespaceHandlerResolver {
    @Delegate
    private final NamespaceHandlerResolver delegate

    AnnotatedNamespaceHandlerResolver(NamespaceHandlerResolver delegate) {
        this.delegate = delegate
    }

    @Override
    NamespaceHandler resolve(String namespaceUri) {
        new WrappedNamespaceHandler(delegate.resolve(namespaceUri))
    }
}
