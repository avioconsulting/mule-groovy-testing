package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

abstract class XMLTransformer<T extends ConnectorInfo> implements IHaveStateToReset {
    protected final XMLMessageBuilder xmlMessageBuilder

    XMLTransformer() {
        this.xmlMessageBuilder = new XMLMessageBuilder()
    }
}
