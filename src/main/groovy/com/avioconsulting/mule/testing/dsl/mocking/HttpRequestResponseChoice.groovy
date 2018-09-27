package com.avioconsulting.mule.testing.dsl.mocking

interface HttpRequestResponseChoice extends StandardRequestResponse {
    def setHttpReturnCode(Integer code)

    def httpConnectError()

    def httpTimeoutError()
}