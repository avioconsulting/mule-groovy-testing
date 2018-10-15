package com.avioconsulting.mule.testing.mulereplacements.wrappers

// need a way to communicate the underlying namespace back to MockingProcessorInterceptor
// this is what org.mule.runtime.module.extension.internal.runtime.exception.ModuleExceptionHandler
// does for the real thing
// this namespace may or may not be the same as the component's
// for example, SOAP with a custom transport will throw HTTP errors, not WSC
class ModuleExceptionWrapper extends Exception {
    private final String namespace

    ModuleExceptionWrapper(Exception moduleException,
                           String namespace) {
        super(moduleException)
        this.namespace = namespace
    }

    String getNamespace() {
        return namespace
    }
}
