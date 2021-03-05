package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

interface Invoker {
    /**
     * gets the initial event to invoke the flow with
     * @return
     */
    EventWrapper getEvent()

    /**
     * Takes the output event from Mulesoft and converts it back to a "friendly" format
     * depending on the invoker
     * @param event
     * @return
     */
    def transformOutput(EventWrapper event)
}
