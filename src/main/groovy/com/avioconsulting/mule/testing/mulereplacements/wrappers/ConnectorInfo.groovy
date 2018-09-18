package com.avioconsulting.mule.testing.mulereplacements.wrappers

class ConnectorInfo {
    private final Map<String, Object> parameters

    ConnectorInfo(String fileName,
                  Integer lineNumber,
                  Map<String, Object> parameters) {
        this.parameters = parameters
    }
}
