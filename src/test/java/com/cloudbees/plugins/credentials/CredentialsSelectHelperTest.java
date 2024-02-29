package com.cloudbees.plugins.credentials;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import hudson.model.UnprotectedRootAction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.Matchers;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlIsIndex;
import org.htmlunit.html.HtmlListItem;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.html.HtmlSpan;
import org.htmlunit.html.HtmlTextArea;
import org.htmlunit.javascript.host.xml.FormData;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

public class CredentialsSelectHelperTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();
    private static final HashSet<String> SUBMITTABLE_ELEMENT_NAMES = new HashSet<>(Arrays.asList(HtmlInput.TAG_NAME,
            HtmlButton.TAG_NAME, HtmlSelect.TAG_NAME, HtmlTextArea.TAG_NAME, HtmlIsIndex.TAG_NAME));

    @Test
    public void doAddCredentialsFromPopupWorksAsExpected() throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient()) {
            HtmlPage htmlPage = wc.goTo("credentials-selection");
            HtmlButton addCredentialsButton = htmlPage.querySelector(".credentials-add-menu");
            addCredentialsButton.click();
            HtmlListItem li = htmlPage.querySelector(".credentials-add-menu-items li");
            li.click();
            wc.waitForBackgroundJavaScript(4000);
            HtmlForm form = htmlPage.querySelector("#credentials-dialog-form");

            HtmlInput username = form.querySelector("input[name='_.username']");
            username.setValue("bob");
            HtmlInput password = form.querySelector("input[name='_.password']");
            password.setValue("secret");
            HtmlInput id = form.querySelector("input[name='_.id']");
            id.setValue("test");

            HtmlButton formSubmitButton = htmlPage.querySelector(".jenkins-button[data-id='ok']");
            for (final HtmlElement element : form.getPage().getDocumentElement().getHtmlElementDescendants()) {
                if (SUBMITTABLE_ELEMENT_NAMES.contains(element.getTagName())
                        ) {
                    HtmlForm parent = element.getEnclosingForm();
                    if (parent == form) {
                        System.err.println(element.getTagName()+"#"+element.getAttributeDirect("name"));
                    }
                }
            }
            FormData data = new FormData(form);
            System.out.println(data);
            formSubmitButton.fireEvent("click");
            wc.waitForBackgroundJavaScript(5000);

            // check if credentials were added
            List<UsernamePasswordCredentials> creds = CredentialsProvider.lookupCredentials(UsernamePasswordCredentials.class);
            assertThat(creds, Matchers.hasSize(1));
            UsernamePasswordCredentials cred = creds.get(0);
            assertThat(cred.getUsername(), is("bob"));
            assertThat(cred.getPassword().getPlainText(), is("secret"));
        }
    }

    @TestExtension
    public static class CredentialsSelectionAction implements UnprotectedRootAction {
        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return "credentials-selection";
        }
    }
}
