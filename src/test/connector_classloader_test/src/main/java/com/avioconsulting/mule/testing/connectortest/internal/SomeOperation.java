package com.avioconsulting.mule.testing.connectortest.internal;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SomeOperation {
    private static final Logger logger = LoggerFactory.getLogger(SomeOperation.class);

    @MediaType("text/plain")
    public PagingProvider<Conn, String> foo() {
        String threadBeforePaging = Thread.currentThread().getName();
        return new PagingProvider<Conn, String>() {
            @Override
            public List<String> getPage(Conn conn) {
                Executor executor = new Executor();
                ArrayList<String> list = new ArrayList<>();
                System.out.println("hello from foo");
                System.out.println("our debug enabled " + logger.isDebugEnabled());
                System.out.println("executor level is " + executor.doExecute());
                System.out.println("name of the thread now is " + Thread.currentThread().getName());
                System.out.println("name of the thread before paging is " + threadBeforePaging);
                System.out.println("the classloader is " + Thread.currentThread().getContextClassLoader());
                return list;
            }

            @Override
            public Optional<Integer> getTotalResults(Conn conn) {
                return Optional.empty();
            }

            @Override
            public void close(Conn conn) throws MuleException {

            }
        };
    }
}
