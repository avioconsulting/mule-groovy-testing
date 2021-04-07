package com.avioconsulting.mule.testing.muleinterfaces.wrappers

trait StreamUtils {
    /**
     * Gets the underlying text from the stream
     * @param closure
     */
    def withCursorAsText(Object payload,
                         Closure closure) {
        if (!payload.getClass().getName().contains('ManagedCursorStreamProvider')) {
            throw new Exception("Expected ${payload.getClass().getName()} to be of type ManagedCursorStreamProvider!")
        }
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
        if (!payload.getClass().getName().contains('ManagedCursorStreamProvider')) {
            throw new Exception("Expected ${payload.getClass().getName()} to be of type ManagedCursorStreamProvider!")
        }
        def cursor = payload.openCursor() as Iterator
        try {
            return closure(cursor.toList())
        }
        finally {
            cursor.close()
        }
    }
}
