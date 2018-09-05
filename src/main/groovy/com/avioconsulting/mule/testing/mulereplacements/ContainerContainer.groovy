package com.avioconsulting.mule.testing.mulereplacements

import groovy.transform.Canonical
import org.mule.runtime.api.artifact.Registry
import org.mule.runtime.module.launcher.MuleContainer

@Canonical
class ContainerContainer {
    Registry registry
    MuleContainer muleContainer
}
