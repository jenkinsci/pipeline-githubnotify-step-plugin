package org.jenkinsci.plugins.pipeline.githubstatusnotification;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
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
import org.kohsuke.github.GitHubBuilder;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.Proxy;

import static org.mockito.Matchers.anyString;

@RunWith (PowerMockRunner.class)
@PrepareForTest ({GitHubStatusNotificationStep.class})
@PowerMockIgnore ({"javax.crypto.*" })
public class GitHubNotificationPipelineStepTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void buildWithNullCredentialsIDMustFail() throws Exception {
        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "description: 'All tests are OK', repo: 'acceptance-test-harness', " +
                        "sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', status: 'SUCCESS', " +
                        "targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }


    @Test
    public void buildWithNotExistingCredentialsMustFail() throws Exception {
                        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
                p.setDefinition(new CpsFlowDefinition(
                        "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                                "credentialsId: 'notExisting', description: 'All tests are OK', " +
                                "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                                "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                        true));
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
        "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
        "credentialsId: 'dummy', description: 'All tests are OK', " +
        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
        true));
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
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com', gitApiUrl:'https://api.example.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.CREDENTIALS_LOGIN_INVALID, b1);
    }

    @Test
    public void buildWithWrongRepoMustFail() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHUser user = PowerMockito.mock(GHUser.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(null);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);


        Credentials dummy = new DummyCredentials(CredentialsScope.GLOBAL, "user", "password");
        SystemCredentialsProvider.getInstance().getCredentials().add(dummy);

        WorkflowJob p = jenkins.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.INVALID_REPO, b1);
    }

    @Test
    public void buildWithWrongCommitMustFail() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
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
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.INVALID_COMMIT, b1);
    }

    @Test
    public void buildWithInferWithoutCommitMustFail() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
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
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', " +
                        "repo: 'acceptance-test-harness',  " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_COMMIT, b1);
    }

    @Test
    public void buildWithInferWithoutAccountMustFail() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
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
                "githubNotify  context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'," +
                        "repo: 'acceptance-test-harness',  " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }

    @Test
    public void buildWithInferWithoutRepoMustFail() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
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
                "githubNotify  account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'," +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }

    @Test
    public void buildWithInferWithoutCredentialsMustFail() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
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
                "githubNotify  account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "description: 'All tests are OK', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'," +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com', repo: 'acceptance-test-harness'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.FAILURE, jenkins.waitForCompletion(b1));
        jenkins.assertLogContains(GitHubStatusNotificationStep.Execution.UNABLE_TO_INFER_DATA, b1);
    }

    @Test
    public void build() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
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
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.SUCCESS, jenkins.waitForCompletion(b1));
    }

    @Test
    public void buildWithFolderCredentials() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
        PowerMockito.when(gh.isCredentialValid()).thenReturn(true);
        GHRepository repo = PowerMockito.mock(GHRepository.class);
        GHUser user = PowerMockito.mock(GHUser.class);
        GHCommit commit = PowerMockito.mock(GHCommit.class);
        PowerMockito.when(user.getRepository(anyString())).thenReturn(repo);
        PowerMockito.when(gh.getUser(anyString())).thenReturn(user);
        PowerMockito.when((repo.getCommit(anyString()))).thenReturn(commit);

        Folder f = jenkins.jenkins.createProject(Folder.class, "folder" + jenkins.jenkins.getItems().size());
        CredentialsStore folderStore = getFolderStore(f);
        folderStore.addCredentials(Domain.global(),
                new DummyCredentials(CredentialsScope.GLOBAL, "user", "password"));

        WorkflowJob p = f.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition(
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.SUCCESS, jenkins.waitForCompletion(b1));
    }

    @Test
    public void buildEnterprise() throws Exception {

        GitHubBuilder ghb = PowerMockito.mock(GitHubBuilder.class);
        PowerMockito.when(ghb.withProxy(Matchers.<Proxy>anyObject())).thenReturn(ghb);
        PowerMockito.when(ghb.withOAuthToken(anyString(), anyString())).thenReturn(ghb);
        PowerMockito.when(ghb.withEndpoint("https://api.example.com")).thenReturn(ghb);
        PowerMockito.whenNew(GitHubBuilder.class).withNoArguments().thenReturn(ghb);
        GitHub gh = PowerMockito.mock(GitHub.class);
        PowerMockito.when(ghb.build()).thenReturn(gh);
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
                "githubNotify account: 'raul-arabaolaza', context: 'ATH Results', " +
                        "credentialsId: 'dummy', description: 'All tests are OK', " +
                        "repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487', " +
                        "status: 'SUCCESS', targetUrl: 'http://www.cloudbees.com', gitApiUrl:'https://api.example.com'",
                true));
        WorkflowRun b1 = p.scheduleBuild2(0).waitForStart();
        jenkins.assertBuildStatus(Result.SUCCESS, jenkins.waitForCompletion(b1));
    }

    private CredentialsStore getFolderStore(Folder f) {
        Iterable<CredentialsStore> stores = CredentialsProvider.lookupStores(f);
        CredentialsStore folderStore = null;
        for (CredentialsStore s : stores) {
            if (s.getProvider() instanceof FolderCredentialsProvider && s.getContext() == f) {
                folderStore = s;
                break;
            }
        }
        return folderStore;
    }


}
