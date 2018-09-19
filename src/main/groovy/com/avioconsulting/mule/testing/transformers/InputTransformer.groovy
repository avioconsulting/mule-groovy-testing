package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface InputTransformer<T extends ConnectorInfo> {
    def transformInput(EventWrapper input,
                                T connectorInfo)

    def disableStreaming()
}
