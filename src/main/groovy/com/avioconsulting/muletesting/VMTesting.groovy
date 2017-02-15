package com.avioconsulting.muletesting

import com.avioconsulting.muletesting.transformers.JSONReceiveTransformer
import com.avioconsulting.muletesting.transformers.YieldType
import org.mule.munit.common.mocking.Attribute
import org.mule.munit.common.mocking.MessageProcessorMocker

trait VMTesting {
    def MessageProcessorMocker mockQueueReceive(String name, Class expectedRequestJsonClass, YieldType yieldType = YieldType.Map, testClosure) {
        def mock = getVmqReceive(name)
        mock.thenApply(new JSONReceiveTransformer(expectedRequestJsonClass, yieldType, testClosure))
        mock
    }

    private MessageProcessorMocker getVmqReceive(String name) {
        whenMessageProcessor('outbound-endpoint')
                .ofNamespace('vm')
                .withAttributes(Attribute.attribute('name').ofNamespace('doc').withValue(name))
    }
}
