package com.avioconsulting.mule.testing.dsl.mocking.sfdc

interface Choice {
    def upsert(@DelegatesTo(UpsertResponseUtil) Closure closure)

    def query(Closure closure)
}