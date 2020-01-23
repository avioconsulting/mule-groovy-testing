package com.avioconsulting.mule.testing.muleinterfaces

class LookupFromRegistryWithMuleBridge implements ILookupFromRegistry {
    private final Object runtimeBridgeMuleSide

    LookupFromRegistryWithMuleBridge(Object runtimeBridgeMuleSide) {

        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
    }

    @Override
    Optional<Object> lookupByName(String name) {
        runtimeBridgeMuleSide.lookupByName(name)
    }
}
