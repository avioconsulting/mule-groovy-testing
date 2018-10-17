package com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

class SoapConsumerInfo extends
        ConnectorInfo {
    private final boolean customTransport
    private final String uri

    SoapConsumerInfo(String fileName,
                     Integer lineNumber,
                     String container,
                     Map<String, Object> parameters) {
        super(fileName,
              lineNumber,
              container,
              parameters)
        def connection = parameters['connection']
        this.customTransport = connection.transportConfiguration.isPresent()
        this.uri = connection.client.address
    }

    boolean isCustomHttpTransportConfigured() {
        customTransport
    }

    String getUri() {
        uri
    }
}
