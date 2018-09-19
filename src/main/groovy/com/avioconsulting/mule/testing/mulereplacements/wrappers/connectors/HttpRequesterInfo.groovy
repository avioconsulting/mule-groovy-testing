package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

class HttpRequesterInfo extends
        ConnectorInfo {
    private final String method
    private final boolean validationEnabled

    HttpRequesterInfo(String fileName,
                      Integer lineNumber,
                      Map<String, Object> parameters) {
        super(fileName,
              lineNumber,
              parameters)
        this.method = parameters['method'] as String
        this.validationEnabled = parameters['responseValidationSettings'].responseValidator != null
    }

    String getMethod() {
        this.method
    }

    // TODO: Will need to fully implement this later on
    boolean isValidationEnabled() {
        this.validationEnabled
    }
}
