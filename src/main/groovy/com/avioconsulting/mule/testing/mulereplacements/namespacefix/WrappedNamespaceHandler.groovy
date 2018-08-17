package com.avioconsulting.mule.testing.mulereplacements.namespacefix

import com.avioconsulting.mule.testing.mulereplacements.MockMethodInterceptor
import com.avioconsulting.mule.testing.mulereplacements.MockingConfiguration
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.xml.NamespaceHandler
import org.springframework.beans.factory.xml.ParserContext
import org.w3c.dom.Element

// for some processors, like WS-Consumer, annotations, which is how we locate the connector's name
// aren't loaded. This lets us influence the XML->Java object transition at an early stage
// we can "plug the gaps" with missing values here
class WrappedNamespaceHandler implements NamespaceHandler {
    static final String ANNOTATION_NAME_ATTRIBUTE = 'MULE_GROOVY_TESTING_ANNOTATION_NAME'
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
        if (beanDefinition && !beanDefinition.propertyValues.contains('annotations')) {
            checkForMissingAnnotations(beanDefinition,
                                       element)
        }
        beanDefinition
    }

    private static void checkForMissingAnnotations(BeanDefinition beanDefinition,
                                                   Element element) {
        def nameQname = MockingConfiguration.processorName
        def name = element.attributes.getNamedItemNS(nameQname.namespaceURI,
                                                     nameQname.localPart)
        if (name) {
            beanDefinition.setAttribute(ANNOTATION_NAME_ATTRIBUTE, name.nodeValue)
        }
    }
}
