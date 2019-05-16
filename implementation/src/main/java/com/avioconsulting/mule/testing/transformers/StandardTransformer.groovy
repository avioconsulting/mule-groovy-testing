package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class StandardTransformer<T extends ConnectorInfo> implements
        MuleMessageTransformer<T> {
    private final OutputTransformer outputTransformer
    private final InputTransformer<T> inputTransformer
    private final Closure closure
    private final ClosureCurrier closureCurrier

    StandardTransformer(Closure closure,
                        ClosureCurrier closureCurrier,
                        InputTransformer<T> inputTransformer,
                        OutputTransformer outputTransformer) {
        this.closureCurrier = closureCurrier
        this.closure = closure
        this.inputTransformer = inputTransformer
        this.outputTransformer = outputTransformer
    }

    EventWrapper transform(EventWrapper muleEvent,
                           T connectorInfo) {
        // now we don't have to worry about feeding event or connector info into the test's
        // mock closure, let's transform the event into the format the test mock wants it in
        def input = inputTransformer.transformInput(muleEvent,
                                                    connectorInfo)
        // now we can actually evaluate the whenCalledWith closure
        def closureResponse = connectorInfo.evaluateClosure(muleEvent,
                                                            input,
                                                            closure,
                                                            closureCurrier)
        // now we have a response, let's go ahead and create a new event
        // with the right format
        def event = outputTransformer.transformOutput(closureResponse.response,
                                                      muleEvent,
                                                      connectorInfo)
        // allows the connector wrapper to make any last changes to attributes, etc.
        connectorInfo.transformEvent(event,
                                     closureResponse)
    }
}
