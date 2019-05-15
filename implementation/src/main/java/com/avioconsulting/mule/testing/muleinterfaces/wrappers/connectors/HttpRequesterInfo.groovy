package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.dsl.mocking.ErrorThrowing
import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class HttpRequesterInfo extends
        ConnectorInfo implements HttpFunctionality {
    private final String method
    private final Map<String, String> queryParams
    private final Map<String, String> headers
    private HttpValidatorWrapper validatorWrapper
    private final String uri
    private final body
    private final String mimeType
    private final ClassLoader appClassLoader

    // don't make HttpAttributeBuilder publicly visible
    private class InnerHttp implements HttpAttributeBuilder {
    }

    HttpRequesterInfo(String fileName,
                      Integer lineNumber,
                      String container,
                      Map<String, Object> parameters) {
        super(fileName,
              lineNumber,
              container,
              parameters)
        this.method = parameters['method'] as String
        def responseValidationSettings = parameters['responseValidationSettings']
        if (!responseValidationSettings) {
            // At the moment, there doesn't seem to be a way to get the actual DW problem that leads to this
            throw new Exception('Usually HTTP requesters have responseValidationSettings set on them. This one does not. This usually happens when the DW 2.0 logic that builds HTTP headers, query params, etc has a DW error in it. Check your DW logic in <http:headers> etc. carefully')
        }
        def muleValidator = responseValidationSettings.responseValidator
        appClassLoader = responseValidationSettings.getClass().classLoader
        if (!muleValidator) {
            // Even if you choose 'None' for response validator in Studio 7, Mule will still validate against 200,201 by default
            // but if none is picked, we won't see a validator
            // so we just build one using the app's classloader
            muleValidator = getValidator(appClassLoader)
        }
        // it's a MultiMap, keep Mule runtime classes away from our tests
        def requestBuilder = parameters['requestBuilder']
        this.body = requestBuilder.body
        this.queryParams = convertMultiMap(requestBuilder.queryParams) as Map<String, String>
        this.headers = convertMultiMap(requestBuilder.headers) as Map<String, String>
        def uriParams = parameters['client'].defaultUriParameters
        def host = "${uriParams.scheme.scheme}://${uriParams.host}:${uriParams.port}"
        def path = requestBuilder.replaceUriParams(parameters['uriSettings'].path)
        this.uri = "${host}${path}"
        this.validatorWrapper = new HttpValidatorWrapper(muleValidator,
                                                         this)
        this.mimeType = parameters['outputMimeType']
    }

    private static Map convertMultiMap(Map map) {
        map.collectEntries { key, value ->
            [key, value instanceof Map ? convertMultiMap(value) : value]
        }
    }

    String getMethod() {
        this.method
    }

    String getMimeType() {
        this.mimeType
    }

    Map<String, String> getQueryParams() {
        return queryParams
    }

    Map<String, String> getHeaders() {
        return headers
    }

    HttpValidatorWrapper getValidator() {
        this.validatorWrapper
    }

    String getUri() {
        return uri
    }

    @Override
    boolean isSupportsIncomingBody() {
        true
    }

    @Override
    Object getIncomingBody() {
        // no payloads should be a part of GET
        if (this.method == 'GET') {
            return null
        }
        def value = this.body.value
        if (value instanceof InputStream) {
            return value.text
        }
        return value
    }

    def getHttpResponseAttributes(int statusCode,
                                  String reasonPhrase,
                                  Map additionalHeaders = [:]) {
        def inner = new InnerHttp()
        inner.getHttpResponseAttributes(statusCode,
                                        reasonPhrase,
                                        appClassLoader,
                                        additionalHeaders)
    }

    @Override
    def closureEvaluator(EventWrapper event) {
        new ErrorThrowing() {
            @Override
            def setHttpReturnCode(Integer code) {
                println 'we got it!'
                return null
            }

            @Override
            def httpConnectError() {
                return null
            }

            @Override
            def httpTimeoutError() {
                return null
            }
        }
    }
}
