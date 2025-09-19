package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageResponse.WarningsEnum.DOCUMENT_TYPE_NOT_RECOGNIZED;

/**
 * L0DocumentValidation performs the validation of a Level 0 document associated with a customer as part of the customer onboarding process.
 */
public class L0DocumentValidation extends CustomerOnboardingApiTest {
    private static final Logger log = LoggerFactory.getLogger(L0DocumentValidation.class);

    public L0DocumentValidation(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes a test to determine the Document Level.
     * This method performs the following steps:
     * 1. Creates a new customer.
     * 2. Adds pages to the created document using an image.
     * 3. Retrieves the customer details.
     * 4. Validates the document using customer information.
     *
     * @throws ApiException       If there is an error with the API request.
     * @throws URISyntaxException If the URI syntax is incorrect.
     * @throws IOException        If an I/O error occurs.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        getApi().createDocument(customerId, new CreateDocumentRequest());
        CreateDocumentPageResponse createDocumentFrontPageResponse =
                getApi().createDocumentPage(customerId, createDocumentPageRequest(getL0DocumentImage("document-front")));

        // add also back side to read MRZ data
        CreateDocumentPageResponse createDocumentBackPageResponse = getApi().createDocumentPage(customerId, createDocumentPageRequest(getL0DocumentImage("document-back")));

        GetCustomerResponse customer = getApi().getCustomer(customerId);
        validateDocument(createDocumentFrontPageResponse, createDocumentBackPageResponse, customer.getCustomer());
    }

    /**
     * Validates the uploaded document based on its recognition level and type.
     *
     * @param createDocumentFrontPageResponse The response object from creating the first document page.
     * @param createDocumentBackPageResponse  The response object from creating the back page of the document.
     * @param customer                        The customer whose document is being validated.
     */
    private void validateDocument(final CreateDocumentPageResponse createDocumentFrontPageResponse,
                                  final CreateDocumentPageResponse createDocumentBackPageResponse,
                                  Customer customer) {
        if (pageHasNotRecognizedWarning(createDocumentFrontPageResponse)
                && pageHasNotRecognizedWarning(createDocumentBackPageResponse)
                && customer.getDocument() != null
                && (customer.getDocument().getType() == null
                || documentTypeIsNotRecognized(customer.getDocument().getType())
                || documentTypeIsNotSupported(customer.getDocument().getType()))
        ) {
            log.info("The uploaded document Level is 0.");
        } else {
            log.warn("The uploaded document other than Level 0.");
        }
    }

    /**
     * Determines if the provided document type is not supported based on support level attribute.
     *
     * @param documentType The document type to be checked.
     * @return true if the document type support level is equal to NOT_SUPPORTED, false otherwise.
     */
    private boolean documentTypeIsNotSupported(final DocumentType documentType) {
        return documentType != null
                && documentType.getSupportLevel() == DocumentType.SupportLevelEnum.NOT_SUPPORTED;
    }

    /**
     * Determines if the provided document type is not recognized based on its attributes.
     *
     * @param documentType The document type to be checked.
     * @return true if the document type has all null attributes (type, edition, country, and machine-readable travel document), false otherwise.
     */
    private boolean documentTypeIsNotRecognized(final DocumentType documentType) {
        return documentType != null
                && documentType.getType() == null
                && documentType.getEdition() == null
                && documentType.getCountry() == null
                && documentType.getMachineReadableTravelDocument() == null;
    }

    /**
     * Checks if both the front and back pages of the document contain a warning indicating that the document type is not recognized.
     *
     * @param createDocumentResponse The response object from creating the front page of the document.
     * @return true if both the front and back page responses contain a "document type not recognized" warning; false otherwise.
     */
    private boolean pageHasNotRecognizedWarning(final CreateDocumentPageResponse createDocumentResponse) {
        return createDocumentResponse.getWarnings() != null
                && !createDocumentResponse.getWarnings().isEmpty()
                && createDocumentResponse.getWarnings().contains(DOCUMENT_TYPE_NOT_RECOGNIZED);
    }


    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new L0DocumentValidation(new Configuration()).test();
    }
}
