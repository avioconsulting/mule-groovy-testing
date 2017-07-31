package com.avioconsulting.mule.testing.dsl.mocking.sfdc

import com.sforce.soap.partner.Error
import com.sforce.soap.partner.UpsertResult
import org.mule.modules.salesforce.bulk.EnrichedUpsertResult

class UpsertResponseUtil {
    List<EnrichedUpsertResult> successful(boolean created = true) {
        successful(1, created)
    }

    List<EnrichedUpsertResult> successful(int number,
                                          boolean created = true) {
        (1..number).collect { index ->
            def wrapped = new UpsertResult()
            wrapped.created = created
            wrapped.success = true
            new EnrichedUpsertResult(wrapped)
        }
    }

    List<EnrichedUpsertResult> failed(Error... errormessages) {
        failed(1,
               errormessages)
    }

    List<EnrichedUpsertResult> failed(int number,
                                      Error... errormessages) {
        (1..number).collect { index ->
            def wrapped = new UpsertResult()
            wrapped.created = false
            wrapped.success = false
            wrapped.errors = errormessages
            new EnrichedUpsertResult(wrapped)
        }
    }
}
