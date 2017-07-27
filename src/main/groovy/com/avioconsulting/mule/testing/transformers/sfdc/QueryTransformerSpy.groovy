package com.avioconsulting.mule.testing.transformers.sfdc

import com.avioconsulting.mule.testing.ProcessorLocator
import com.avioconsulting.mule.testing.spies.DsqlBasedSpy
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleMessage
import org.mule.modules.interceptor.processors.MuleMessageTransformer

class QueryTransformerSpy extends DsqlBasedSpy implements MuleMessageTransformer {
    private final Closure closure

    QueryTransformerSpy(ProcessorLocator locator,
                        MuleContext muleContext,
                        Closure closure) {
        super(locator, muleContext)
        this.closure = closure
    }

    MuleMessage transform(MuleMessage muleMessage) {
        def result = closure(this.dSqlQuery)
        validateMockReturn(result)
        new DefaultMuleMessage(result,
                               this.muleContext)
    }

    private static void validateMockReturn(Object result) {
        if (!(result instanceof List)) {
            throw new Exception(
                    "Must return a List<Map> result from your mock instead of ${result} which is of type ${result.class}!")
        }
        if (result.any() && !(result[0] instanceof Map)) {
            def item = result[0]
            throw new Exception("Must return a List<Map> result from your mock instead of List<${item.class}>!")
        }
    }
}
