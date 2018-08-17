package com.avioconsulting.mule.testing.mulereplacements.endpoints

import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import org.mule.api.MuleException
import org.mule.api.endpoint.EndpointBuilder
import org.mule.api.endpoint.EndpointFactory
import org.mule.api.endpoint.OutboundEndpoint

// just need to be abled to tweaked which endpoints are produced
class OverrideEndpointFactory implements EndpointFactory {
    @Delegate
    private EndpointFactory underlying
    private final MockingConfiguration mockingConfiguration

    OverrideEndpointFactory(EndpointFactory underlying,
                            MockingConfiguration mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration
        this.underlying = underlying
    }

    @Override
    OutboundEndpoint getOutboundEndpoint(EndpointBuilder builder) throws MuleException {
        def endpoint = underlying.getOutboundEndpoint(builder)
        new MockableOutboundEndpoint(endpoint,
                                     mockingConfiguration)
    }
}
