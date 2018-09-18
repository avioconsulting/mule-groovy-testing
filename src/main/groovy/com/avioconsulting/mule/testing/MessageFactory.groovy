package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapper

interface MessageFactory {
    MessageWrapper buildMessage(Object payload)
}
