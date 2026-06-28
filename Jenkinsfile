pipeline {
    agent any

    environment {
        IMAGE_BACKEND  = "loan-decision-engine-backend"
        IMAGE_FRONTEND = "loan-decision-engine-frontend"
        REGISTRY       = "your-registry.io"
        KUBECONFIG     = credentials('kubeconfig')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/nagaramnagalla014-ctrl/Loan-Decision-Engine.git'
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests -q'
                }
            }
        }

        stage('Test Backend') {
            steps {
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    docker.withRegistry("https://${REGISTRY}", 'registry-credentials') {
                        docker.build("${IMAGE_BACKEND}:${BUILD_NUMBER}", "-f Dockerfile.backend .").push()
                        docker.build("${IMAGE_FRONTEND}:${BUILD_NUMBER}", "-f Dockerfile.frontend .").push()
                        docker.image("${IMAGE_BACKEND}:${BUILD_NUMBER}").push('latest')
                        docker.image("${IMAGE_FRONTEND}:${BUILD_NUMBER}").push('latest')
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl apply -f k8s/ --kubeconfig=${KUBECONFIG}"
                sh "kubectl set image deployment/loan-backend backend=${REGISTRY}/${IMAGE_BACKEND}:${BUILD_NUMBER} -n loan-engine --kubeconfig=${KUBECONFIG}"
                sh "kubectl set image deployment/loan-frontend frontend=${REGISTRY}/${IMAGE_FRONTEND}:${BUILD_NUMBER} -n loan-engine --kubeconfig=${KUBECONFIG}"
                sh "kubectl rollout status deployment/loan-backend -n loan-engine --timeout=120s --kubeconfig=${KUBECONFIG}"
            }
        }
    }

    post {
        success { echo "Loan Decision Engine deployed successfully. Build: ${BUILD_NUMBER}" }
        failure { echo "Deployment failed for build: ${BUILD_NUMBER}" }
    }
}
