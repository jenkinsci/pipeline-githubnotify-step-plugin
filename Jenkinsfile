@Library('pipeline-library@runATH-step') _

node("linux") {
    
    dir("ath") {
      checkout scm
      List<String> env = [
                "JAVA_HOME=${tool 'jdk8'}",
                'PATH+JAVA=${JAVA_HOME}/bin',
                "PATH+MAVEN=${tool 'mvn'}/bin"]
      withEnv(env) {
        sh "mvn clean install -DskipTests"
      }
      dir("target") {
       stash includes: '*.hpi', name: 'snapshots'
      }
    
      env.RUN_ATH_LOCAL_PLUGINS_STASH_NAME="snapshots"
      runATH(metadataFile:"metadata.yml", athRevision: "master")
    }
}
