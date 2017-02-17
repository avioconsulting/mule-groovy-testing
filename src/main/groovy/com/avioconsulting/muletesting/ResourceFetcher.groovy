package com.avioconsulting.muletesting

class ResourceFetcher {
    private final Object mockPathClosure

    def ResourceFetcher(mockPathClosure) {
        this.mockPathClosure = mockPathClosure
    }

    BufferedInputStream getResource(String filename) {
        def path = this.mockPathClosure(filename)
        def stream = getClass().getResourceAsStream path
        if (stream == null) {
            throw new FileNotFoundException(path)
        }
        stream
    }
}
