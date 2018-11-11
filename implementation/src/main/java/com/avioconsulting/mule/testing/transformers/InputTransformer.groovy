package com.avioconsulting.mule.testing.transformers

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

interface InputTransformer<T extends ConnectorInfo> {
    def transformInput(EventWrapper input,
                       T connectorInfo)
}
