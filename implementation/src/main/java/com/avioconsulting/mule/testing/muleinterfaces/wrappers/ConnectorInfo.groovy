package com.avioconsulting.mule.testing.muleinterfaces.wrappers

import com.avioconsulting.mule.testing.transformers.ClosureEvaluationResponse

class ConnectorInfo<TClosureResponse extends ClosureEvaluationResponse> {
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

    // want to be able to differentiate between a deliberate null body and a connector that doesn't support body
    boolean isSupportsIncomingBody() {
        false
    }

    /**
     *
     * @return null if there is no custom body specified for the connector
     */
    Object getIncomingBody() {
        null
    }

    TClosureResponse evaluateClosure(EventWrapper event,
                                     Object input,
                                     Closure closure) {
        def result = closure.parameterTypes.size() == 0 ? closure() : closure(input)
        new ClosureEvaluationResponse(result)
    }

    EventWrapper transformEvent(EventWrapper incomingEvent,
                                TClosureResponse closureResponse) {
        incomingEvent
    }
}
