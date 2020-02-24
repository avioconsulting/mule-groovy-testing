node {
    stage('Checkout') {
         checkout scm
    }

    stage('Build and deploy') {
        withMaven(maven: 'Maven 3',
                  mavenSettingsConfig: '3144821b-28b7-414d-99b5-10ce1bee8c09') {
            // multi-module project so needs install
            sh 'mvn clean install'
        }
    }
}
