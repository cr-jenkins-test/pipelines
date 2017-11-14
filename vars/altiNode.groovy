def call(Closure body) {
  def secretsRoot = System.getenv('JENKINS_SECRETS') ?: '/var/jenkins_secrets'
  def artifactoryToken = new File("$secretsRoot/artifactory-token").text.trim()

  withEnv(['CI=true', "BERKSHELF_PATH=${env.WORKSPACE}/.berkshelf", 'ALTISCALE_BERKS_ARTIFACTORY=true', "ARTIFACTORY_API_KEY=$artifactoryToken"]) {
    node('cookbook') {
      container('alti-pipeline') {
        body()
      }
    }
  }
}
