package com.avioconsulting.mule.testing.mulereplacements

import net.sf.cglib.proxy.Enhancer
import org.mule.AbstractAnnotatedObject
import org.mule.api.MuleContext
import org.mule.api.MuleEvent
import org.mule.api.MuleException
import org.mule.api.context.MuleContextAware
import org.mule.api.lifecycle.Initialisable
import org.mule.api.lifecycle.InitialisationException
import org.mule.api.processor.MessageProcessor
import org.mule.construct.Flow
import org.springframework.beans.factory.FactoryBean

class MockableFlowRefFactoryBean extends AbstractAnnotatedObject implements FactoryBean<MessageProcessor>, MuleContextAware, Initialisable {
    private final MockingConfiguration mockingConfiguration
    private MuleContext muleContext
    private MessageProcessor messageProcessor

    String name

    MockableFlowRefFactoryBean(MockingConfiguration mockingConfiguration) {
        this.mockingConfiguration = mockingConfiguration
    }

    @Override
    void setMuleContext(MuleContext context) {
        this.muleContext = context
    }

    @Override
    void initialise() throws InitialisationException {
        def flowRef = Enhancer.create(FlowReference,
                                      new MockMethodInterceptor(this.mockingConfiguration,
                                                                null)) as FlowReference
        flowRef.flow = muleContext.registry.lookupFlowConstruct(this.name) as Flow
        this.messageProcessor = flowRef
    }

    class FlowReference extends AbstractAnnotatedObject implements MessageProcessor {
        Flow flow

        @Override
        MuleEvent process(MuleEvent event) throws MuleException {
            flow.process(event)
        }
    }

    @Override
    MessageProcessor getObject() throws Exception {
        this.messageProcessor
    }

    @Override
    Class<?> getObjectType() {
        MessageProcessor
    }

    @Override
    boolean isSingleton() {
        return false
    }
}
