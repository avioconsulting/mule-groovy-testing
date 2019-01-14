package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

trait HttpFunctionality {
    def getValidator(ClassLoader classLoader,
                     String successCodes = '0..399') {
        def validatorClass = classLoader
                .loadClass('org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator')
        validatorClass.newInstance(successCodes)
    }
}
