package com.avioconsulting.mule.testing

import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.construct.AbstractPipeline
import org.mule.util.NotificationUtils

import javax.xml.namespace.QName

class ProcessorLocator {
    private static final String doc = 'http://www.mulesoft.org/schema/mule/documentation'
    private final String processorName

    ProcessorLocator(String processorName) {
        this.processorName = processorName
    }

    def getProcessor(MuleEvent muleEvent) {
        // easiest way to get all processors in a flow
        // TODO: Find a public API way of doing this
        def flowMapField = AbstractPipeline.getDeclaredField('flowMap')
        flowMapField.accessible = true
        def flowMap = flowMapField.get(muleEvent.flowConstruct) as NotificationUtils.FlowMap
        def allProcessors = flowMap.flowMap.keySet()
        def processor = findProcessor(allProcessors)
        assert processor: "Unable to find processor with name '${processorName}'. Is doc:name present on the connector?"
        processor
    }

    private def findProcessor(Set<MessageProcessor> processors) {
        processors.find { processor ->
            if (!(processor instanceof AnnotatedObject)) {
                return false
            }
            def annotated = processor as AnnotatedObject
            try {
                def name = annotated.annotations[new QName(doc, 'name')]
                name == processorName
            }
            catch (NullPointerException ignored) {
                // processor chain without annotations does this
                false
            }
        }
    }
}
