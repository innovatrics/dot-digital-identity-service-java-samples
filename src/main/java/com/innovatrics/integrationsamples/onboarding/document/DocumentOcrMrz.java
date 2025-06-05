package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentRequest;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;


/**
 * The DocumentOcrMrz class is responsible for executing a test process related to document OCR (Optical
 * Character Recognition) using MRZ (Machine Readable Zone) data.
 */
public class DocumentOcrMrz extends CustomerOnboardingApiTest {
    private static final Logger log = LoggerFactory.getLogger(DocumentOcrMrz.class);

    public DocumentOcrMrz(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes a document OCR MRZ test process, which involves the following steps:
     * 1. Create a customer.
     * 2. Create a document for the customer with MRZ sources.
     * 3. Create a document page with an image.
     * 4. Check for errors in the document creation process.
     * 5. Retrieve and log customer details.
     * 6. Delete the customer.
     *
     * @throws ApiException If there is an error with the API calls.
     * @throws URISyntaxException If there is a URI syntax error.
     * @throws IOException If there is an I/O error during processing.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        getApi().createDocument(customerId, new CreateDocumentRequest().addSourcesItem(CreateDocumentRequest.SourcesEnum.MRZ));
        CreateDocumentPageResponse createDocumentResponse =
                getApi().createDocumentPage1(customerId, createDocumentPageRequest(getL2DocumentImage("document-back")));

        checkDocumentResponseAndDeleteCustomer(createDocumentResponse, customerId);
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new DocumentOcrMrz(new Configuration()).test();
    }
}
