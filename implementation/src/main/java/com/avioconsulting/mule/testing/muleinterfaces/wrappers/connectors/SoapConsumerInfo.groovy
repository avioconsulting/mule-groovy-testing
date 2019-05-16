package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.dsl.mocking.SoapErrorThrowing
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.CustomErrorWrapperException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.ClosureEvaluationResponse

import javax.xml.namespace.QName
import java.util.concurrent.TimeoutException

class SoapConsumerInfo extends
        ConnectorInfo implements HttpFunctionality {
    private final boolean customTransport
    private final boolean validatorWorkaroundConfigured
    private final String uri
    private final String headers
    private HttpValidatorWrapper validatorWrapper
    // all SOAP requests are POSTs
    private static final String SOAP_METHOD = 'POST'
    private final ClassLoader appClassLoader
    @Lazy
    private Class dispatchExceptionClass = {
        appClassLoader.loadClass('org.mule.runtime.soap.api.exception.DispatchingException')
    }()


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
        this.appClassLoader = transportConfig.getClass().classLoader
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

    private def throwConnectionException() {
        Throwable exception
        if (customHttpTransportConfigured) {
            exception = getConnectionException(uri,
                                               SOAP_METHOD,
                                               appClassLoader)
        } else {
            exception = wrapWithCustom(dispatchExceptionClass.newInstance('An error occurred while sending the SOAP request') as Throwable)
        }
        throw exception
    }

    private def throwTimeoutException() {
        Throwable exception
        if (customHttpTransportConfigured) {
            exception = getTimeoutException(uri,
                                            SOAP_METHOD,
                                            appClassLoader)
        } else {
            exception = wrapWithCustom(dispatchExceptionClass.newInstance('The SOAP request timed out',
                                                                          new TimeoutException('HTTP timeout!')) as Throwable)
        }
        throw exception
    }

    private static Exception wrapWithCustom(Throwable dispatchException) {
        // If you don't use the custom transport, all exceptions seem to have this type code
        new CustomErrorWrapperException(dispatchException,
                                        'WSC',
                                        'CANNOT_DISPATCH')
    }

    @Override
    ClosureEvaluationResponse evaluateClosure(EventWrapper event,
                                              Object input,
                                              Closure closure) {
        def errorHandler = new SoapErrorThrowing() {
            @Override
            def soapFault(String message,
                          QName faultCode,
                          QName subCode) {
                return null
            }

            @Override
            def soapFault(String message,
                          QName faultCode,
                          QName subCode,
                          Closure detailMarkupBuilderClosure) {
                return null
            }

            // TODO: Better class structure/remove this
            @Override
            def setHttpStatusCode(int code) {
                return null
            }

            @Override
            def httpConnectError() {
                throwConnectionException()
            }

            @Override
            def httpTimeoutError() {
                throwTimeoutException()
            }
        }
        closure = closure.rehydrate(errorHandler,
                                    closure.owner,
                                    closure.thisObject)
        def result = closure.parameterTypes.size() == 0 ? closure() : closure(input)
        new ClosureEvaluationResponse(result)
    }
}
