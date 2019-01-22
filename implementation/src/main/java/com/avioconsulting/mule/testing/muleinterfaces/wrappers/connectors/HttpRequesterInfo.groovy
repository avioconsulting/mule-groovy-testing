package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo

class HttpRequesterInfo extends
        ConnectorInfo implements HttpFunctionality {
    private final String method
    private final Map<String, String> queryParams
    private final Map<String, String> headers
    private HttpValidatorWrapper validatorWrapper
    private final String uri
    private final body

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
        def muleValidator = responseValidationSettings.responseValidator
        if (!muleValidator) {
            // Even if you choose 'None' for response validator in Studio 7, Mule will still validate against 200,201 by default
            // but if none is picked, we won't see a validator
            // so we just build one using the app's classloader
            def appClassLoader = responseValidationSettings.getClass().classLoader
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
    }

    private static Map convertMultiMap(Map map) {
        map.collectEntries { key, value ->
            [key, value instanceof Map ? convertMultiMap(value) : value]
        }
    }

    String getMethod() {
        this.method
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
}
