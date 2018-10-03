package com.avioconsulting.mule.testing.mulereplacements

interface IFetchAppClassLoader {
    ClassLoader getAppClassloader()
    def lookupErrorType(id,
                        String errorType)
}
