package com.innovatrics.integrationsamples.testhelper;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class CustomerOnboardingApiTest extends BaseApiTest<CustomerOnboardingApi> {
    private static final Logger log = LoggerFactory.getLogger(CustomerOnboardingApiTest.class);

    public CustomerOnboardingApiTest(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Checks the document response for the specified customer, logs the Customer and deletes the customer if no errors are found.
     *
     * @param createDocumentResponse the response object containing the result of the document creation process
     * @param customerId the ID of the customer to be deleted if no errors are present in the document response
     * @throws ApiException if an error occurs during API calls for retrieving or deleting the customer
     */
    protected void checkDocumentResponseAndDeleteCustomer(CreateDocumentPageResponse createDocumentResponse, String customerId) throws ApiException {
        checkDocumentResponseThrowsWhenError(createDocumentResponse);

        getCustomerByIdAndLog(customerId);
        deleteCustomerWithId(customerId);
    }

    /**
     * Checks the document response for any errors and throws an ApiException if an error is present.
     *
     * @param createDocumentResponse the response object containing the result of the document creation process
     * @throws ApiException if an error is present in the document response
     */
    protected void checkDocumentResponseThrowsWhenError(CreateDocumentPageResponse createDocumentResponse) throws ApiException {
        CreateDocumentPageResponse.ErrorCodeEnum documentError = createDocumentResponse.getErrorCode();
        if (documentError != null) {
            throw new ApiException(documentError.getValue());
        }
    }

    /**
     * Deletes a customer with the specified ID.
     *
     * @param customerId the ID of the customer to delete
     * @throws ApiException if there is an error during the API call
     */
    protected void deleteCustomerWithId(String customerId) throws ApiException {
        log.info("Deleting customer with id: {}", customerId);
        getApi().deleteCustomer(customerId);
    }

    /**
     * Retrieves a customer by ID and logs the customer information.
     *
     * @param customerId the ID of the customer to retrieve
     * @throws ApiException if there is an error during the API call
     */
    protected void getCustomerByIdAndLog(String customerId) throws ApiException {
        Customer customer = getApi().getCustomer(customerId).getCustomer();
        log.info("Customer: {}", customer);
    }

    /**
     * Creates a CreateDocumentPageRequest instance with the provided image data.
     *
     * @param image the byte array representing the image to be included in the request
     * @return a CreateDocumentPageRequest object populated with the provided image data
     */
    protected CreateDocumentPageRequest createDocumentPageRequest(byte[] image) {
        return new CreateDocumentPageRequest()
                .image(new Image().data(image));
    }

    /**
     * Creates a new CreateCustomerLivenessSelfieRequest using the specified file name.
     *
     * @param fileName the name of the file to be used in the request
     * @return a CreateCustomerLivenessSelfieRequest object populated with the image data
     * @throws URISyntaxException if there is an issue with the syntax of a URI
     * @throws IOException if an I/O error occurs
     */
    protected CreateCustomerLivenessSelfieRequest createCustomerLivenessSelfieRequest(final String fileName, CreateCustomerLivenessSelfieRequest.AssertionEnum segmentPlacement) throws URISyntaxException, IOException {
        return new CreateCustomerLivenessSelfieRequest()
                .image(new Image().data(getFacesImage(fileName)))
                .assertion(segmentPlacement);
    }
}
