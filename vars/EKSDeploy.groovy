def call(Map configMap){
     pipeline {
    // This is a pre build section
    agent {
        node {
            label 'AGENT-1'
        }
    }
    environment {
        COURSE = "Jenkins"
        appVersion = configMap.get("appVersion") //appversion comes from configmap
        ACC_ID = "485658242739"
        PROJECT = configMap.get("project")
        COMPONENT = configMap.get("component")
        deploy_to =  configMap.get("deploy_to")
        region = "us-east-1"
    }
    options {
        timeout(time: 10, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    // This is a build section.
     stages {
        stage('Deploy') {
            steps {
                script { 
                    withAWS(region:'us-east-1',credentials:'aws-creds') {

                     sh """
                        aws eks update-kubeconfig --region ${REGION} --name ${PROJECT}-${deploy_to}
                        kubectl get nodes
                        echo ${deploy_to},${appVersion}
                        //sed -i "s/IMAGE_VERSION/${appVersion}/g" values.yaml
                        //helm upgrade --install ${COMPONENT} -f values-${deploy_to}.yaml -n ${PROJECT} --atomic --wait --timeout=5m .
                        //kubectl apply -f ${COMPONENT}-${deploy_to}.yml
                     """
                }
                }

            }
        }

    }

    post {
        always {
            echo 'I will always say hello again!'
            cleanWs()
        }

        success {
            echo 'I will run if success'
        }
       
       failure {
           echo 'I will run if failure'
       }
       aborted {
            echo 'pipeline is aborted'
       }
    }
}
}
