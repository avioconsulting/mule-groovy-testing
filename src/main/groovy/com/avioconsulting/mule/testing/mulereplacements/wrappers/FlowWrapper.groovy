package com.avioconsulting.mule.testing.mulereplacements.wrappers

import javax.xml.namespace.QName

class FlowWrapper extends
        ConnectorInfo {
    String name
    private final Object nativeMuleObject

    // TODO: Better design
    FlowWrapper(String name,
                Object nativeMuleObject) {
        super(getComponentLocationField(nativeMuleObject, 'fileName') as String,
              getComponentLocationField(nativeMuleObject, 'lineInFile') as Integer,
              [:])
        this.nativeMuleObject = nativeMuleObject
        this.name = name
    }

    private static Object getComponentLocationField(Object nativeMuleObject,
                                                    String field) {
        def componentLocation = getComponentLocation(nativeMuleObject)
        componentLocation[field].get()
    }

    private static Object getComponentLocation(Object nativeMuleObject) {
        nativeMuleObject.annotations[new QName('mule',
                                               'COMPONENT_LOCATION')]
    }

    EventWrapper process(EventWrapper input) {
        assert input instanceof EventWrapperImpl
        def muleEvent = nativeMuleObject.process(input.nativeMuleEvent)
        new EventWrapperImpl(muleEvent)
    }
}
