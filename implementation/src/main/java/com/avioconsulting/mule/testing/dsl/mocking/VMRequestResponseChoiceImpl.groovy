package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.muleinterfaces.RuntimeBridgeTestSide

class VMRequestResponseChoiceImpl extends
        StandardRequestResponseImpl {
    VMRequestResponseChoiceImpl(RuntimeBridgeTestSide runtimeBridgeTestSide) {
        super(runtimeBridgeTestSide)
    }
}
