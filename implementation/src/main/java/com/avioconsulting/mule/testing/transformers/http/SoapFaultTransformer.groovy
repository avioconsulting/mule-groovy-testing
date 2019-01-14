package com.avioconsulting.mule.testing.transformers.http


import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.CustomErrorWrapperException
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.SoapConsumerInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset
import groovy.util.logging.Log4j2
import groovy.xml.DOMBuilder

import javax.xml.namespace.QName

@Log4j2
class SoapFaultTransformer implements IHaveStateToReset,
        MuleMessageTransformer<SoapConsumerInfo> {
    private final IFetchClassLoaders fetchAppClassLoader
    private String message
    private QName faultCode
    private QName subCode
    private Closure detailClosure

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

    SoapFaultTransformer(IFetchClassLoaders fetchAppClassLoader) {
        this.fetchAppClassLoader = fetchAppClassLoader
        reset()
    }

    def triggerSoapFault(String message,
                         QName faultCode,
                         QName subCode,
                         Closure detailClosure) {
        this.message = message
        this.faultCode = faultCode
        this.subCode = subCode
        this.detailClosure = detailClosure
    }

    @Override
    EventWrapper transform(EventWrapper event,
                           SoapConsumerInfo connectorInfo) {
        if (message == null) {
            return event
        }
        if (connectorInfo.customHttpTransportConfigured) {
            log.warn 'You are throwing a SOAP fault from a SOAP mock on a WSC config with a custom transport configured. When you have this configuration, Mule will treat the likely HTTP 500 coming back from the SOAP server as an exception and never get to the SOAP fault. The testing framework is mirroring this behavior so that you know it is happening.'
            // We have no easy way of getting a validator setup
            connectorInfo.validator.validate(500,
                                             'some fault',
                                             [:])
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
    def reset() {
        this.message = null
        this.faultCode = null
        this.subCode = null
        this.detailClosure = null
    }
}
