def call(Map config = [:]) {
    pipeline {
        agent {
            node {
                label 'linux'
            }
        }

        parameters {
            gitParameter(type: 'PT_BRANCH', name: 'BRANCH', branchFilter: 'origin/(.*)', defaultValue: 'main', description: 'Select branch to deploy from repository', selectedValue: 'TOP', sortMode: 'DESCENDING_SMART', tagFilter: '*', listSize: '1')
        }

        stages {
            stage('Checkout') {

                steps {
                    script {
                        // Use the branch from parameters
                        checkout([$class: 'GitSCM', 
                            branches: [[name: "${params.BRANCH}"]], 
                            userRemoteConfigs: [[url: config.repoUrl ?: 'https://github.com/muharik28/vibe-coding-bun-router']]
                        ])
                    }
                }
            }

            stage('Build') {

                steps {
                    script {
                        echo "Building application: ${config.appName ?: 'App'}"

                        sh '''
                            # Aktifkan Docker BuildKit untuk mempercepat proses build
                            export DOCKER_BUILDKIT=1
                            export COMPOSE_DOCKER_CLI_BUILD=1

                            # Pengecekan apakah command docker ada di dalam Jenkins container
                            if ! command -v docker &> /dev/null; then
                                # Hanya download jika folder docker belum ada di workspace
                                if [ ! -d "$(pwd)/docker" ]; then
                                    echo "Docker CLI tidak ditemukan! Mengunduh versi static sementara..."
                                    curl -fsSLO https://download.docker.com/linux/static/stable/x86_64/docker-24.0.9.tgz
                                    tar xzvf docker-24.0.9.tgz
                                else
                                    echo "Menggunakan Docker CLI static dari cache workspace..."
                                fi
                                export PATH=$PATH:$(pwd)/docker
                            fi

                            # Pengecekan apakah command docker-compose ada
                            if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
                                # Hanya download jika file docker-compose belum ada di workspace
                                if [ ! -f "$(pwd)/docker-compose" ]; then
                                    echo "Docker Compose tidak ditemukan! Mengunduh versi standalone..."
                                    curl -SL https://github.com/docker/compose/releases/download/v2.26.1/docker-compose-linux-x86_64 -o docker-compose
                                    chmod +x docker-compose
                                else
                                    echo "Menggunakan Docker Compose standalone dari cache workspace..."
                                fi
                                export PATH=$PATH:$(pwd)
                            fi

                            # Menjalankan build dan deploy menggunakan standalone docker-compose
                            docker-compose up -d --build
                        '''
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
