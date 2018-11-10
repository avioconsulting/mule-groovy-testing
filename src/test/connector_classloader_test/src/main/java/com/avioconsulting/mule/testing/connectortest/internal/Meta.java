package com.avioconsulting.mule.testing.connectortest.internal;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;

@Extension(name = "connector-test-classpath",
        vendor = "AVIO")
@Operations(SomeOperation.class)
public class Meta {
}
