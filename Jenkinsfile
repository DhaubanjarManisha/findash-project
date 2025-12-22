pipeline {
    agent linux-agent

    parameters {
        choice(name: 'ACTION', choices: ['Deploy New Version', 'Rollback'], description: 'Choose whether to build code or rollback.')
        string(name: 'VERSION_TAG', defaultValue: 'v1.0.0', description: 'The tag to apply (e.g., v1.0.0) or the tag to Rollback to.')
        string(name: 'APP_COLOR', defaultValue: '#ADD8E6', description: 'Hex     (Blue: #ADD8E6, Green: #90EE90, Red: #FFCCCC)')
    }

    environment {
        // Nexus Internal URL (Jenkins -> Nexus)
        NEXUS_REGISTRY = 'nexus.nchldemo.com:5000'
        // Image Name
        IMAGE_NAME = 'findash-app-trainer' // Add your username here
        // Jenkins Credential ID
        NEXUS_CRED = 'nexus-auth-trainer' 
    }

    stages {
        // --- Stage 1: Build & Push (Only runs if NOT rolling back) ---
        stage('Build & Push Artifact') {
            when { expression { params.ACTION == 'Deploy New Version' } }
            steps {
                script {
                    echo "Building Version: ${params.VERSION_TAG}"
                    
                    // Build Docker Image
                    // We pass build-args to "bake" the configuration into the image for this demo
                    def appImage = docker.build("${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG}")

                    // Login to Nexus and Push
                    docker.withRegistry("http://${NEXUS_REGISTRY}", "${NEXUS_CRED}") {
                        appImage.push()
                        appImage.push("latest")
                    }
                }
            }
        }

        // --- Stage 2: Deploy (Runs for BOTH new deploys and rollbacks) ---
        stage('Deploy to Environment') {
            steps {
                script {
                    echo "Starting Deployment for Tag: ${params.VERSION_TAG}..."

                    // 1. Clean up existing container (if any)
                    // The '|| true' ensures the pipeline doesn't fail if the container doesn't exist yet
                    sh "docker rm -f ${CONTAINER_NAME} || true"

                    // 2. Determine configuration based on deployment type
                    // In a real scenario, these configs might come from a ConfigMap or Helm Chart
                    def envColor = params.APP_COLOR
                    def envVersion = params.VERSION_TAG

                    // 3. Login & Run
                    docker.withRegistry("http://${NEXUS_REGISTRY}", "${NEXUS_CRED}") {
                        
                        // Pull the specific version (important for Rollback)
                        sh "docker pull ${NEXUS_REGISTRY}/${IMAGE_NAME}:${params.VERSION_TAG}"

                        // Run Container
                        // We map Host 9090 -> Container 8080 change port 9090 to a random port
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
                    // Simple health check
                    sh "sleep 5" // Wait for startup and add the same random port here
                    sh "curl -f http://localhost:9091 || exit 1"
                    echo "Deployment Successful! Access at http://localhost:9091"
                }
            }
        }
    }
}