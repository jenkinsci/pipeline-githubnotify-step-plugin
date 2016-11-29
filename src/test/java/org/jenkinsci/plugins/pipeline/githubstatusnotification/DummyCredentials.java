package org.jenkinsci.plugins.pipeline.githubstatusnotification;

import com.cloudbees.plugins.credentials.BaseCredentials;
import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

public class DummyCredentials extends BaseCredentials implements UsernamePasswordCredentials, IdCredentials {

    private final String username;

    private final Secret password;

    @DataBoundConstructor
    public DummyCredentials(CredentialsScope scope, String username, String password) {
        super(scope);
        this.username = username;
        this.password = Secret.fromString(password);
    }

    public String getUsername() {
        return username;
    }

    @NonNull
    public Secret getPassword() {
        return password;
    }

    @NonNull
    @Override
    public String getId() {
        return "dummy";
    }

    @NonNull
    @Override
    public CredentialsDescriptor getDescriptor() {
        return null;
    }

    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        public DescriptorImpl() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "Dummy Credentials";
        }
    }
}

