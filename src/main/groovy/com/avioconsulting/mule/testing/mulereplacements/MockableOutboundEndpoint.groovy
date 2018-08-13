package com.avioconsulting.mule.testing.mulereplacements

import groovy.util.logging.Log4j2
import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.api.endpoint.OutboundEndpoint
import org.mule.api.processor.MessageProcessor

@Log4j2
class MockableOutboundEndpoint implements OutboundEndpoint {
    @Delegate
    private final OutboundEndpoint underlying
    private final MockingConfiguration mockingConfiguration

    MockableOutboundEndpoint(OutboundEndpoint underlying,
                             MockingConfiguration mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration
        this.underlying = underlying
    }

    @Override
    MuleEvent process(MuleEvent event) throws MuleException {
        if (!(underlying instanceof AnnotatedObject)) {
            log.warn 'Unable to find annotations/name for {}',
                     underlying
        } else {
            def mock = mockingConfiguration.getMockProcess(underlying as AnnotatedObject)
            if (mock) {
                assert underlying instanceof MessageProcessor
                return mock.process(event,
                                    underlying)
            }
        }
        underlying.process(event)
    }
}
