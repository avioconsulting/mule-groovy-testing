package com.avioconsulting.mule.testing

trait EnvironmentDetector {
    boolean isEclipse() {
        System.getProperty('sun.java.command').contains('org.eclipse.jdt.internal.junit.runner.RemoteTestRunner')
    }
}
