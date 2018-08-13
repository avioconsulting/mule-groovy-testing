package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.EventFactory
import com.avioconsulting.mule.testing.mulereplacements.MockProcess
import com.avioconsulting.mule.testing.payloadvalidators.IPayloadValidator
import org.mule.DefaultMuleMessage
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

class DsqlMock implements MockProcess {
    @Lazy
    private static Class dsqlMelParserUtilsKlass = {
        DsqlMock.classLoader.loadClass('org.mule.devkit.internal.dsql.DsqlMelParserUtils')
    }()
    private final MuleContext muleContext
    private final Closure closure
    private final IPayloadValidator payloadValidator
    private final EventFactory eventFactory

    DsqlMock(MuleContext muleContext,
             Closure closure,
             IPayloadValidator payloadValidator,
             EventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.payloadValidator = payloadValidator
        this.closure = closure
        this.muleContext = muleContext
    }
    
    MuleEvent process(MuleEvent muleEvent,
                      MessageProcessor processor) {
        assert processor.hasProperty(
                'query'): "Tried to get DSQL 'query' field from class ${processor.class} but it was not found. Most DevKit based DSQL processors have a private field with a setter only. Check the class and examine what might have changed."
        def query = processor.query
        def dsqlParserQuery = dsqlMelParserUtilsKlass.newInstance()
        def prefixedDsql = dsqlParserQuery.parseDsql(this.muleContext,
                                                     muleEvent,
                                                     query) as String
        def dSqlQuery = prefixedDsql.replace('dsql:', '')
        def result = closure(dSqlQuery)
        if (payloadValidator.payloadTypeValidationRequired) {
            payloadValidator.validatePayloadType(result)
        }
        def message = new DefaultMuleMessage(result,
                                             this.muleContext)
        eventFactory.getMuleEvent(message,
                                  muleEvent)
    }
}
