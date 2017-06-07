package com.avioconsulting.mule.testing.dsl.mocking

class QueryParamOptionsImpl implements QueryParamOptions {
    Integer httpReturnCode

    def setHttpReturnCode(Integer code) {
        this.httpReturnCode = code
    }
}
