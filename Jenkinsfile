#!/usr/bin/env groovy

// so much of this is yoinked from hexdebug, thanks kyra
RELEASE_BRANCHES = ["main"]

pipeline {
    agent any
    tools {
        jdk "jdk-17" // valid options are: "jdk-8", "jdk-16", "jdk-17" or "jdk-21", choose which one you need
    }
    parameters {
        booleanParam(
            name: "PUBLISH_MAVEN_RELEASE",
            description: "Publish release to Maven (default: publish snapshot)",
            defaultValue: false,
        )
        booleanParam(
            name: "PUBLISH_CURSEFORGE",
            description: "Publish to CurseForge",
            defaultValue: false,
        )
        booleanParam(
            name: "PUBLISH_MODRINTH",
            description: "Publish to Modrinth",
            defaultValue: false,
        )
        booleanParam(
            name: "PUBLISH_GITHUB",
            description: "Publish to GitHub",
            defaultValue: false,
        )
    }
    stages {
        stage('Clean') {
            steps {
                echo 'Cleaning Project'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }
        stage('Build') {
            steps {
                echo 'Building'
                sh './gradlew build'
            }
        }
        stage("Publish") {
            when {
                expression { env.BRANCH_NAME in RELEASE_BRANCHES }
            }
            stages {
                stage("Publish: Maven") {
                    steps {
                        sh "./gradlew publish"
                    }
                }
                stage("Publish: GitHub") {
                    when {
                        expression { params.PUBLISH_GITHUB }
                    }
                    environment {
                        GITHUB_TOKEN = credentials("github-release-pat")
                        GITHUB_REPOSITORY = "SamsTheNerd/inline"
                        GITHUB_SHA = "${env.GIT_COMMIT}"
                    }
                    steps {
                        sh "./gradlew publishGithub"
                    }
                }
                stage("Publish: CurseForge") {
                    when {
                        expression { params.PUBLISH_CURSEFORGE }
                    }
                    environment {
                        CURSEFORGE_TOKEN = credentials("curseforge-token")
                    }
                    steps {
                        sh "./gradlew publishCurseforge"
                    }
                }
                stage("Publish: Modrinth") {
                    when {
                        expression { params.PUBLISH_MODRINTH }
                    }
                    environment {
                        MODRINTH_TOKEN = credentials("modrinth-pat")
                    }
                    steps {
                        sh "./gradlew publishModrinth"
                    }
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts "build/jenkinsArtifacts/*"
        }
    }
}