package com.avioconsulting.mule.testing.muleinterfaces

interface IFetchClassLoaders {
    ClassLoader getAppClassloader()

    ClassLoader getRuntimeClassLoader()
}
