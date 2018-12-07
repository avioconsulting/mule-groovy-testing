package com.avioconsulting.mule.testing.muleinterfaces.wrappers

class ConnectorInfo {
    private final Map<String, Object> parameters
    private final String fileName, container
    private final int lineNumber
    String name

    ConnectorInfo(String fileName,
                  Integer lineNumber,
                  String container,
                  Map<String, Object> parameters) {
        this.lineNumber = lineNumber
        this.container = container
        this.fileName = fileName
        this.parameters = parameters
    }

    Map<String, Object> getParameters() {
        this.parameters
    }

    @Override
    String toString() {
        "${getClass().name} - ${name} - in ${fileName}:${lineNumber}"
    }

    String getFileName() {
        return fileName
    }

    int getLineNumber() {
        return lineNumber
    }

    String getContainer() {
        return container
    }

    String getTargetFlowVariable() {
        parameters['target']
    }

    /**
     *
     * @return null if there is no custom body specified for the connector
     */
    Object getIncomingBody() {
        null
    }
}
