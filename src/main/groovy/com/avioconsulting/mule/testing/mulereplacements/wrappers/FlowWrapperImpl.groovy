package com.avioconsulting.mule.testing.mulereplacements.wrappers

class FlowWrapperImpl implements FlowWrapper {
    String name
    private final Object nativeMuleObject

    // TODO: Better design
    FlowWrapperImpl(String name,
                    Object nativeMuleObject) {
        this.nativeMuleObject = nativeMuleObject
        this.name = name
    }

    @Override
    EventWrapper process(EventWrapper input) {
        assert input instanceof EventWrapperImpl
        def muleEvent = nativeMuleObject.process(input.nativeMuleEvent)
        new EventWrapperImpl(muleEvent)
    }
}
