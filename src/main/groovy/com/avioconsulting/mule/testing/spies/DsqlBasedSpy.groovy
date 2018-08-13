package com.avioconsulting.mule.testing.spies


import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import com.avioconsulting.mule.testing.mulereplacements.SpyProcess

abstract class DsqlBasedSpy implements SpyProcess {
    @Lazy
    private static Class dsqlMelParserUtilsKlass = {
        DsqlBasedSpy.classLoader.loadClass('org.mule.devkit.internal.dsql.DsqlMelParserUtils')
    }()

    protected final MuleContext muleContext
    private String dSqlQuery

    DsqlBasedSpy(MuleContext muleContext) {
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
        def dsqlParserQuery = dsqlMelParserUtilsKlass.newInstance()
        def prefixedDsql = dsqlParserQuery.parseDsql(this.muleContext,
                                                     muleEvent,
                                                     query) as String
        this.dSqlQuery = prefixedDsql.replace('dsql:', '')
    }
}
