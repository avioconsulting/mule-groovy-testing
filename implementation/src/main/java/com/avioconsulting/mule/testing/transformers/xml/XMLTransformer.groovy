package com.avioconsulting.mule.testing.transformers.xml

import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.IHaveStateToReset

abstract class XMLTransformer<T extends ConnectorInfo> implements IHaveStateToReset {
    protected final XMLMessageBuilder xmlMessageBuilder
    // TODO: Refactor our error code to be evaluated with a closure context such that you can call httpConnectError() in the deepest closure and have the closure "reverse curried" to add the event and connector to it. then our error transformers can throw directly. See XMLFormatterImpl
    private boolean impendingFault

    XMLTransformer() {
        this.xmlMessageBuilder = new XMLMessageBuilder()
        reset()
    }

    def notifyImpendingFault() {
        impendingFault = true
    }

    protected boolean isImpendingFault() {
        this.impendingFault
    }

    @Override
    def reset() {
        impendingFault = false
    }
}
