package com.avioconsulting.mule.testing.transformers.http

import groovy.transform.Canonical

@Canonical
class HttpState {
    int httpStatus
    Object body
    String mediaType
}
