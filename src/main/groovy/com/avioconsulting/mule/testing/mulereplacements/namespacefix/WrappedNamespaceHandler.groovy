package com.avioconsulting.mule.testing.mulereplacements.namespacefix

import org.mule.api.AnnotatedObject
import org.mule.api.processor.MessageProcessor
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.beans.factory.xml.NamespaceHandler
import org.springframework.beans.factory.xml.ParserContext
import org.w3c.dom.Element

import javax.xml.namespace.QName

// for some processors, like WS-Consumer, annotations, which is how we locate the connector's name
// aren't loaded
class WrappedNamespaceHandler implements NamespaceHandler {
    @Delegate
    NamespaceHandler wrapped

    @Override
    BeanDefinition parse(Element element,
                         ParserContext parserContext) {
        def beanDefinition = wrapped.parse(element, parserContext)
        if (beanDefinition instanceof RootBeanDefinition) {
            def beanKlass = beanDefinition.beanClass
            if (MessageProcessor.isAssignableFrom(beanKlass) &&
                    !AnnotatedObject.isAssignableFrom(beanKlass)) {
                def name = element.attributes.getNamedItemNS('http://www.mulesoft.org/schema/mule/documentation',
                                                             'name')
                if (name) {
                    def qname = new QName(name.namespaceURI,
                                          'name')
                    def annotations = [
                            (qname): name.nodeValue
                    ]
                    beanDefinition.propertyValues.add('annotations',
                                                      annotations)
                }
            }
        }
        beanDefinition
    }
}
