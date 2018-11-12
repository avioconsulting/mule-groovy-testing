package com.avioconsulting.mule.testing.transformers.json.input

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.InputTransformer

abstract class Common<T extends ConnectorInfo> implements
        InputTransformer<T> {
    abstract def transform(String jsonString)

    def transformInput(EventWrapper muleEvent,
                       T messageProcessor) {
        // comes back from some Mule connectors like JSON
        if (muleEvent.message.payload == null) {
            return null
        }
        def jsonString = muleEvent.message.messageAsString
        return transform(jsonString)
    }
}
