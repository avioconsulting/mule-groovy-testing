package com.avioconsulting.mule.testing.muleinterfaces

interface ILookupFromRegistry {
    Optional<Object> lookupByName(String name)
}
