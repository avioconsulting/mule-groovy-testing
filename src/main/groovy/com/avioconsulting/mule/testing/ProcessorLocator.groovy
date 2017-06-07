package com.avioconsulting.mule.testing

import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor
import org.mule.api.processor.MessageProcessorChain
import org.mule.processor.AbstractMessageProcessorOwner

import javax.xml.namespace.QName

class ProcessorLocator {
    private static final String doc = 'http://www.mulesoft.org/schema/mule/documentation'
    private final String processorName

    ProcessorLocator(String processorName) {
        this.processorName = processorName
    }

    def getProcessor(MuleEvent muleEvent) {
        def processors = muleEvent.flowConstruct.messageProcessors as List<MessageProcessor>
        def allProcessors = recursiveProcessorList(processors)
        def processor = findProcessor(allProcessors)
        assert processor: "Unable to find processor with name '${processorName}'. Is doc:name present on the connector?"
        processor
    }

    private def findProcessor(List<MessageProcessor> processors) {
        processors.find { processor ->
            def annotated = processor as AnnotatedObject
            if (!annotated) {
                return
            }
            def name = annotated.annotations[new QName(doc, 'name')]
            name == processorName
        }
    }

    private List<MessageProcessor> recursiveProcessorList(List<MessageProcessor> input) {
        input.collect { MessageProcessor processor ->
            if (processor instanceof MessageProcessorChain) {
                def processorList = processor.messageProcessors
                return recursiveProcessorList(processorList)
            }
            // if it's inside an enricher, we need to search
            if (processor instanceof AbstractMessageProcessorOwner) {
                def processorList = processor.ownedMessageProcessors
                return recursiveProcessorList(processorList)
            }
            processor
        }.flatten() as List<MessageProcessor>
    }
}
