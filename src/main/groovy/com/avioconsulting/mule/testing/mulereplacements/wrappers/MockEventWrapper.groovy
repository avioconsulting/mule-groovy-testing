package com.avioconsulting.mule.testing.mulereplacements.wrappers

interface MockEventWrapper extends EventWrapper {
    void changeMessage(MessageWrapper messageWrapper)
}
