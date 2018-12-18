package com.avioconsulting.mule.testing.dsl.invokers

interface JavaInvoker {
    def inputPayload(inputObject)

    def inputPayload(inputObject,
                     String mediaType)
}
