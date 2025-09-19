package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentPageQuality;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * L2DocumentQuality is a test class to perform tests
 * related to the document quality verification process during customer onboarding.
 * Level_2 documents are trained documents. Metadata and models are available
 */
public class DocumentQuality extends CustomerOnboardingApiTest {
    private static final Logger log = LoggerFactory.getLogger(DocumentQuality.class);

    public DocumentQuality(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the test that involves creating a customer, verifying the quality of a document, and then deleting the customer.
     *
     * @throws ApiException if an API-related error occurs during the test
     * @throws URISyntaxException if the URI for the API request is malformed
     * @throws IOException if an input or output exception occurs
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            verifyDocumentQuality(customerId);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    /**
     * Verifies the quality of a document for a given customer. The method attempts to create a document,
     * create a document page, and then evaluate if the document page is of acceptable quality.
     *
     * @param customerId the unique identifier of the customer whose document quality is to be verified
     * @throws ApiException if there's an error related to API operations
     * @throws URISyntaxException if there's an error with the URI syntax
     * @throws IOException if an IO error occurs during the operation
     */
    private void verifyDocumentQuality(String customerId) throws ApiException, URISyntaxException, IOException {
        getApi().createDocument(customerId, new CreateDocumentRequest());
        CreateDocumentPageResponse createDocumentResponse =
                getApi().createDocumentPage(customerId, createDocumentPageRequest(getL2DocumentImage("document-front")));

        checkDocumentResponseThrowsWhenError(createDocumentResponse);

        DocumentPageQuality pageQuality = getApi().documentPageQuality(customerId, "front");
        if (pageQuality.getFine()) {
            log.info("Document processed successfully.");
            if (pageQuality.getWarnings() != null) {
                for (DocumentPageQuality.WarningsEnum warning : pageQuality.getWarnings()) {
                    log.warn("Document quality warning: {}", warning.getValue());
                }
            }
        } else {
            log.error("Document processing failed with errors.");
            if (pageQuality.getIssues() != null) {
                for (DocumentPageQuality.IssuesEnum issue : pageQuality.getIssues()) {
                    log.error("Document quality issue: {}", issue.getValue());
                }
            }
            throw new ApiException("Document quality issue.");
        }
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new DocumentQuality(new Configuration()).test();
    }
}
