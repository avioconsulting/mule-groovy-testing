package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

class HttpRequesterInfo extends
        ConnectorInfo {
    private final String method
    private final boolean validationEnabled
    private final Map<String, String> queryParams

    HttpRequesterInfo(String fileName,
                      Integer lineNumber,
                      Map<String, Object> parameters) {
        super(fileName,
              lineNumber,
              parameters)
        this.method = parameters['method'] as String
        this.validationEnabled = parameters['responseValidationSettings'].responseValidator != null
        // it's a MultiMap, keep Mule runtime classes away from our tests
        this.queryParams = convertMultiMap(parameters['requestBuilder'].queryParams) as Map<String, String>
    }

    private static Map convertMultiMap(Map map) {
        map.collectEntries { key, value ->
            [key, value instanceof Map ? convertMultiMap(value) : value]
        }
    }

    String getMethod() {
        this.method
    }

    // TODO: Will need to fully implement this later on
    boolean isValidationEnabled() {
        this.validationEnabled
    }

    Map<String, String> getQueryParams() {
        return queryParams
    }
}
