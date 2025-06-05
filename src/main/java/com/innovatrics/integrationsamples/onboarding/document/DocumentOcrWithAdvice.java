package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentClassificationAdvice;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;


/**
 * This class handles the creation of a customer, create a document with
 * specific advice, create document page with an image, and performs
 * validation by checking the response and cleaning up by deleting the customer.
 */
public class DocumentOcrWithAdvice extends CustomerOnboardingApiTest {
    private static final Logger log = LoggerFactory.getLogger(DocumentOcrWithAdvice.class);
    private static final String ID_TYPE = "identity-card";
    private static final String COUNTRY = "INO";

    public DocumentOcrWithAdvice(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the test scenario of creating a customer, adding a document with advice,
     * creating a document page with an image, and performing validation and cleanup.
     *
     * @throws ApiException if an error occurs while interacting with the API
     * @throws URISyntaxException if there is an issue with URI syntax
     * @throws IOException if an I/O exception occurs
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        getApi().createDocument(customerId,
                new CreateDocumentRequest()
                        .advice(new DocumentAdvice().classification(
                                new DocumentClassificationAdvice().addCountriesItem(COUNTRY).addTypesItem(ID_TYPE))));

        CreateDocumentPageResponse createDocumentResponse =
                getApi().createDocumentPage1(customerId, createDocumentPageRequest(getL2DocumentImage("document-front")));

        checkDocumentResponseAndDeleteCustomer(createDocumentResponse, customerId);
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new DocumentOcrWithAdvice(new Configuration()).test();
    }
}
