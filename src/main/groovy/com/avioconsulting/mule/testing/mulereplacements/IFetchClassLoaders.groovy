package com.avioconsulting.mule.testing.mulereplacements

interface IFetchClassLoaders {
    ClassLoader getAppClassloader()

    ClassLoader getRuntimeClassLoader()
}
