pipeline {
    agent any

    environment {
        jdk = 'jdk8'
        mvn = 'Maven 3'
        version = "1.0.${env.BUILD_NUMBER}"
        standard_avio_mvn_settings = '3144821b-28b7-414d-99b5-10ce1bee8c09'
    }

    stages {
        stage('Build and unit test') {
            steps {
                withMaven(jdk: env.jdk,
                          maven: env.mvn,
                          mavenSettingsConfig: env.standard_avio_mvn_settings,
                          // only want to capture artifact if we're deploying (see below)
                          options: [artifactsPublisher(disabled: true)]) {
                    mavenSetVersion(env.version)
                    // would usually use package but we have a multi module interdependent project
                    quietMaven 'clean install'
                }
                archiveArtifacts 'cli/target/appassembler/**/*'
            }

            options {
                lock('mule-groovy-testing-fr')
            }
        }

        stage('Deploy') {
            steps {
                withMaven(jdk: env.jdk,
                          maven: env.mvn,
                          mavenSettingsConfig: env.standard_avio_mvn_settings) {
                    quietMaven 'clean deploy -DskipTests'
                    // keeps buildDiscarder from getting rid of stuff we've published
                    keepBuild()
                }
            }

            when {
                branch 'master'
            }
        }
    }

    options {
        buildDiscarder logRotator(numToKeepStr: '3')
        timestamps()
    }

    post {
        always {
            cleanWs()
        }
        failure {
            handleError([message: 'Build failed'])
        }
        unstable {
            handleError([message: 'Build failed'])
        }
    }
}
