package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;


/**
 * L1DocumentValidation performs the validation of a Level 1 document associated with a customer as part of the customer onboarding process.
 */
public class L1DocumentValidation extends CustomerOnboardingApiTest {
    private static final Logger log = LoggerFactory.getLogger(L1DocumentValidation.class);

    public L1DocumentValidation(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the test process for validating a document associated with a customer.
     * This method performs the following steps:
     * 1. Creates a new customer.
     * 2. Creates a document for the customer.
     * 3. Creates front and back pages for the document using images.
     * 4. Retrieves the customer details.
     * 5. Validates the created document against the customer details.
     *
     * @throws ApiException       If an API error occurs during any of the API calls.
     * @throws URISyntaxException If there is an error in the URI syntax.
     * @throws IOException        If an I/O error occurs during any of the operations.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        getApi().createDocument(customerId, new CreateDocumentRequest());
        CreateDocumentPageResponse responseFront =
                getApi().createDocumentPage1(customerId, createDocumentPageRequest(getL1DocumentImage("document-front")));
        // add also back side to read MRZ data
        getApi().createDocumentPage1(customerId, createDocumentPageRequest(getL1DocumentImage("document-back")));

        GetCustomerResponse customer = getApi().getCustomer(customerId);
        assert customer.getCustomer() != null;
        validateDocument(customer.getCustomer());
    }

    /**
     * Validates the document associated with the given customer.
     *
     * @param customer the customer whose document is to be validated
     */
    private void validateDocument(Customer customer) {
        if (documentHasMrz(customer.getDocument()) && documentTypeHasOnlyTravelType(customer.getDocument().getType())) {
            log.info("The uploaded document Level is 1.");
        } else {
            log.info("The uploaded document other than Level 1.");
        }
    }

    /**
     * Checks if the given customer document contains a Machine Readable Zone (MRZ).
     *
     * @param document the CustomerDocument object to be evaluated
     * @return true if the document contains an MRZ; false otherwise
     */
    private boolean documentHasMrz(final CustomerDocument document) {
        return document != null && document.getMrz() != null;
    }

    /**
     * Checks if the given DocumentType has only the Machine Readable Travel Document (MRTD) information.
     *
     * @param documentType the DocumentType object to be evaluated
     * @return true if the DocumentType contains only the MRTD; false otherwise
     */
    private boolean documentTypeHasOnlyTravelType(final DocumentType documentType) {
        return documentType != null
                && documentType.getType() == null
                && documentType.getEdition() == null
                && documentType.getCountry() == null
                && documentType.getMachineReadableTravelDocument() != null;
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new L1DocumentValidation(new Configuration()).test();
    }
}
