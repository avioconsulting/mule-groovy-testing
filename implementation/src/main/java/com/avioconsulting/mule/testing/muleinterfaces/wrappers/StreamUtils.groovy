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

    /**
     * Gets the underlying list from a cursor iterator (e.g. for DB connectors)
     * @param payload
     * @param closure
     */
    List withCursorAsList(Object payload,
                         Closure closure) {
        assert payload.getClass().getName().contains('ManagedCursorIteratorProvider')
        def cursor = payload.openCursor() as Iterator
        try {
            return closure(cursor.toList())
        }
        finally {
            cursor.close()
        }
    }
}
