package com.avioconsulting.mule.testing

import org.mule.api.transformer.TransformerException
import org.mule.transformer.AbstractTransformer

class TransformerDebugger extends AbstractTransformer {
    protected Object doTransform(Object o, String s) throws TransformerException {
        println "Object is ${o} and of type ${o.class}"
        return o
    }
}
