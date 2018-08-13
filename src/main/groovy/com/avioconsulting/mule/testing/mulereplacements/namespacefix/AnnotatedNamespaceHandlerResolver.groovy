package com.avioconsulting.mule.testing.mulereplacements.namespacefix

import org.springframework.beans.factory.xml.NamespaceHandler
import org.springframework.beans.factory.xml.NamespaceHandlerResolver

// solely here to insert WrappedNamespaceHandler into the chain
class AnnotatedNamespaceHandlerResolver implements NamespaceHandlerResolver {
    @Delegate
    NamespaceHandlerResolver delegate

    @Override
    NamespaceHandler resolve(String namespaceUri) {
        def wrapped = new WrappedNamespaceHandler()
        wrapped.wrapped = delegate.resolve(namespaceUri)
        return wrapped
    }
}
