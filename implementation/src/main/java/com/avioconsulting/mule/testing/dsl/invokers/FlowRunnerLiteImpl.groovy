package com.avioconsulting.mule.testing.dsl.invokers

import com.avioconsulting.mule.testing.InvokerEventFactory
import com.avioconsulting.mule.testing.TestingFrameworkException
import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.FlowWrapper
import groovy.util.logging.Log4j2
import org.apache.logging.log4j.CloseableThreadContext

// FlowRunnerLiteImpl mainly exists for our BaseApiKitTest class since it does not
// want you to customize the event before calling the flow
@Log4j2
class FlowRunnerLiteImpl implements FlowRunnerWithoutEventControl {
    protected final InvokerEventFactory invokerEventFactory
    protected final FlowWrapper flow
    protected Invoker invoker
    protected Closure closure
    protected final RuntimeBridgeTestSide runtimeBridge
    protected final String flowName
    private final CloseableThreadContext.Instance threadContext
    private Closure muleOutputEventHook = null

    FlowRunnerLiteImpl(FlowWrapper flow,
                       RuntimeBridgeTestSide runtimeBridge,
                       String flowName) {
        this.invokerEventFactory = runtimeBridge
        this.flow = flow
        this.runtimeBridge = runtimeBridge
        this.flowName = flowName
        threadContext = CloseableThreadContext.push('Flow invocation')
        this.threadContext.put('flowInvocation',
                               flowName)
    }

    def json(@DelegatesTo(JsonInvoker) Closure closure) {
        def jsonInvoker = new JsonInvokerImpl(invokerEventFactory,
                                              flow)
        invoker = jsonInvoker
        this.closure = closure
    }

    def java(@DelegatesTo(JavaInvoker) Closure closure) {
        def javaInvoker = new JavaInvokerImpl(invokerEventFactory,
                                              flowName)
        invoker = javaInvoker
        this.closure = closure
    }

    @Override
    def soap(@DelegatesTo(SoapInvoker) Closure closure) {
        def soapInvoker = new SoapOperationFlowInvokerImpl(invokerEventFactory,
                                                           runtimeBridge,
                                                           flowName)
        invoker = soapInvoker
        this.closure = closure
    }

    EventWrapper getEvent() {
        assert invoker: 'Need to specify a proper format! (e.g. json)'
        log.info 'Creating event to invoke flow'
        def code = closure.rehydrate(invoker,
                                     this,
                                     this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        invoker.getEvent()
    }

    def transformOutput(EventWrapper event) {
        def response = null
        // filters return null events
        if (event != null) {
            response = invoker.transformOutput(event)
        }
        if (muleOutputEventHook) {
            muleOutputEventHook(event)
        }
        return response
    }

    def closeLogContext() {
        log.info 'Completed flow execution'
        threadContext.close()
    }

    def withOutputEvent(Closure closure) {
        muleOutputEventHook = closure
    }

    def withOutputHttpStatus(Closure closure) {
        withOutputEvent { EventWrapper outputEvent ->
            if (outputEvent == null) {
                throw new TestingFrameworkException(
                        'A null event was returned (filter?) so No HTTP status was returned from your flow. With the real flow, an HTTP status of 200 will usually be set by default so this test is usually not required.')
            }
            def status = outputEvent.getVariable('httpStatus')?.value as Integer
            if (!status) {
                throw new TestingFrameworkException('No HTTP status was returned from your flow in the httpStatus variable. Did you forget?')
            }
            closure(status)
        }
    }
}
