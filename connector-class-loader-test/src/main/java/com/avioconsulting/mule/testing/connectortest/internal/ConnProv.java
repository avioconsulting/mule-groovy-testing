package com.avioconsulting.mule.testing.connectortest.internal;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;

public class ConnProv implements PoolingConnectionProvider<Conn> {
    @Override
    public Conn connect() throws ConnectionException {
        return new Conn();
    }

    @Override
    public void disconnect(Conn conn) {
    }

    @Override
    public ConnectionValidationResult validate(Conn conn) {
        return ConnectionValidationResult.success();
    }
}
