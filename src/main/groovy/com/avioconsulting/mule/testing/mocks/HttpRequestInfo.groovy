package com.avioconsulting.mule.testing.mocks

import groovy.transform.Immutable

@Immutable
class HttpRequestInfo {
    String httpVerb, uri
    Map queryParams, headers
}
