package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.CustomErrorWrapperException
import com.avioconsulting.mule.testing.transformers.http.WsConsumerConnectorErrorTransformer
import com.avioconsulting.mule.testing.transformers.xml.XMLMessageBuilder
import groovy.xml.DOMBuilder

import javax.xml.namespace.QName

class SOAPFormatterImpl extends
        XMLFormatterImpl implements
        SOAPFormatter {
    private Class getSoapClass(String klass) {
        def artifactClassLoaders = fetchAppClassLoader.appClassloader.getArtifactPluginClassLoaders() as List<ClassLoader>
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

    private WsConsumerConnectorErrorTransformer httpConnectorErrorTransformer
    private final IFetchClassLoaders fetchAppClassLoader

    SOAPFormatterImpl(IFetchClassLoaders fetchAppClassLoader) {
        super(XMLMessageBuilder.MessageType.Soap)
        this.fetchAppClassLoader = fetchAppClassLoader
    }

    def httpConnectError() {
        httpConnectorErrorTransformer = new WsConsumerConnectorErrorTransformer(fetchAppClassLoader)
        httpConnectorErrorTransformer.triggerConnectException()
        // avoid DSL weirdness
        return null
    }

    def httpTimeoutError() {
        httpConnectorErrorTransformer = new WsConsumerConnectorErrorTransformer(fetchAppClassLoader)
        httpConnectorErrorTransformer.triggerTimeoutException()
        // avoid DSL weirdness
        return null
    }

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
                  Closure closure) {
        def detailResult = closure(DOMBuilder.newInstance())
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
    MuleMessageTransformer getTransformer() {
        if (httpConnectorErrorTransformer) {
            return httpConnectorErrorTransformer
        }
        return super.transformer
    }
}
