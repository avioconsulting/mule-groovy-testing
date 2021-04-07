package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.dsl.mocking.SOAPErrorThrowing
import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.ILookupFromRegistry
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.CustomErrorWrapperException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.MessageWrapperImpl
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.ClosureEvaluationResponse
import groovy.util.logging.Log4j2
import groovy.xml.DOMBuilder

import javax.xml.namespace.QName
import java.util.concurrent.TimeoutException

@Log4j2
class SoapConsumerInfo extends
        ConnectorInfo implements HttpFunctionality {
    private final boolean customTransport
    private final boolean validatorWorkaroundConfigured
    private final String uri
    private final String headers
    private HttpValidatorWrapper validatorWrapper
    // all SOAP requests are POSTs
    private static final String SOAP_METHOD = 'POST'
    @Lazy
    private Class dispatchExceptionClass = {
        fetchClassLoaders.appClassloader.loadClass('org.mule.runtime.soap.api.exception.DispatchingException')
    }()

    @Lazy
    private Class middleSoapFaultClass = {
        getSoapClass('org.mule.soap.api.exception.SoapFaultException')
    }()

    @Lazy
    private Class outerSoapFaultClass = {
        getSoapClass('org.mule.extension.ws.internal.error.SoapFaultMessageAwareException')
    }()

    @Lazy
    private Class cxfSoapFaultClass = {
        getSoapClass('org.apache.cxf.binding.soap.SoapFault')
    }()
    private final ILookupFromRegistry lookupFromRegistry

    private Class getSoapClass(String klass) {
        def artifactClassLoaders = fetchClassLoaders.appClassloader.getArtifactPluginClassLoaders() as List<ClassLoader>
        def value = artifactClassLoaders.findResult { ClassLoader cl ->
            try {
                cl.loadClass(klass)
            }
            catch (ClassNotFoundException e) {
                return null
            }
        }
        assert value: "Was not able to load ${klass} properly. Do you have the WSC consumer module in your POM?"
        value
    }


    SoapConsumerInfo(String fileName,
                     Integer lineNumber,
                     String container,
                     Map<String, Object> parameters,
                     IFetchClassLoaders fetchClassLoaders,
                     ILookupFromRegistry lookupFromRegistry) {
        super(fileName,
              lineNumber,
              container,
              parameters,
              fetchClassLoaders)
        this.lookupFromRegistry = lookupFromRegistry
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

    private def findValidator(transportConfig,
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
                                                       'requesterConfig') as String
        def requesterConfig = lookupFromRegistry.lookupByName(requesterConfigName).get().configuration.value
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
        def messageBody = parameters['message'].body
        if (messageBody instanceof InputStream) {
            return messageBody.text
        }
        // not on the Mule classloader side, cannot directly reference the class
        else if (messageBody.getClass().name == MessageWrapperImpl.TYPED_VALUE_CLASS_NAME) {
            def typedValueValue = messageBody.value
            if (typedValueValue instanceof InputStream) {
                return typedValueValue.text
            } else {
                throw new TestingFrameworkException("Do not understand type ${typedValueValue.getClass()}!")
            }
        } else {
            throw new TestingFrameworkException("Do not understand type ${messageBody.getClass()}!")
        }
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
                                               fetchClassLoaders.appClassloader)
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
                                            fetchClassLoaders.appClassloader)
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

    def throwSoapFault(String message,
                       QName faultCode,
                       QName subCode,
                       Closure detailClosure) {
        if (customHttpTransportConfigured) {
            if (!validatorWorkaroundConfigured) {
                log.warn 'You are throwing a SOAP fault from a SOAP mock on a WSC config with a custom transport configured. When you have this configuration, Mule will treat the likely HTTP 500 coming back from the SOAP server as an exception and never get to the SOAP fault. The testing framework is mirroring this behavior so that you know it is happening. You may want to configure a response validator on the custom transport/request config with a success range of 0..399,500 so that it stays out of the way.'
            }
            // We have no easy way of getting a validator setup
            def emptyResponse = new ByteArrayOutputStream()
            validator.validate(500,
                               'some fault',
                               [:],
                               emptyResponse)
        }
        def detailResult = detailClosure(DOMBuilder.newInstance())
        def detailString = detailResult ? detailResult.serialize() : '<detail/>'
        def cxfException = cxfSoapFaultClass.newInstance(message,
                                                         faultCode)
        def muleException = middleSoapFaultClass.newInstance(faultCode,
                                                             subCode,
                                                             '<?xml version="1.0" encoding="UTF-8"?>' + detailString,
                                                             message,
                                                             null,
                                                             // node
                                                             null,
                                                             cxfException) as Throwable
        throw new CustomErrorWrapperException(outerSoapFaultClass.newInstance(muleException) as Throwable,
                                              'WSC',
                                              'SOAP_FAULT')

    }

    @Override
    ClosureEvaluationResponse evaluateClosure(EventWrapper event,
                                              Object input,
                                              Closure closure,
                                              ClosureCurrier closureCurrier) {
        def connectorInfo = this
        def errorHandler = new SOAPErrorThrowing() {
            @Override
            def soapFault(String message,
                          QName faultCode,
                          QName subCode) {
                soapFault(message,
                          faultCode,
                          subCode) { builder ->
                    null
                }
            }

            @Override
            def soapFault(String message,
                          QName faultCode,
                          QName subCode,
                          Closure detailMarkupBuilderClosure) {
                connectorInfo.throwSoapFault(message,
                                             faultCode,
                                             subCode,
                                             detailMarkupBuilderClosure)
            }

            // TODO: Better class structure/remove this
            @Override
            def setHttpStatusCode(int code) {
                throw new Exception('Should be no need to change the HTTP status code on a SOAP reply')
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
        def curried = closureCurrier.curryClosure(closure,
                                                  event,
                                                  this)
        def result = curried.parameterTypes.size() == 0 ? curried() : curried(input)
        new ClosureEvaluationResponse(result)
    }
}
