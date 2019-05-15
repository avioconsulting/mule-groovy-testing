package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ModuleExceptionWrapper

class HttpValidatorWrapper {
    private final Object muleValidator
    private final Class httpResponseAttrClass
    private final Class multiMapClass
    private final Object httpRequestBuilder
    private final Object muleResultBuilder
    private final String httpMethod
    private final String httpUri

    HttpValidatorWrapper(Object muleValidator,
                         HttpRequesterInfo httpRequesterInfo) {
        this(muleValidator,
             httpRequesterInfo.method,
             httpRequesterInfo.uri)
    }

    HttpValidatorWrapper(Object muleValidator,
                         String httpMethod,
                         String httpUri) {
        this.httpUri = httpUri
        this.httpMethod = httpMethod
        this.muleValidator = muleValidator
        def muleClassLoader = muleValidator.class.classLoader
        this.httpResponseAttrClass = muleClassLoader.loadClass('org.mule.extension.http.api.HttpResponseAttributes')
        this.multiMapClass = muleClassLoader.loadClass('org.mule.runtime.api.util.MultiMap')
        def httpRequestClass = muleClassLoader.loadClass('org.mule.runtime.http.api.domain.message.request.HttpRequest')
        this.httpRequestBuilder = httpRequestClass.builder()
        def muleResultClass = muleClassLoader.loadClass('org.mule.runtime.extension.api.runtime.operation.Result')
        this.muleResultBuilder = muleResultClass.builder()
    }

    def validate(int statusCode,
                 String reasonPhrase,
                 Map<String, String> headers,
                 Object errorResponse) {
        def httpRequest = httpRequestBuilder
                .method(httpMethod)
                .uri(httpUri)
                .build()
        def multiMap = multiMapClass.newInstance(headers)
        def httpResponseAttr = httpResponseAttrClass.newInstance(statusCode,
                                                                 reasonPhrase,
                                                                 multiMap)
        def result = this.muleResultBuilder
                .attributes(httpResponseAttr)
                .output(errorResponse)
                .build()
        try {
            muleValidator.validate(result,
                                   httpRequest)
        }
        catch (e) {
            // ResponseValidatorTypedException extends from ModuleException but others do not
            // this way our mock/error type handling works right (see interceptors/mocking config)
            if (e.getClass().name.endsWith('ResponseValidatorTypedException')) {
                throw new ModuleExceptionWrapper(e,
                                                 'HTTP')
            }
            throw e
        }
    }
}
