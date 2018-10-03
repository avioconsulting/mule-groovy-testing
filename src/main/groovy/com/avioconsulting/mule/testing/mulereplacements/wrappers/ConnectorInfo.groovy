package com.avioconsulting.mule.testing.mulereplacements.wrappers

// TODO: Possibly subclass this for different connectors?
class ConnectorInfo {
    private final Map<String, Object> parameters
    private final String fileName
    private final int lineNumber
    private final Object componentId

    ConnectorInfo(String fileName,
                  Integer lineNumber,
                  Map<String, Object> parameters,
                  Object componentId) {
        this.componentId = componentId
        this.lineNumber = lineNumber
        this.fileName = fileName
        this.parameters = parameters
    }

    Map<String, Object> getParameters() {
        this.parameters
    }

    Object getComponentId() {
        return componentId
    }

    @Override
    String toString() {
        "${getClass().name} - in ${fileName}:${lineNumber}"
    }
}
