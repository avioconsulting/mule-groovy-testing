node {
    stage('Checkout') {
         checkout([
            $class: 'GitSCM',
            branches: scm.branches,
            extensions: [[$class: 'CleanCheckout']],
            userRemoteConfigs: []
          ])
    }

    stage('Build and deploy') {
        sh './gradlew clean test'
    }
}
