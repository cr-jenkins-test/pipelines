def altiPipelineVersion = '4.9.0'

def call() {
    podTemplate(label: 'cookbook', imagePullSecrets: ['artifactory-pull'], containers: [
      containerTemplate(name: 'alti-pipeline', image: "altiscale-docker-dev.jfrog.io/alti_pipeline:testing" /* "altiscale-docker-dev.jfrog.io/alti_pipeline:${altiPipelineVersion}" */, alwaysPullImage: false /* true */, command: "/bin/sh -c \"trap 'exit 0' TERM; sleep 2147483647 & wait\""),
    ]) {
        // stage('Wat') {
        //     withCredentials([string(credentialsId: 'artifactory-jenkins-dev', variable: 'ARTIFACTORY_API_KEY')]) {
        //         echo env.ARTIFACTORY_API_KEY[0..10]
        //         altiNode {
        //             sh 'env'
        //         }
        //     }
        //     altiNode {
        //         sh 'env'
        //     }
        // }
        def integrationTests = []
        stage('Check') {
            altiNode {
                checkout scm
                // Check that we have an acceptable version of alti_pipeline, just looks at the major version.
                def gemfile = readFile('Gemfile')
                if(gemfile =~ /gem.*alti_pipeline.*\b${altiPipelineVersion[0]}\./) {
                    echo "Gemfile is compatible with alti_pipeline ${altiPipelineVersion}"
                } else {
                    error "Gemfile is not compatible with alti_pipeline ${altiPipelineVersion}:\n"+gemfile
                }
                // Parse out the integration tests for use in the next stage.
                integrationTests = sh(script: 'kitchen list --bare', returnStdout: true).split()
            }
        }
        stage('Test') {
            testJobs = [
                'Lint': {
                    altiNode {
                        checkout scm
                        sh 'rm -f Gemfile Gemfile.lock'
                        sh 'rake style'
                    }
                },
                // 'Unit Tests': {
                //     altiNode {
                //         checkout scm
                //         try {
                //             sh 'rm -f Gemfile Gemfile.lock'
                //             sh 'rake spec'
                //         } finally {
                //             junit 'results.xml'
                //         }
                //     }
                // },
                // 'Integration Tests': {
                //     altiNode {
                //         checkout scm
                //         sh 'rm -f Gemfile Gemfile.lock'
                //         sh 'kitchen test --destroy always'
                //     }
                // }
            ]
            // integrationTests.each { instance ->
            //   testJobs["Integration $instance"] = {
            //         altiNode {
            //             checkout scm
            //             sh 'rm -f Gemfile Gemfile.lock'
            //             sh "kitchen test --destroy always $instance"
            //         }
            //     }
            // }
            parallel(testJobs)
        }
        stage('Publish') {
            echo 'TODO Publishing ...'
        }

    }
}
