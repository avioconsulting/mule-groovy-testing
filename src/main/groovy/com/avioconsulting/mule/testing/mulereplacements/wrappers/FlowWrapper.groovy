package com.avioconsulting.mule.testing.mulereplacements.wrappers

import javax.xml.namespace.QName

class FlowWrapper extends
        ConnectorInfo {
    String name
    private final Object nativeMuleObject

    // TODO: Better design
    FlowWrapper(String name,
                Object nativeMuleObject) {
        super(getComponentLocation(nativeMuleObject, 'fileName') as String,
              getComponentLocation(nativeMuleObject, 'lineInFile') as Integer,
              [:])
        this.nativeMuleObject = nativeMuleObject
        this.name = name
    }

    private static Object getComponentLocation(Object nativeMuleObject,
                                               String field) {
        def componentLocation = nativeMuleObject.annotations[new QName('mule',
                                                                       'COMPONENT_LOCATION')]
        componentLocation[field].get()
    }

    EventWrapper process(EventWrapper input) {
        assert input instanceof EventWrapperImpl
        def muleEvent = nativeMuleObject.process(input.nativeMuleEvent)
        new EventWrapperImpl(muleEvent)
    }
}
