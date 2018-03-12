@Library('pipeline-library@runATH-step') _

buildPlugin()

node("linux") {
    dir("ath") {
      checkout scm
      List<String> mavenEnv = [
                "JAVA_HOME=${tool 'jdk8'}",
                'PATH+JAVA=${JAVA_HOME}/bin',
                "PATH+MAVEN=${tool 'mvn'}/bin"]
      withEnv(mavenEnv) {
        def settingsXml = "${pwd tmp: true}/settings-azure.xml"
        writeFile file: settingsXml, text: libraryResource('settings-azure.xml')
        sh "mvn clean install -DskipTests -s $settingsXml"
      }
      dir("target") {
       stash includes: '*.hpi', name: 'snapshots'
      }
    
      env.RUN_ATH_LOCAL_PLUGINS_STASH_NAME="snapshots"
      runATH(metadataFile:"metadata.yml", athRevision: "master")
    }
}
