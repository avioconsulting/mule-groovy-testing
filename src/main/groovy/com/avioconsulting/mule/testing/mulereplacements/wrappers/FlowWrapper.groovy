package com.avioconsulting.mule.testing.mulereplacements.wrappers

import javax.xml.namespace.QName
import java.util.concurrent.atomic.AtomicReference

class FlowWrapper extends
        ConnectorInfo {
    String name
    private final Object nativeMuleObject
    private final Object runtimeBridgeMuleSide
    private static final QName componentId = new QName('config', 'componentIdentifier')

    // TODO: Better design
    FlowWrapper(String name,
                Object nativeMuleObject,
                Object runtimeBridgeMuleSide) {
        super(getComponentLocationField(nativeMuleObject, 'fileName') as String,
              getComponentLocationField(nativeMuleObject, 'lineInFile') as Integer,
              [:])
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
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
        new EventWrapperImpl(muleEvent,
                             runtimeBridgeMuleSide)
    }

    Object getConfigurationInstance(String processorComponentIdentifier) {
        def processors = nativeMuleObject.processors as List
        def processor = processors.find { p ->
            def id = p.annotations[componentId]
            id.toString() == processorComponentIdentifier
        }
        assert processor: "Was unable to find processor ${processorComponentIdentifier} in ${processors}"
        def configProviderReference = processor.configurationProvider as AtomicReference
        def configProvider = configProviderReference.get()
    }
}
