package com.avioconsulting.mule.testing.spies

import com.avioconsulting.mule.testing.ProcessorLocator
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.devkit.internal.dsql.DsqlMelParserUtils
import org.mule.munit.common.mocking.SpyProcess

abstract class DsqlBasedSpy implements SpyProcess {
    private final ProcessorLocator locator
    protected final MuleContext muleContext
    private String dSqlQuery

    DsqlBasedSpy(ProcessorLocator locator,
                 MuleContext muleContext) {
        this.locator = locator
        this.muleContext = muleContext
    }

    protected String getdSqlQuery() {
        assert dSqlQuery: 'Expected query spy to have already run!'
        this.dSqlQuery
    }

    void spy(MuleEvent muleEvent) throws MuleException {
        def processor = locator.getProcessor(muleEvent)
        // not a public field unfortunately
        assert processor.hasProperty(
                'query'): "Tried to get DSQL 'query' field from class ${processor.class} but it was not found. Most DevKit based DSQL processors have a private field with a setter only. Check the class and examine what might have changed."
        def query = processor.query
        def dsqlParserQuery = new DsqlMelParserUtils()
        def prefixedDsql = dsqlParserQuery.parseDsql(this.muleContext,
                                                     muleEvent,
                                                     query) as String
        this.dSqlQuery = prefixedDsql.replace('dsql:', '')
    }
}
