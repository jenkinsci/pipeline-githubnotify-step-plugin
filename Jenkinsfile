@Library('pipeline-library@runATH-step') _

node("linux") {
    dir("ath") {
      checkout scm
      List<String> mavenEnv = [
                "JAVA_HOME=${tool 'jdk8'}",
                'PATH+JAVA=${JAVA_HOME}/bin',
                "PATH+MAVEN=${tool 'mvn'}/bin"]
      withEnv(mavenEnv) {
        dir("test") {
            sh "mvn dependency:copy -Dartifact=org.jenkins-ci.main:jenkins-war:2.110:war -DoutputDirectory=. -Dmdep.stripVersion=true -Dmaven.repo.remote=https://repo.azure.jenkins.io/public/ -U"
        }
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
