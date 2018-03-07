@Library('pipeline-library@INFRA-1489') _

node("linux") {
    
    dir("ath") {
      checkout scm
    
      sh "mvn clean install -DskipTests"
      dir("target") {
       stash includes: '*.hpi', name: 'snapshots'
      }
    
      env.RUN_ATH_LOCAL_PLUGINS_STASH_NAME="snapshots"
      runATH(metadataFile:"metadata.yml", athRevision: "master")
    }
}
