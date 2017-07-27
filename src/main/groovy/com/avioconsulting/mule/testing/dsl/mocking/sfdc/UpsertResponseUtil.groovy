package com.avioconsulting.mule.testing.dsl.mocking.sfdc

import com.sforce.soap.partner.UpsertResult
import org.mule.modules.salesforce.bulk.EnrichedUpsertResult

class UpsertResponseUtil {
    List<EnrichedUpsertResult> successfulUpsertResult() {
        successfulUpsertResults(1)
    }

    List<EnrichedUpsertResult> successfulUpsertResults(int number) {
        (1..number).collect { index ->
            def wrapped = new UpsertResult()
            wrapped.created = true
            wrapped.success = true
            new EnrichedUpsertResult(wrapped)
        }
    }
}
