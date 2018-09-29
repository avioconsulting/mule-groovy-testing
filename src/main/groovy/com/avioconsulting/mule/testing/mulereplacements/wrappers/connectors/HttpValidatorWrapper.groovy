package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

class HttpValidatorWrapper {
    private final Object muleValidator
    private final HttpRequesterInfo httpRequesterInfo
    private final Class httpResponseAttrClass
    private final Class multiMapClass
    private final Object httpRequestBuilder
    private final Object muleResultBuilder

    HttpValidatorWrapper(Object muleValidator,
                         HttpRequesterInfo httpRequesterInfo) {
        this.httpRequesterInfo = httpRequesterInfo
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
                 Map<String, String> headers) {
        def httpRequest = httpRequestBuilder
                .method(httpRequesterInfo.method)
                .uri(httpRequesterInfo.uri)
                .build()
        def multiMap = multiMapClass.newInstance(headers)
        def httpResponseAttr = httpResponseAttrClass.newInstance(statusCode,
                                                                 reasonPhrase,
                                                                 multiMap)
        def result = this.muleResultBuilder
                .attributes(httpResponseAttr)
                .output(new ByteArrayInputStream())
                .build()
        muleValidator.validate(result,
                               httpRequest)
    }
}
