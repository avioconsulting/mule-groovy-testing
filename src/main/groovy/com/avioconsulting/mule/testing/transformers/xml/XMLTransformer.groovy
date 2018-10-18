package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo

abstract class XMLTransformer<T extends ConnectorInfo> {
    protected final XMLMessageBuilder xmlMessageBuilder

    XMLTransformer() {
        this.xmlMessageBuilder = new XMLMessageBuilder()
    }
}
