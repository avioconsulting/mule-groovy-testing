package com.avioconsulting.mule.testing.dsl.invokers

import org.mule.api.MuleEvent

interface Invoker {
    MuleEvent getEvent()
    def transformOutput(MuleEvent event)
}