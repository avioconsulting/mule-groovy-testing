package com.avioconsulting.mule.testing.muleinterfaces.wrappers

trait StreamUtils {
    /**
     * Gets the underlying text from the stream
     * @param closure
     */
    def withCursorAsTest(Object payload,
                         Closure closure) {
        assert payload.getClass().getName().contains('ManagedCursorStreamProvider')
        def cursor = payload.openCursor()
        try {
            closure(cursor.text)
        }
        finally {
            cursor.close()
        }
    }
}
