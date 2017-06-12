package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.HttpConnectorSpy
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker

class VmRequestResponse extends BaseRequestResponse {
    VmRequestResponse(MessageProcessorMocker muleMocker,
                      MuleContext muleContext,
                      List<Class> allowedPayloadTypes,
                      ConnectorType connectorType) {
        super(muleMocker, muleContext, allowedPayloadTypes, connectorType)
    }

    // TODO: Remove this once JSONFormatter has its query params stuff fixed
    HttpConnectorSpy getHttpConnectorSpy() {
        null
    }
}
