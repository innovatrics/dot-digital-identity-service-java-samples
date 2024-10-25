package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CustomerOnboardingApi;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentPageQuality;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;


public class DocumentQuality {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentQuality.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final CustomerOnboardingApi customerOnboardingApi = new CustomerOnboardingApi(client);

        try {
            final CreateCustomerResponse customerResponse = customerOnboardingApi.createCustomer();
            String customerId = customerResponse.getId();
            LOG.info("Customer created with id: " + customerId);

            customerOnboardingApi.createDocument(customerId, new CreateDocumentRequest());
            CreateDocumentPageResponse createDocumentResponse = customerOnboardingApi.createDocumentPage1(customerId, new CreateDocumentPageRequest().image(new Image().data(getDocumentImage("document-front"))));
            CreateDocumentPageResponse.ErrorCodeEnum documentError = createDocumentResponse.getErrorCode();
            if (documentError != null) {
                LOG.error(documentError.getValue());
                return;
            }
            DocumentPageQuality pageQuality = customerOnboardingApi.documentPageQuality(customerId, "front");
            if (pageQuality.getFine()) {
                LOG.info("Document processed successfully.");
                if (pageQuality.getWarnings() != null) {
                    for (DocumentPageQuality.WarningsEnum warning : pageQuality.getWarnings()) {
                        LOG.warn("Document quality warning: " + warning.getValue());
                    }
                }
            } else {
                LOG.error("Document processing failed with errors.");
                if (pageQuality.getIssues() != null) {
                    for (DocumentPageQuality.IssuesEnum issue : pageQuality.getIssues()) {
                        LOG.error("Document quality issue: " + issue.getValue());
                    }
                }
                return;
            }

            LOG.info("Deleting customer with id: " + customerId);
            customerOnboardingApi.deleteCustomer(customerId);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    private static byte[] getDocumentImage(String imageId) throws URISyntaxException, IOException {
        final URL resource = DocumentQuality.class.getClassLoader().getResource("images/documents/" + imageId + ".jpeg");
        return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
    }
}
