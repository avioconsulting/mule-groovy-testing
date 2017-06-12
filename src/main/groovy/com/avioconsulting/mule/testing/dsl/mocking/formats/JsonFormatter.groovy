package com.avioconsulting.mule.testing.dsl.mocking.formats

import com.avioconsulting.mule.testing.dsl.mocking.QueryParamOptions

interface JsonFormatter {
    def whenCalledWithQueryParams(@DelegatesTo(QueryParamOptions) Closure closure)

    def whenCalledWith(Closure closure)

    def whenCalledWith(Class inputClass,
                       Closure closure)
}
