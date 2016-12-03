# Pipeline GitHub Notify Step Plugin

This step allows a pipeline job to notify a status for any GitHub commit.

Intended for jobs that want to notify GitHub of any desired event with complete control over the
notification content. Including context, status or target url.

The available parameters are:

* _credentialsId_: The id of the github's credentials to use, must be of type UsernameAndPassword
* _status_: The status to send, one of SUCCESS, FAILURE, ERROR or PENDING
* _description_: The description that will appear at the notification
* _context_: The notifications context, GH uses the context to diferentiate notifications (optional, jenkins/githubnotify is used by default)
* _sha_: The sha that identifies the commit to notify status
* _repo_: The repo that ows the commit we want to notify
* _account_: The account that owns the repository;
* _gitApiUrl_: GitHub Enterprise instance API URL (optional, https://api.github.com is used by default)
* _targetUrl_: The targetUrl for the notification

# Inferring parameter values

It may be cumbersome to specify all parameters, so this step will try to infer some of them if and only if
are not provided. The parameters that can be inferred are:

* _credentialsId_ Is inferred from the SCM used on the parent project
* _repo_ is inferred from the Git Build Data of the current build
* _sha_ is inferred from the Git Build Data of the current build
* _account_ is inferred from the Git Build Data of the current build

*Please note that infer will only work if you have Git Build Data and the parent of the Build has one and only one SCM, for example you created a Multibranch Pipeline
project and you are using a Jenkinsfile build mode. If you find problems when inferring please specify the
required data explicitly. (You can access this data on your Jenkinsfile by using the appropriate env variables)*

# Example

```
githubNotify account: 'raul-arabaolaza', context: 'Final Test', credentialsId: 'raul-github',
    description: 'This is an example', repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'
    , status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'
```

# Example with data inference and default values

```
githubNotify description: 'This is a shorted example',  status: 'SUCCESS'
```
