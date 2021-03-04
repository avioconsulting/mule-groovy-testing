package com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors

import com.avioconsulting.mule.testing.dsl.mocking.ErrorThrowing
import com.avioconsulting.mule.testing.muleinterfaces.HttpAttributeBuilder
import com.avioconsulting.mule.testing.muleinterfaces.IFetchClassLoaders
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.http.HttpClosureEvalResponse

class HttpRequesterInfo extends
        ConnectorInfo<HttpClosureEvalResponse> implements HttpFunctionality {
    private final String method
    private final Map<String, String> queryParams
    private final Map<String, String> headers
    private HttpValidatorWrapper validatorWrapper
    private final String uri
    private final body
    private final String mimeType
    private final ClassLoader appClassLoader

    // don't make HttpAttributeBuilder publicly visible
    private class InnerHttp implements HttpAttributeBuilder {
    }

    HttpRequesterInfo(String fileName,
                      Integer lineNumber,
                      String container,
                      Map<String, Object> parameters,
                      IFetchClassLoaders fetchClassLoaders) {
        super(fileName,
              lineNumber,
              container,
              parameters,
              fetchClassLoaders)
        this.method = parameters['method'] as String
        def responseValidationSettings = parameters['responseValidationSettings']
        if (!responseValidationSettings) {
            // At the moment, there doesn't seem to be a way to get the actual DW problem that leads to this
            throw new Exception('Expected responseValidationSettings')
        }
        def muleValidator = responseValidationSettings.responseValidator
        appClassLoader = fetchClassLoaders.appClassloader
        if (!muleValidator) {
            // Even if you choose 'None' for response validator in Studio 7, Mule will still validate against 200,201 by default
            // but if none is picked, we won't see a validator
            // so we just build one using the app's classloader
            muleValidator = getValidator(appClassLoader)
        }
        // it's a MultiMap, keep Mule runtime classes away from our tests
        def requestBuilder = parameters['requestBuilder']
        this.body = requestBuilder.body
        this.queryParams = convertMultiMap(requestBuilder.queryParams) as Map<String, String>
        this.headers = convertMultiMap(requestBuilder.headers) as Map<String, String>
        this.uri = requestBuilder.replaceUriParams(parameters['uriSettings'].path)
        this.validatorWrapper = new HttpValidatorWrapper(muleValidator,
                                                         this)
        this.mimeType = parameters['outputMimeType']
    }

    private static Map convertMultiMap(Map map) {
        map.collectEntries { key, value ->
            [key, value instanceof Map ? convertMultiMap(value) : value]
        }
    }

    String getMethod() {
        this.method
    }

    String getMimeType() {
        this.mimeType
    }

    Map<String, String> getQueryParams() {
        return queryParams
    }

    Map<String, String> getHeaders() {
        return headers
    }

    HttpValidatorWrapper getValidator() {
        this.validatorWrapper
    }

    String getUri() {
        return uri
    }

    @Override
    boolean isSupportsIncomingBody() {
        true
    }

    @Override
    Object getIncomingBody() {
        // no payloads should be a part of GET
        if (this.method == 'GET') {
            return null
        }
        def value = this.body.value
        if (value instanceof InputStream) {
            return value.text
        }
        return value
    }

    def getHttpResponseAttributes(int statusCode,
                                  String reasonPhrase,
                                  Map additionalHeaders = [:]) {
        def inner = new InnerHttp()
        inner.getHttpResponseAttributes(statusCode,
                                        reasonPhrase,
                                        appClassLoader,
                                        additionalHeaders)
    }

    private def throwConnectException() {
        def exception = getConnectionException(uri,
                                               method,
                                               appClassLoader)
        throw exception
    }

    private def throwTimeOutException() {
        def exception = getTimeoutException(uri,
                                            method,
                                            appClassLoader)
        throw exception
    }

    @Override
    HttpClosureEvalResponse evaluateClosure(EventWrapper event,
                                            Object input,
                                            Closure closure,
                                            ClosureCurrier closureCurrier) {
        // there might be a better way to do this but this will allow specific connectors
        // to handle stuff inside "whenCalledWith" like error triggering, etc.
        def statusCode = 200
        def connector = this
        def errorHandler = new ErrorThrowing() {
            @Override
            def setHttpStatusCode(int code) {
                // will allow us to grab the desired code and then use the output from the closure
                // to populate a prospective error message
                statusCode = code
                return null
            }

            @Override
            def httpConnectError() {
                // immediate is OK
                connector.throwConnectException()
                return null
            }

            @Override
            def httpTimeoutError() {
                // immediate is OK
                connector.throwTimeOutException()
                return null
            }
        }
        closure = closure.rehydrate(errorHandler,
                                    closure.owner,
                                    closure.thisObject)
        def curried = closureCurrier.curryClosure(closure,
                                                  event,
                                                  this)
        def result = curried.parameterTypes.size() == 0 ? curried() : curried(input)
        validator.validate(statusCode,
                           'Test framework told us to',
                           ['X-Some-Header': '123'],
                           result)
        new HttpClosureEvalResponse(httpStatus: statusCode,
                                    response: result)
    }

    @Override
    EventWrapper transformEvent(EventWrapper incomingEvent,
                                HttpClosureEvalResponse closureResponse) {
        // will allow rest of flow to use our mocked HTTP status via attributes
        def attributes = getHttpResponseAttributes(closureResponse.httpStatus,
                                                   'the reason')
        incomingEvent.withNewAttributes(attributes)
    }
}
