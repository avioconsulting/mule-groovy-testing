package com.avioconsulting.mule.testing.connectortest.internal;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@Extension(name = "connector-test-classpath",
        vendor = "AVIO")
@Operations(SomeOperation.class)
@ConnectionProviders(ConnProv.class)
public class Meta {
}
