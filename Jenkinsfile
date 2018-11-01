node {
    stage('Checkout') {
         checkout scm
    }

    stage('Build and deploy') {
        sh './gradlew clean test'
    }
}
