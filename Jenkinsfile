node {
    stage('Checkout') {
         checkout([
            $class: 'GitSCM',
            branches: scm.branches,
            extensions: scm.extensions + [[$class: 'CleanCheckout']],
            userRemoteConfigs: scm.userRemoteConfigs
          ])
    }

    stage('Build and deploy') {
        sh './gradlew clean test'
    }
}
