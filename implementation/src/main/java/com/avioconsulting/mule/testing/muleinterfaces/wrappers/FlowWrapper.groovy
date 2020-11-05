package com.avioconsulting.mule.testing.muleinterfaces.wrappers

import com.avioconsulting.mule.testing.muleinterfaces.FetchClassLoaders

import javax.xml.namespace.QName

class FlowWrapper extends
        ConnectorInfo {
    private final Object nativeMuleObject
    private final Object runtimeBridgeMuleSide
    private static final QName componentId = new QName('config',
                                                       'componentIdentifier')

    FlowWrapper(String name,
                Object nativeMuleObject,
                Object runtimeBridgeMuleSide) {
        super(getComponentLocationField(nativeMuleObject,
                                        'fileName') as String,
              getComponentLocationField(nativeMuleObject,
                                        'lineInFile') as Integer,
              'n/a',
              [:],
              new FetchClassLoaders(runtimeBridgeMuleSide))
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
        nativeMuleObject.location
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
        def configurationOptional = processor.getStaticConfiguration() as Optional
        assert configurationOptional.isPresent(): 'Expected config to exist!'
        // LifecycleAwareConfigurationInstance
        def lifecyleAwareConfig = configurationOptional.get()
        lifecyleAwareConfig.getValue()
    }

    def start() {
        nativeMuleObject.start()
    }

    def stop() {
        nativeMuleObject.stop()
    }
}
