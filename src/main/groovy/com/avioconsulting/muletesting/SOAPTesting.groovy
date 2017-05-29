package com.avioconsulting.muletesting

import com.avioconsulting.muletesting.transformers.XmlRequestReplyTransformer
import org.mule.api.MuleEvent
import org.mule.munit.common.mocking.Attribute
import org.mule.munit.common.mocking.MessageProcessorMocker
import org.mule.munit.common.mocking.MunitSpy

trait SOAPTesting implements XmlTesting {
    abstract MessageProcessorMocker whenMessageProcessor(String name)
    abstract MunitSpy spyMessageProcessor(String name)

    private def applyXmlReplyCallback(MessageProcessorMocker mock,
                                      alternateFetcher,
                                      Closure testClosure) {
        mock.thenApply(new XmlRequestReplyTransformer(mock,
                                                      getJaxbContext(),
                                                      alternateFetcher,
                                                      this.getMockResourceFetcher(),
                                                      this.&getXmlMessage,
                                                      testClosure))
    }

    MessageProcessorMocker mockSoapReply(String name = null, boolean untilSuccessful = false, testClosure) {
        def mock = whenMessageProcessor('consumer').ofNamespace('ws')
        if (name != null) {
            mock.withAttributes(Attribute.attribute('name').ofNamespace('doc').withValue(name))
        }
        MuleEvent alternateEvent = null
        def alternateFetcher = null
        // until successful messes up ability to get request in mock
        if (untilSuccessful) {
            alternateFetcher = { ['until-successful', alternateEvent.message] }
            def spyClosure = { muleEvent ->
                alternateEvent = muleEvent
            }
            spyMessageProcessor('until-successful')
                    .before(new TestSpyProcess(spyClosure))
        }

        applyXmlReplyCallback(mock, alternateFetcher, testClosure)
        mock
    }
}