package com.avioconsulting.mule.testing.dsl.mocking

import com.avioconsulting.mule.testing.mulereplacements.MuleMessageTransformer
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ConnectorInfo
import com.avioconsulting.mule.testing.transformers.ClosureCurrier
import com.avioconsulting.mule.testing.transformers.StandardTransformer
import com.avioconsulting.mule.testing.transformers.json.input.JacksonInputTransformer
import com.avioconsulting.mule.testing.transformers.json.output.JacksonOutputTransformer

class JsonFormatterImpl<T extends ConnectorInfo> implements
        JsonFormatter,
        IFormatter {
    private MuleMessageTransformer<T> transformer
    private final ClosureCurrier<T> closureCurrier

    JsonFormatterImpl(ClosureCurrier<T> closureCurrier) {
        this.closureCurrier = closureCurrier
    }

    def whenCalledWith(Closure closure) {
        def input = new JacksonInputTransformer<T>(Map)
        def output = new JacksonOutputTransformer()
        this.transformer = new StandardTransformer<T>(closure,
                                                      closureCurrier,
                                                      input,
                                                      output)
    }

    def whenCalledWith(Class inputClass,
                       Closure closure) {
        def input = new JacksonInputTransformer<T>(inputClass)
        def output = new JacksonOutputTransformer()
        this.transformer = new StandardTransformer<T>(closure,
                                                      closureCurrier,
                                                      input,
                                                      output)
    }

    MuleMessageTransformer<T> getTransformer() {
        transformer
    }
}
