package com.avioconsulting.mule.testing.muleinterfaces

class FetchClassLoaders implements IFetchClassLoaders {
    private final Object runtimeBridgeMuleSide

    FetchClassLoaders(Object runtimeBridgeMuleSide) {
        this.runtimeBridgeMuleSide = runtimeBridgeMuleSide
    }

    ClassLoader getAppClassloader() {
        runtimeBridgeMuleSide.getAppClassloader()
    }

    @Override
    ClassLoader getRuntimeClassLoader() {
        runtimeBridgeMuleSide.getRuntimeClassLoader()
    }
}
