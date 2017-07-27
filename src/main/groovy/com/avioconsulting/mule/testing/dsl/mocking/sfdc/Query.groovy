package com.avioconsulting.mule.testing.dsl.mocking.sfdc

interface Query {
    def withQuery(Closure closure)
}
