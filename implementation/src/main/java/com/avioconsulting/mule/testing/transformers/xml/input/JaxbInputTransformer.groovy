package com.avioconsulting.mule.testing.transformers.xml.input

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.InputTransformer
import com.avioconsulting.mule.testing.transformers.xml.JAXBMarshalHelper
import groovy.util.logging.Log4j2

@Log4j2
class JaxbInputTransformer<T extends ConnectorInfo> implements InputTransformer<T> {
    private final JAXBMarshalHelper helper

    JaxbInputTransformer(JAXBMarshalHelper helper) {
        this.helper = helper
    }

    @Override
    def transformInput(EventWrapper event,
                       T connectorInfo) {
        helper.unmarshal(event,
                         connectorInfo)
    }
}
