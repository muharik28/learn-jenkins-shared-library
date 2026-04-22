def call(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    call(config)
}

def call(Map config = [:]) {
    pipeline {
        agent any
        
        parameters {
            string(name: 'BRANCH', defaultValue: 'main', description: 'Select branch to deploy')
        }

        stages {
            stage('Checkout') {
                steps {
                    script {
                        // Use the branch from parameters
                        checkout([$class: 'GitSCM', 
                            branches: [[name: "*/${params.BRANCH}"]], 
                            userRemoteConfigs: [[url: config.repoUrl ?: 'https://github.com/muharik28/vibe-coding-bun-router']]
                        ])
                    }
                }
            }
            
            stage('Build') {
                steps {
                    script {
                        echo "Building application: ${config.appName ?: 'App'}"
                        // Using docker-compose as per project structure
                        sh "docker compose up -d --build"
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        echo 'Deploying application successful'
                    }
                }
            }
        }
        
        post {
            success {
                echo "Deployment of ${params.BRANCH} successful!"
            }
            failure {
                echo "Deployment of ${params.BRANCH} failed."
            }
        }
    }
}
