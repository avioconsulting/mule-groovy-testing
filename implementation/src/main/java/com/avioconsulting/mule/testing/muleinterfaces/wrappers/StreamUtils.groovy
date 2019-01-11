package com.avioconsulting.mule.testing.muleinterfaces.wrappers

trait StreamUtils {
    /**
     * Gets the underlying text from the stream
     * @param closure
     */
    def withCursorAsText(Object payload,
                         Closure closure) {
        assert payload.getClass().getName().contains('ManagedCursorStreamProvider')
        def cursor = payload.openCursor()
        try {
            return closure(cursor.text)
        }
        finally {
            cursor.close()
        }
    }
}
