package com.avioconsulting.mule.testing.dsl.mocking.formats

import org.mule.modules.interceptor.processors.MuleMessageTransformer

interface ISelectPrimaryTransformer {
    MuleMessageTransformer getTransformer()
}