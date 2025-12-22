pipeline {
    agent { label 'linux-agent' }

    parameters {
        choice(name: 'ACTION', choices: ['Deploy New Version', 'Rollback'], description: 'Choose whether to build code or rollback.')
        string(name: 'VERSION_TAG', defaultValue: 'v1.0.0', description: 'The tag to apply (e.g., v1.0.0) or the tag to Rollback to.')
        string(name: 'APP_COLOR', defaultValue: '#ADD8E6', description: 'Hex (Blue: #ADD8E6, Green: #90EE90, Red: #FFCCCC)')
    }

    environment {
        NEXUS_REGISTRY = 'registry.nchldemo.com'
        IMAGE_NAME = 'findash-app-trainer'
        NEXUS_CRED = 'nexus-auth'
        CONTAINER_NAME = 'findash-app-trainer'
    }

    stages {
        // --- Stage 1: Build the Image ---
        stage('Build Image') {
            when { expression { params.ACTION == 'Deploy New Version' } }
            steps {
                script {
                    echo "Building Version: ${params.VERSION_TAG}"
                    docker.build("${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG}")
                }
            }
        }

        // --- Stage 2: Trivy Security Scan ---
        stage('Trivy Security Scan') {
            when { expression { params.ACTION == 'Deploy New Version' } }
            steps {
                script {
                    echo "Scanning Image for Vulnerabilities..."
                    // --severity: Only show High and Critical bugs
                    // --exit-code 0: Don't fail the build (Change to 1 if you want to block bad builds)
                    // --no-progress: Cleaner logs in Jenkins
                    sh "trivy image --severity HIGH,CRITICAL --no-progress --exit-code 0 ${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG}"
                }
            }
        }

        // --- Stage 3: Push to Nexus ---
        stage('Push to Nexus') {
            when { expression { params.ACTION == 'Deploy New Version' } }
            steps {
                script {
                    docker.withRegistry("http://${NEXUS_REGISTRY}", "${NEXUS_CRED}") {
                        docker.image("${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG}").push()
                    }
                }
            }
        }

        // --- Stage 4: Deploy ---
        stage('Deploy to Environment') {
            steps {
                script {
                    echo "Starting Deployment for Tag: ${params.VERSION_TAG}..."
                    
                    // Cleanup old container
                    sh "docker rm -f ${CONTAINER_NAME} || true"

                    def envColor = params.APP_COLOR
                    def envVersion = params.VERSION_TAG

                    docker.withRegistry("http://${NEXUS_REGISTRY}", "${NEXUS_CRED}") {
                        // Pull logic ensures we use the registry version
                        sh "docker pull ${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG}"

                        sh """
                            docker run -d \
                            --name ${CONTAINER_NAME} \
                            -p 9091:8080 \
                            -e APP_VERSION="${envVersion}" \
                            -e BG_COLOR="${envColor}" \
                            ${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG}
                        """
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    sh "sleep 5"
                    sh "curl -f http://localhost:9091 || exit 1"
                    echo "Deployment Successful! Access at http://localhost:9091"
                }
            }
        }
    }

    // --- CLEANUP SECTION (Best Practice) ---
    // This runs regardless of whether the build passed or failed
    post {
        always {
            script {
                echo '--- Cleaning up Docker Agent ---'
                
                // 1. Remove the specific image version created in this build to free space
                sh "docker rmi ${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG} || true"
                
                // 2. Remove "dangling" images (failed builds or intermediate layers)
                // This keeps the agent clean over time
                sh "docker image prune -f"
            }
        }
    }
}