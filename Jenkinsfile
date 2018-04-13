node("docker&&highmem") {
    dir("ath-githubnotify") {
      deleteDir() 
      checkout scm
      runATH(metadataFile:"metadata.yml", athRevision: "master")
    }
}
