package com.avioconsulting.mule.testing.mulereplacements

import org.mule.api.MuleContext
import org.mule.config.ConfigResource
import org.mule.config.spring.MuleArtifactContext
import org.mule.config.spring.OptionalObjectsController
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory

class GroovyTestingArtifactContext extends MuleArtifactContext {
    GroovyTestingArtifactContext(MuleContext muleContext,
                                 ConfigResource[] configResources,
                                 OptionalObjectsController optionalObjectsController) throws BeansException {
        super(muleContext,
              configResources,
              optionalObjectsController)
    }

    @Override
    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.prepareBeanFactory(beanFactory)
        beanFactory.addBeanPostProcessor(new ConnectorReplacerProcessor())
    }
}
