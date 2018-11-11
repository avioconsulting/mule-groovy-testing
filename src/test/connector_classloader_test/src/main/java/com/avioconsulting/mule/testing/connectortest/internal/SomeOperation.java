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
    public PagingProvider<Conn, String> classloadertest() {
        String threadBeforePaging = "id: " + Thread.currentThread().getId() + " name: " + Thread.currentThread().getName();
        String classLoaderBeforePaging = Thread.currentThread().getContextClassLoader().toString();
        return new PagingProvider<Conn, String>() {
            @Override
            public List<String> getPage(Conn conn) {
                Executor executor = new Executor();
                ArrayList<String> list = new ArrayList<>();
                list.add("our debug enabled " + logger.isDebugEnabled());
                logger.info("executor level is " + executor.doExecute());
                logger.info("name of the thread now is " + "id: " + Thread.currentThread().getId() + " name: " + Thread.currentThread().getName());
                logger.info("name of the thread before paging is " + threadBeforePaging);
                list.add("the classloader before paging is " + classLoaderBeforePaging);
                list.add("the classloader now is " + Thread.currentThread().getContextClassLoader());
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

    @MediaType("text/plain")
    public PagingProvider<Conn, String> staticlist() {
        final List<Integer> done = new ArrayList<>();
        return new PagingProvider<Conn, String>() {
            @Override
            public List<String> getPage(Conn conn) {
                ArrayList<String> list = new ArrayList<>();
                if (done.isEmpty()) {
                    list.add("item1");
                    list.add("item2");
                    done.add(1);
                }
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
