package com.avioconsulting.mule.testing.spies

import org.mule.api.MuleEvent

interface IReceiveMuleEvents {
    def receive(MuleEvent muleEvent)
}