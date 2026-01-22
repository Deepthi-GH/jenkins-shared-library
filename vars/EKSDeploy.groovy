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
                        set -e
                        aws eks update-kubeconfig --region ${REGION} --name ${PROJECT}-${deploy_to}
                        kubectl get nodes
                        sed -i "s/IMAGE_VERSION/${appVersion}/g" values.yml #replace these values to values.yaml.
                        helm upgrade --install ${COMPONENT} -f values-${deploy_to}.yml -n ${PROJECT} --atomic --wait --timeout=5m .
                        #if 1st time, it will take install .otherwise it will take upgrade.if deployment/pods are not ready at this stage helm should roll back to previous version.but pipeline fails.
                        #atomic means automatically wait for 5mins.if pods not available fail the pipeline.
                        #kubectl apply -f ${COMPONENT}-${deploy_to}.yml
                     """
                }
                }

            }
        }
        stage('Functional Testing') {
            when{
                expression { deploy_to == "dev" }
            }
            steps {
                script {
                    sh """
                       echo "functional tests in DEV environment"
                    """
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
