package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo

class SoapConsumerInfo extends
        ConnectorInfo implements HttpFunctionality {
    private final boolean customTransport
    private final boolean validatorWorkaroundConfigured
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
        def transportConfig = connection.transportConfiguration
        this.customTransport = transportConfig.getClass().getName().contains('CustomHttpTransportConfiguration')
        this.uri = connection.info.address
        this.headers = parameters['message'].headers?.text
        Object validator = null
        if (customTransport) {
            validator = findValidator(transportConfig,
                                      parameters)
        }
        if (!validator) {
            // create one by default just like Mule does
            validator = getValidator(parameters['transportConfig'].getClass().classLoader)
        } else {
            this.validatorWorkaroundConfigured = true
        }
        this.validatorWrapper = new HttpValidatorWrapper(validator,
                                                         'POST',
                                                         // all SOAP reqs should be POSTs
                                                         this.uri)
    }

    private static def findValidator(transportConfig,
                                     Map<String, Object> parameters) {
        def getPrivateFieldValue = { Object object,
                                     String fieldName ->
            def klass = object.getClass()
            if (klass.name.contains('EnhancerByCGLIB')) {
                // proxies get in the way if we try and use the actual class to find the private field
                klass = klass.superclass
            }
            def field = klass.getDeclaredField(fieldName)
            assert field: "Expected to find ${fieldName}"
            field.accessible = true
            field.get(object)
        }
        def requesterConfigName = getPrivateFieldValue(transportConfig,
                                                       'requesterConfig')
        def requesterConfig = parameters['client'].registry.lookupByName(requesterConfigName).value.configuration.value
        def responseSettings = getPrivateFieldValue(requesterConfig,
                                                    'responseSettings')
        getPrivateFieldValue(responseSettings,
                             'responseValidator')
    }

    boolean isCustomHttpTransportConfigured() {
        customTransport
    }

    boolean isValidatorWorkaroundConfigured() {
        validatorWorkaroundConfigured
    }

    String getUri() {
        uri
    }

    @Override
    boolean isSupportsIncomingBody() {
        true
    }

    @Override
    Object getIncomingBody() {
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
