@Library("pipeline-library@INFRA-1607") _
def branches = [:]

branches["ATH"] = {
    node("docker && highmem") {
        def checkoutGit
        stage("ATH: Checkout") {
            checkoutGit = pwd(tmp:true) + "/athgit"
            dir(checkoutGit) {
                checkout scm
                infra.runMaven(["clean", "package", "-DskipTests"])
                dir("target") {
                    stash name: "localPlugins", includes: "*.hpi"
                }
            }
        }
        def metadataPath = checkoutGit + "/essentials.yml"
        stage("Run ATH") {
            def athFolder=pwd(tmp:true) + "/ath"
            dir(athFolder) {
                runATH metadataFile: metadataPath
            }
        }
    }
}

parallel branches
