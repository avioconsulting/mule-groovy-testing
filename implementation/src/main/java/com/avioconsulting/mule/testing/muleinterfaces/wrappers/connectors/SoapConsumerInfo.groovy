package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo

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

    @Override
    String getIncomingBody() {
        def value = parameters['message'].body
        if (value instanceof InputStream) {
            return value.text
        }
        throw new Exception("Do not understand type ${value.getClass()}!")
    }
}
