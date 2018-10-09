package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

class XMLMessageBuilder {
    EventWrapper build(String xmlPayload,
                       EventWrapper rewriteEvent,
                       Integer httpStatus = null) {
        def messageProps = getXmlProperties(httpStatus)
        rewriteEvent.withNewStreamingPayload(xmlPayload,
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