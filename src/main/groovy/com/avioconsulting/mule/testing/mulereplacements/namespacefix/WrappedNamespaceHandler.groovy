package com.avioconsulting.mule.testing.mulereplacements.namespacefix

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.beans.factory.xml.NamespaceHandler
import org.springframework.beans.factory.xml.ParserContext
import org.w3c.dom.Element

// for some processors, like WS-Consumer, annotations, which is how we locate the connector's name
// aren't loaded
class WrappedNamespaceHandler implements NamespaceHandler {
    @Delegate
    NamespaceHandler wrapped

    @Override
    BeanDefinition parse(Element element,
                         ParserContext parserContext) {
        def wrapped = wrapped.parse(element, parserContext)
        if (element.tagName == 'ws:consumer') {
            assert wrapped instanceof RootBeanDefinition
            wrapped.propertyValues.add('annotations',
                                       [:])

            println 'gound it!'
        }
        wrapped
    }
}
