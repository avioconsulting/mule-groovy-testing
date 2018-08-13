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
// aren't loaded. This lets us influence the XML->Java object transition at an early stage
// we can "plug the gaps" with missing values here
class WrappedNamespaceHandler implements NamespaceHandler {
    @Delegate
    private final NamespaceHandler wrapped

    WrappedNamespaceHandler(NamespaceHandler wrapped) {
        this.wrapped = wrapped
    }

    @Override
    BeanDefinition parse(Element element,
                         ParserContext parserContext) {
        def beanDefinition = wrapped.parse(element, parserContext)
        // connectors like WSConsumer will already have a loaded class w/ RootBeanDefinition
        // some classes won't have a bean class yet
        if (beanDefinition instanceof RootBeanDefinition && beanDefinition.hasBeanClass()) {
            checkForMissingAnnotations(beanDefinition,
                                       element)
        }
        beanDefinition
    }

    private static void checkForMissingAnnotations(RootBeanDefinition beanDefinition,
                                                   Element element) {
        def beanKlass = beanDefinition.beanClass
        // don't care if it's not a message processor
        // some classes with annotations already may end up here
        if (!MessageProcessor.isAssignableFrom(beanKlass) || AnnotatedObject.isAssignableFrom(beanKlass)) {
            return
        }
        def name = element.attributes.getNamedItemNS('http://www.mulesoft.org/schema/mule/documentation',
                                                     'name')
        if (name) {
            def qname = new QName(name.namespaceURI,
                                  'name')
            def annotations = [
                    (qname): name.nodeValue
            ]
            // This will get picked up by the MockMethodInterceptor in this library. This is part of the AnnotatedObject
            // interface
            beanDefinition.propertyValues.add('annotations',
                                              annotations)
        }
    }
}
