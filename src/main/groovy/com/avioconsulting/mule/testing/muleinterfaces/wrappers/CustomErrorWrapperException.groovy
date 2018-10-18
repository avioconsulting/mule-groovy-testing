package com.avioconsulting.mule.testing.muleinterfaces.wrappers

// some errors may not use ModuleExceptions and thus they will not have ErrorTypeDefinitions
// see ModuleExceptionWrapper for more details
class CustomErrorWrapperException extends
        Exception {
    private final String namespace
    private final String errorCode

    CustomErrorWrapperException(Throwable otherException,
                                String namespace,
                                String errorCode) {
        super(otherException)
        this.errorCode = errorCode
        this.namespace = namespace
    }

    String getNamespace() {
        return namespace
    }

    String getErrorCode() {
        return errorCode
    }
}
