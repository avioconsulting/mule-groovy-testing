package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.TransformingEventFactory
import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class XMLMessageBuilder {
    // don't want to tie ourselves to a given version of CXF by expressing a compile dependency
    @Lazy
    private static Class apikitStreamReaderClass = {
        try {
            XMLMessageBuilder.classLoader.loadClass('org.mule.module.soapkit.NamespaceRestorerXMLStreamReader')
        }
        catch (e) {
            throw new Exception('Was not able to load NamespaceRestorerXMLStreamReader properly. You need to have CXF in your project to use the XML functions. Consider adding the org.mule.modules:mule-module-apikit-soap:1.0.3 dependency with at least test scope to your project',
                                e)
        }
    }()

    private final boolean wrapWithApiKitStreamReader
    private final TransformingEventFactory transformingEventFactory

    XMLMessageBuilder(boolean wrapWithApiKitStreamReader,
                      TransformingEventFactory transformingEventFactory) {
        this.transformingEventFactory = transformingEventFactory
        this.wrapWithApiKitStreamReader = wrapWithApiKitStreamReader
    }

    EventWrapper build(String xmlPayload,
                       EventWrapper rewriteEvent,
                       Integer httpStatus = null) {
        def messageProps = getXmlProperties(httpStatus)
        transformingEventFactory.getStreamedMuleEventWithPayload(xmlPayload,
                                                                 rewriteEvent,
                                                                 'application/xml',
                                                                 messageProps)
    }

    private static LinkedHashMap<String, String> getXmlProperties(Integer httpStatus) {
        // need some of these props for SOAP mock to work properly
        def messageProps = [:]
        if (httpStatus != null) {
            messageProps['http.status'] = httpStatus
        }
        messageProps
    }
}