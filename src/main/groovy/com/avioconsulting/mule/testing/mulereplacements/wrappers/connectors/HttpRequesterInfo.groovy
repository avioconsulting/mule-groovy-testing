package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

class HttpRequesterInfo extends
        ConnectorInfo {
    private final String method
    private final boolean validationEnabled
    private final Map<String, String> queryParams
    private final Map<String, String> headers
    private HttpValidatorWrapper validatorWrapper
    private final String uri

    HttpRequesterInfo(String fileName,
                      Integer lineNumber,
                      Map<String, Object> parameters) {
        super(fileName,
              lineNumber,
              parameters)
        this.method = parameters['method'] as String
        def muleValidator = parameters['responseValidationSettings'].responseValidator
        this.validationEnabled = muleValidator != null
        if (this.validationEnabled) {
            this.validatorWrapper = new HttpValidatorWrapper(muleValidator,
                                                             this)
        }
        // it's a MultiMap, keep Mule runtime classes away from our tests
        def requestBuilder = parameters['requestBuilder']
        this.queryParams = convertMultiMap(requestBuilder.queryParams) as Map<String, String>
        this.headers = convertMultiMap(requestBuilder.headers) as Map<String, String>
        this.uri = requestBuilder.replaceUriParams(parameters['uriSettings'].path)
    }

    private static Map convertMultiMap(Map map) {
        map.collectEntries { key, value ->
            [key, value instanceof Map ? convertMultiMap(value) : value]
        }
    }

    String getMethod() {
        this.method
    }

    boolean isValidationEnabled() {
        this.validationEnabled
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
}
