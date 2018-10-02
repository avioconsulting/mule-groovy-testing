package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ReturnWrapper

interface MessageFactory {
    MessageWrapper buildMessage(Object payload)
    MessageWrapper buildMessage(ReturnWrapper returnWrapper)
    MessageWrapper withNewAttributes(MessageWrapper existingMessage,
                                     Map attributes)
}
