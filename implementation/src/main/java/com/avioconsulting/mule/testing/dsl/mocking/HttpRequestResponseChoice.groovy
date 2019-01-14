package com.avioconsulting.mule.testing.dsl.mocking

interface HttpRequestResponseChoice extends
        StandardRequestResponse {
    // TODO: All of these methods belong in closure context (see XMLFormatterImpl)
    @Deprecated()
    def setHttpReturnCode(Integer code)

    @Deprecated()
    def httpConnectError()

    @Deprecated()
    def httpTimeoutError()
}
