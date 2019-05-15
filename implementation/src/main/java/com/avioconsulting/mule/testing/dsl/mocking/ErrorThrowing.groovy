package com.avioconsulting.mule.testing.dsl.mocking

interface ErrorThrowing {
    def setHttpReturnCode(Integer code)

    def httpConnectError()

    def httpTimeoutError()
}
