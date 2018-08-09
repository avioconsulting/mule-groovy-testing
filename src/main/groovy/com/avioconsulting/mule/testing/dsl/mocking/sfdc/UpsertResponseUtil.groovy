package com.avioconsulting.mule.testing.dsl.mocking.sfdc

class UpsertResponseUtil {
    @Lazy
    private static Class upsertResultKlass = {
       UpsertResponseUtil.classLoader.loadClass('com.sforce.soap.partner.UpsertResult')
    }()

    @Lazy
    static Class enrichedUpsertResultKlass = {
        UpsertResponseUtil.classLoader.loadClass('org.mule.modules.salesforce.bulk.EnrichedUpsertResult')
    }()

    List successful(boolean created = true) {
        successful(1, created)
    }

    List successful(int number,
                    boolean created = true) {
        (1..number).collect { index ->
            def wrapped = upsertResultKlass.newInstance()
            wrapped.created = created
            wrapped.success = true
            enrichedUpsertResultKlass.newInstance(wrapped)
        }
    }

    List failed(Error... errormessages) {
        failed(1,
               errormessages)
    }

    List failed(int number,
                Error... errormessages) {
        (1..number).collect { index ->
            def wrapped = upsertResultKlass.newInstance()
            wrapped.created = false
            wrapped.success = false
            wrapped.errors = errormessages
            enrichedUpsertResultKlass.newInstance(wrapped)
        }
    }
}
