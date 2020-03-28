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
        def attrClass = appClassLoader.loadClass('org.mule.extension.http.api.HttpRequestAttributes')
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
        // public HttpRequestAttributes(MultiMap<String, String> headers, String listenerPath, String relativePath, String version, String scheme, String method, String requestPath, String requestUri, String queryString, MultiMap<String, String> queryParams, Map<String, String> uriParams, String remoteAddress, Certificate clientCertificate) {
        //        this(headers, listenerPath, relativePath, version, scheme, method, requestPath, requestUri, queryString, queryParams, uriParams, "", remoteAddress, clientCertificate);
        //    }
        attrClass.newInstance(getMultiMap(headers),
                              httpListenerPath,
                              url,
                              'HTTP/1.1',
                              'http',
                              method,
                              url,
                              url,
                              '',
                              // query string
                              getMultiMap(queryParams),
                              [:],
                              // uri params
                              '/remoteaddress',
                              null)
    }
}