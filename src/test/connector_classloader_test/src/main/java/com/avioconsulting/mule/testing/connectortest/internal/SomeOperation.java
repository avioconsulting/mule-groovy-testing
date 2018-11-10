package com.avioconsulting.mule.testing.connectortest.internal;

import org.mule.runtime.extension.api.annotation.param.MediaType;

public class SomeOperation {
    @MediaType("text/plain")
    public String foo() {
        return "hello there from foo";
    }
}
