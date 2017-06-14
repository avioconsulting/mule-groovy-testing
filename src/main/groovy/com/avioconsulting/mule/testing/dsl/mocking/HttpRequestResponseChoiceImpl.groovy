package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.dsl.ConnectorType
import com.avioconsulting.mule.testing.transformers.HttpConnectorSpy
import com.avioconsulting.mule.testing.transformers.HttpVerbTransformer
import org.mule.api.MuleContext
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

class HttpRequestResponseChoiceImpl extends StandardRequestResponseImpl implements HttpRequestResponseChoice {
    private final HttpConnectorSpy httpConnectorSpy

    HttpRequestResponseChoiceImpl(MessageProcessorMocker muleMocker,
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

    def withHttpOptions(Closure closure) {
        def verbTransformer = new HttpVerbTransformer(httpConnectorSpy,
                                                      closure,
                                                      muleContext)
        // let verbs be processed first so they can be used in the payload transformers
        transformerChain.prependTransformer(verbTransformer)
    }
}
