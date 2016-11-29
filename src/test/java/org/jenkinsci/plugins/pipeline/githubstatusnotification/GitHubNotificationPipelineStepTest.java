package org.jenkinsci.plugins.pipeline.githubstatusnotification;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.Matchers.anyString;

@RunWith (PowerMockRunner.class)
@PrepareForTest ({GitHub.class})
@PowerMockIgnore ({"javax.crypto.*" })
public class GitHubNotificationPipelineStepTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void buildWithNullCredentialsIDMustFail() throws Exception {
        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "description: 'PCT Is OK', repo: 'acceptance-test-harness', " +
                        "sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', status: 'SUCCESS', " +
                        "targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }


    @Test
    public void buildWithNotExistingCredentialsMustFail() throws Exception {
                        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                                "credentialsId: 'notExisting', description: 'PCT Is OK', " +
                                "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                                "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
                ));
                WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
                jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
                jenkins.assertLogContains(GitHubStatusNotificationStep.CREDENTIALS_ID_NOT_EXISTS, b1);
            }

    @Test
    public void buildWithWrongCredentialsMustFail() throws Exception {

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
        "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
        "credentialsId: 'dummy', description: 'PCT Is OK', " +
        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.CREDENTIALS_LOGIN_INVALID, b1);
    }

    @Test
    public void buildWithWrongCredentialsMustFailEnterprise() throws Exception {

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com', gitApiUrl:'https://api.example.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.CREDENTIALS_LOGIN_INVALID, b1);
    }

    @Test
    public void buildWithWrongRepoMustFail() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connect("user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHUser user = PowerMockito.mock(GHUser.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(null);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);


        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.INVALID_REPO, b1);
    }

    @Test
    public void buildWithWrongCommitMustFail() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connect("user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenThrow(IOException.class);

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.INVALID_COMMIT, b1);
    }

    @Test
    public void buildWithInferWithoutCommitMustFail() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connect("user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenReturn(null);

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', " +
                        "repo: 'acceptance-test-harness',  " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_COMMIT, b1);
    }

    @Test
    public void buildWithInferWithoutAccountMustFail() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connect("user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenReturn(null);

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify  context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'," +
                        "repo: 'acceptance-test-harness',  " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }

    @Test
    public void buildWithInferWithoutRepoMustFail() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connect("user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenReturn(null);

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify  account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'," +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }

    @Test
    public void buildWithInferWithoutCredentialsMustFail() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connect("user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenReturn(null);

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify  account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "description: 'PCT Is OK', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'," +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com', repo: 'acceptance-test-harness'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }

    @Test
    public void build() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connect("user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        GHCommit commit = PowerMockito.mock(GHCommit.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenReturn(commit);

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.SUCCESS, jenkins.waitForCompletion(b1));
    }

    @Test
    public void buildEnterprise() throws Exception {

        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.mockStatic(GitHub.class);
        PowerMockito.when(GitHub.connectToEnterprise("https://api.example.com","user", "password")).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        GHCommit commit = PowerMockito.mock(GHCommit.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenReturn(commit);

        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'PCT Results', " +
                        "credentialsId: 'dummy', description: 'PCT Is OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com', gitApiUrl:'https://api.example.com'"
        ));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.SUCCESS, jenkins.waitForCompletion(b1));
    }

}
