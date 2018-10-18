package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.mulereplacements.wrappers.EventWrapper

interface Invoker {
    EventWrapper getEvent()

    def transformOutput(EventWrapper event)
}