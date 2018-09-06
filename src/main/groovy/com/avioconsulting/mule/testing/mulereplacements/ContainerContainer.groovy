package com.avioconsulting.mule.testing.mulereplacements

import groovy.transform.Canonical
import org.mule.runtime.module.launcher.MuleContainer

@Canonical
class ContainerContainer {
    Object registry
    MuleContainer muleContainer
}
