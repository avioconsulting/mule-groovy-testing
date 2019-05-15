package com.avioconsulting.mule.testing.dsl.mocking

interface ErrorThrowing {
    def setHttpStatusCode(int code)

    def httpConnectError()

    def httpTimeoutError()
}
