package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

class HttpRequesterInfo extends ConnectorInfo {
    private final String method

    HttpRequesterInfo(String fileName,
                      Integer lineNumber,
                      Map<String, Object> parameters,
                      String method) {
        super(fileName,
              lineNumber,
              parameters)
        this.method = method
    }

    String getMethod() {
        this.method
    }
}
