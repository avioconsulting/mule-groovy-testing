package com.avioconsulting.mule.testing.transformers.sfdc

import com.avioconsulting.mule.testing.ProcessorLocator
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.api.MuleMessage
import org.mule.devkit.internal.dsql.DsqlMelParserUtils
import org.mule.modules.interceptor.processors.MuleMessageTransformer
import org.mule.modules.salesforce.generated.processors.QueryMessageProcessorDebuggable
import org.mule.munit.common.mocking.SpyProcess

class QueryTransformerSpy implements MuleMessageTransformer, SpyProcess {
    private final ProcessorLocator locator
    private final MuleContext muleContext
    private String dsqlQuery
    private final Closure closure

    QueryTransformerSpy(ProcessorLocator locator,
                        MuleContext muleContext,
                        Closure closure) {
        this.closure = closure
        this.muleContext = muleContext
        this.locator = locator
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def result = closure(this.dsqlQuery)
        if (!(result instanceof List)) {
            throw new Exception("Must return a List<Map> result from your mock instead of ${result} which is of type ${result.class}!")
        }
        if (result.any() && !(result[0] instanceof Map)) {
            def item = result[0]
            throw new Exception("Must return a List<Map> result from your mock instead of List<${item.class}>!")
        }
        new DefaultMuleMessage(result,
                               this.muleContext)
    }

    void spy(MuleEvent muleEvent) throws MuleException {
        def processor = locator.getProcessor(muleEvent) as QueryMessageProcessorDebuggable
        def dsqlParserQuery = new DsqlMelParserUtils()
        def prefixedDsql = dsqlParserQuery.parseDsql(this.muleContext,
                                                     muleEvent,
                                                     processor.query) as String
        this.dsqlQuery = prefixedDsql.replace('dsql:', '')
    }
}
