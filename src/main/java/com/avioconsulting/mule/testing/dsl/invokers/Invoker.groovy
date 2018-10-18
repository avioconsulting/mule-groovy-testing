package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

interface Invoker {
    EventWrapper getEvent()

    def transformOutput(EventWrapper event)
}