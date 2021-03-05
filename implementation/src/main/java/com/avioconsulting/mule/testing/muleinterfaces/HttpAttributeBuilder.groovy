package com.avioconsulting.mule.testing.muleinterfaces

trait HttpAttributeBuilder {
    def getHttpResponseAttributes(int statusCode,
                                  String reasonPhrase,
                                  ClassLoader appClassLoader,
                                  Map additionalHeaders = [:]) {
        def multiMapClass = appClassLoader.loadClass('org.mule.runtime.api.util.MultiMap')
        def getMultiMap = { Map incoming ->
            multiMapClass.newInstance(incoming)
        }
        def attrClass = appClassLoader.loadClass('org.mule.extension.http.api.HttpResponseAttributes')
//        public HttpResponseAttributes(int statusCode, String reasonPhrase, MultiMap<String, String> headers) {
//            super(headers);
//            this.statusCode = statusCode;
//            this.reasonPhrase = reasonPhrase;
//        }
        attrClass.newInstance(statusCode,
                              reasonPhrase,
                              getMultiMap(additionalHeaders))
    }

    def getHttpRequestAttributes(String httpListenerPath,
                                 String method,
                                 String path,
                                 Map queryParams,
                                 RuntimeBridgeTestSide runtimeBridge,
                                 String mimeType,
                                 String host,
                                 Map additionalHeaders = [:]) {
        def urlParts = httpListenerPath.split('/')
        assert urlParts.last() == '*': 'Expected wildcard listener!'
        urlParts = urlParts[0..-2]
        urlParts.addAll(path.split('/'))
        urlParts.removeAll { part -> part == '' }
        def url = '/' + urlParts.join('/')
        def appClassLoader = runtimeBridge.appClassloader
        def multiMapClass = appClassLoader.loadClass('org.mule.runtime.api.util.MultiMap')
        def getMultiMap = { Map incoming ->
            multiMapClass.newInstance(incoming)
        }
        queryParams = (queryParams ?: [:]).collectEntries { key, value ->
            // everything needs to be a string to mimic real HTTP listener
            [key.toString(), value.toString()]
        }
        def headers = [
                host          : host.toString(),
                // Even though content type is set on the message/payload mediatype,
                // apikit router in mule 4 depends on this
                'content-type': mimeType
        ] + additionalHeaders
        def attrBuilderClass = appClassLoader.loadClass('org.mule.extension.http.api.HttpRequestAttributesBuilder')
        attrBuilderClass.newInstance()
                .headers(getMultiMap(headers))
                .listenerPath(httpListenerPath)
                .relativePath(url)
                .version('HTTP/1.1')
                .scheme('http')
                .method(method)
                .requestPath(url)
                .requestUri(url)
                .queryParams(getMultiMap(queryParams))
        // has to be non-null
                .localAddress('/localAddress')
                .remoteAddress('/remoteAddress')
                .build()
    }
}
