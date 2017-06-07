package com.avioconsulting.mule.testing

import org.mule.api.AnnotatedObject
import org.mule.api.MuleEvent
import org.mule.api.processor.MessageProcessor

import javax.xml.namespace.QName

class ProcessorLocator {
    private static final String doc = 'http://www.mulesoft.org/schema/mule/documentation'
    private final String processorName

    ProcessorLocator(String processorName) {
        this.processorName = processorName
    }

    def getProcessor(MuleEvent muleEvent) {
        def processors = muleEvent.flowConstruct.messageProcessors as List<MessageProcessor>
        def processor = processors.find { processor ->
            def annotated = processor as AnnotatedObject
            if (!annotated) {
                return false
            }
            def name = annotated.annotations[new QName(doc, 'name')]
            name == processorName
        }
        assert processor: "Unable to find processor with name '${processorName}'. Is doc:name present on the connector?"
        processor
    }
}
