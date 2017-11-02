def call(Closure body) {
    withCredentials([string(credentialsId: 'artifactory-jenkins-dev', variable: 'ARTIFACTORY_API_KEY')]) {
        withEnv(['CI=true', "BERKSHELF_PATH=${env.WORKSPACE}/.berkshelf", 'ALTISCALE_BERKS_ARTIFACTORY=true']) {
            node('cookbook') {
                container('alti-pipeline') {
                    body()
                }
            }
        }
    }
}
