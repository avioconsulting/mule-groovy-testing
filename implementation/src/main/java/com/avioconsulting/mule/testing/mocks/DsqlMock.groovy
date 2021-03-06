package com.avioconsulting.mule.testing.mocks

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.muleinterfaces.MuleMessageTransformer
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper

class DsqlMock<T extends ConnectorInfo> implements
        MuleMessageTransformer<T> {
    @Lazy
    private static Class dsqlMelParserUtilsKlass = {
        DsqlMock.classLoader.loadClass('org.mule.devkit.internal.dsql.DsqlMelParserUtils')
    }()
    private final Closure closure
    private final InvokerEventFactory eventFactory

    DsqlMock(Closure closure,
             InvokerEventFactory eventFactory) {
        this.eventFactory = eventFactory
        this.closure = closure
    }

    EventWrapper transform(EventWrapper muleEvent,
                           T connectorInfo) {
        assert processor.hasProperty(
                'query'): "Tried to get DSQL 'query' field from class ${processor.class} but it was not found. Most DevKit based DSQL processors have a private field with a setter only. Check the class and examine what might have changed."
        def query = processor.query
        def dsqlParserQuery = dsqlMelParserUtilsKlass.newInstance()
        // only way to parse DSQL is with this
        def prefixedDsql = dsqlParserQuery.parseDsql(this.muleContext,
                                                     muleEvent,
                                                     query) as String
        def dSqlQuery = prefixedDsql.replace('dsql:',
                                             '')
        def result = closure(dSqlQuery)
        if (payloadValidator.isPayloadTypeValidationRequired(processor)) {
            payloadValidator.validatePayloadType(result)
        }
        eventFactory.getMuleEventWithPayload(result,
                                             muleEvent)
    }
}
