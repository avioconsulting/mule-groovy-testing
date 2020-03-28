package com.avioconsulting.mule.testing.background


import org.junit.runners.model.FrameworkField
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.TestClass

import java.lang.annotation.Annotation

class ModifiedTestClass extends TestClass {
    /**
     * Creates a {@code TestClass} wrapping {@code clazz}. Each time this
     * constructor executes, the class is scanned for annotations, which can be
     * an expensive process (we hope in future JDK's it will not be.) Therefore,
     * try to share instances of {@code TestClass} where possible.
     */
    ModifiedTestClass(Class<?> clazz) {
        super(clazz)
    }

    @Override
    protected void scanAnnotatedMembers(Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations,
                                        Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations) {
        super.scanAnnotatedMembers(methodsForAnnotations,
                                   fieldsForAnnotations)
        methodsForAnnotations.each { k, v ->
            methodsForAnnotations[k] = v.collect { fm ->
                new ProxyFrameworkMethod(fm.method)
            }
        }
    }
}
