package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo

class SoapConsumerInfo extends
        ConnectorInfo implements HttpFunctionality {
    private final boolean customTransport
    private final String uri
    private final String headers
    private HttpValidatorWrapper validatorWrapper

    SoapConsumerInfo(String fileName,
                     Integer lineNumber,
                     String container,
                     Map<String, Object> parameters) {
        super(fileName,
              lineNumber,
              container,
              parameters)
        def connection = parameters['connection']
        this.customTransport = connection.transportConfiguration.getClass().getName().contains('CustomHttpTransportConfiguration')
        this.uri = connection.info.address
        this.headers = parameters['message'].headers?.text
        def validator = getValidator(parameters['transportConfig'].getClass().classLoader)
        this.validatorWrapper = new HttpValidatorWrapper(validator,
                                                         'POST', // all SOAP reqs should be POSTs
                                                         this.uri)
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
        throw new TestingFrameworkException("Do not understand type ${value.getClass()}!")
    }

    String getHeaders() {
        this.headers
    }

    HttpValidatorWrapper getValidator() {
        this.validatorWrapper
    }
}
