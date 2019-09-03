package com.avioconsulting.mule.testing.muleinterfaces.wrappers

import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.ClosureEvaluationResponse

class ConnectorInfo<TClosureResponse extends ClosureEvaluationResponse> {
    private final Map<String, Object> parameters
    private final String fileName, container
    private final Integer lineNumber
    String name
    protected final IFetchClassLoaders fetchClassLoaders

    ConnectorInfo(String fileName,
                  Integer lineNumber,
                  String container,
                  Map<String, Object> parameters,
                  IFetchClassLoaders fetchClassLoaders) {
        this.fetchClassLoaders = fetchClassLoaders
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
                                     Closure closure,
                                     ClosureCurrier closureCurrier) {
        def curried = closureCurrier.curryClosure(closure,
                                                  event,
                                                  this)
        def result = curried.parameterTypes.size() == 0 ? curried() : curried(input)
        new ClosureEvaluationResponse(result)
    }

    EventWrapper transformEvent(EventWrapper incomingEvent,
                                TClosureResponse closureResponse) {
        incomingEvent
    }
}
