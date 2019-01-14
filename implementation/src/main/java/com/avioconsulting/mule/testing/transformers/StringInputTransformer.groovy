package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class StringInputTransformer<T extends ConnectorInfo> implements
        InputTransformer<T> {
    def transformInput(EventWrapper muleEvent,
                       T connectorInfo) {
        def muleMessage = muleEvent.message
        // comes back from some Mule connectors like JSON
        if (muleMessage.payload == null) {
            return null
        }
        if (muleMessage.dataTypeClass != String) {
            throw new TestingFrameworkException(
                    "Expected payload to be of type String here but it actually was ${muleMessage.dataTypeClass}. Check the connectors you're mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings)")
        }
        muleMessage.valueInsideTypedValue
    }

    def disableStreaming() {
        // we already expect a string
    }
}
