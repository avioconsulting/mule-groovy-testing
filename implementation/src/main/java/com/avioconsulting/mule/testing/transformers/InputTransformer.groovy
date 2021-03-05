package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

/**
 * Job is to transform from a Mule "serialized" event to a real object that can be used
 * in the mock's "whenCalledWith" closure to capture mock payloads for assertions
 * @param <T>
 */
interface InputTransformer<T extends ConnectorInfo> {
    def transformInput(EventWrapper input,
                       T connectorInfo)
}
