package com.avioconsulting.mule.testing.dsl.mocking

interface HttpRequestResponseChoice extends StandardRequestResponse {
    def withHttpOptions(Closure closure)

    def setHttpReturnCode(Integer code)

    def disableContentTypeCheck()
}