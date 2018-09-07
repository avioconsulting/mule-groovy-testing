package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface FlowWrapper {
    String getName()
    EventWrapper process(EventWrapper input)
}
