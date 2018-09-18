package com.avioconsulting.mule.testing.mulereplacements.wrappers

// TODO: Possibly subclass this for different connectors?
class ConnectorInfo {
    private final Map<String, Object> parameters

    ConnectorInfo(String fileName,
                  Integer lineNumber,
                  Map<String, Object> parameters) {
        this.parameters = parameters
    }
}
