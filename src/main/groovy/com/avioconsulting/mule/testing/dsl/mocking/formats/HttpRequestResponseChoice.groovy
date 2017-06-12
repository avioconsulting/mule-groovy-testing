package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.HttpConnectorSpy
import com.avioconsulting.mule.testing.transformers.HttpVerbTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

class HttpRequestResponseChoice extends BaseRequestResponse {
    private final HttpConnectorSpy httpConnectorSpy

    HttpRequestResponseChoice(MessageProcessorMocker muleMocker,
                              MunitSpy spy,
                              ProcessorLocator processorLocator,
                              MuleContext muleContext,
                              List<Class> allowedPayloadTypes,
                              ConnectorType connectorType) {
        super(muleMocker,
              muleContext,
              allowedPayloadTypes,
              connectorType)
        this.httpConnectorSpy = new HttpConnectorSpy(processorLocator,
                                                     muleContext)
        spy.before(httpConnectorSpy)
    }

    def withHttpVerb(Closure closure) {
        def verbTransformer = new HttpVerbTransformer(httpConnectorSpy,
                                                      closure,
                                                      muleContext)
        transformerChain.addTransformer(verbTransformer)
    }

    HttpConnectorSpy getHttpConnectorSpy() {
        httpConnectorSpy
    }
}
