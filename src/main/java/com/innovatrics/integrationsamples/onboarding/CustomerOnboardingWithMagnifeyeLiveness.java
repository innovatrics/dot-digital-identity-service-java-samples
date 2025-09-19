package com.innovatrics.integrationsamples.onboarding;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessRecordResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentPageResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateDocumentRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateSelfieRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateSelfieResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Customer;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentClassificationAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest.TypeEnum;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.ImageCrop;
import com.innovatrics.dot.integrationsamples.disapi.model.LivenessSelfieOrigin;
import com.innovatrics.integrationsamples.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example demonstrates comprehensive usage of customer onboarding API centered around the Magnifeye liveness.
 */
public class CustomerOnboardingWithMagnifeyeLiveness extends CustomerOnboardingApiTest {

    private static final Logger log = LoggerFactory.getLogger(CustomerOnboardingWithMagnifeyeLiveness.class);

    public CustomerOnboardingWithMagnifeyeLiveness(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the customer onboarding test using Magnifeye liveness.
     * This method performs the following steps:
     * 1. Creates a new customer via API and retrieves the customer's ID.
     * 2. Logs the customer ID for reference.
     * 3. Evaluates the customer onboarding process with Magnifeye liveness.
     * 4. Deletes the customer to clean up after the test, even if the evaluation step fails.
     *
     * @throws ApiException       If an error occurs during API operations for customer creation or deletion.
     * @throws URISyntaxException If there is an error with URI syntax during API interactions.
     * @throws IOException        If an input/output error occurs during the evaluation.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final Configuration configuration = new Configuration();
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluateCustomerOnboardingWithMagnifeyeLiveness(customerId, configuration);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    /**
     * Evaluates customer onboarding using Magnifeye liveness, capturing and verifying various required documents and selfies.
     *
     * @param customerId    The unique identifier of the customer to evaluate.
     * @param configuration The configuration object containing the necessary settings and file locations.
     * @throws URISyntaxException If there is an error with URI syntax when accessing API endpoints.
     * @throws IOException        If there is an I/O error during image loading or saving tasks.
     * @throws ApiException       If there is an error response from the API during customer operations.
     */
    private void evaluateCustomerOnboardingWithMagnifeyeLiveness(String customerId, Configuration configuration) throws URISyntaxException, IOException, ApiException {
        getApi().createLiveness(customerId);

        // create customer liveness record for Magnifeye Liveness
        final File magnifeyeBinaryData = getBinaryFile(configuration.LIVENESS_RECORDS_MAGNIFEYE_LIVENESS_BINARY_FILE);
        final CreateCustomerLivenessRecordResponse livenessRecordResponse = getApi().createLivenessRecord(customerId, magnifeyeBinaryData);
        CreateCustomerLivenessRecordResponse.ErrorCodeEnum livenessRecordError = livenessRecordResponse.getErrorCode();
        if (livenessRecordError != null) {
            log.error(livenessRecordError.getValue());
            return;
        }

        log.info("Customer liveness record for Magnifeye Liveness was successfully created.");

        // evaluate Magnifeye liveness score
        final EvaluateCustomerLivenessResponse magnifeyeLivenessResponse = getApi().evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(TypeEnum.MAGNIFEYE_LIVENESS));
        EvaluateCustomerLivenessResponse.ErrorCodeEnum magnifeyeLivenessError = magnifeyeLivenessResponse.getErrorCode();
        if (magnifeyeLivenessError != null) {
            log.error(magnifeyeLivenessError.getValue());
            return;
        }

        log.info("Customer Magnifeye Liveness score: {}", magnifeyeLivenessResponse.getScore());

        // get Magnifeye Liveness selfie link from the response
        if (livenessRecordResponse.getLinks() == null) {
            throw new ApiException("LivenessRecordResponse does not contain links.");
        }
        final String livenessSelfieLink = livenessRecordResponse.getLinks().getSelfie();

        // create customer selfie using Magnifeye liveness selfie
        final CreateSelfieResponse selfieResponse = getApi().createSelfie(customerId, new CreateSelfieRequest().selfieOrigin(new LivenessSelfieOrigin().link(livenessSelfieLink)));
        CreateSelfieResponse.ErrorCodeEnum createSelfieError = selfieResponse.getErrorCode();
        if (createSelfieError != null) {
            log.error(createSelfieError.getValue());
            return;
        }

        log.info("Successfully created customer selfie from Magnifeye liveness selfie.");

        List<CreateSelfieResponse.WarningsEnum> createSelfieWarnings = selfieResponse.getWarnings();
        if (createSelfieWarnings != null && !createSelfieWarnings.isEmpty()) {
            for (CreateSelfieResponse.WarningsEnum warning : createSelfieWarnings) {
                log.warn("Customer selfie warning: {}", warning.getValue());
            }
        }

        // create document
        getApi().createDocument(customerId, new CreateDocumentRequest().advice(new DocumentAdvice().classification(new DocumentClassificationAdvice().addCountriesItem("INO"))));

        // create document front page
        final CreateDocumentPageResponse createDocumentResponseFront =
                getApi().createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getL2DocumentImage("document-front"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentFrontError = createDocumentResponseFront.getErrorCode();
        if (documentFrontError != null) {
            log.error(documentFrontError.getValue());
            return;
        }
        log.info("Document classified: {} page type: {}", createDocumentResponseFront.getDocumentType().getType(), createDocumentResponseFront.getPageType());

        // create document back page
        final CreateDocumentPageResponse createDocumentResponseBack =
                getApi().createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getL2DocumentImage("document-back"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentBackError = createDocumentResponseBack.getErrorCode();
        if (documentBackError != null) {
            log.error(documentBackError.getValue());
            return;
        }
        log.info("Document classified: {} page type: {}", createDocumentResponseBack.getDocumentType().getType(), createDocumentResponseBack.getPageType());

        Customer customer = getApi().getCustomer(customerId).getCustomer();
        if (customer == null || customer.getDocument() == null || customer.getDocument().getLinks().getPortrait() == null) {
            log.error("Face not found on document portrait");
            return;
        }

        log.info("Customer: {}", customer);

        ImageCrop frontPage = getApi().documentPageCrop(customerId, "front", null, null);
        saveImage(frontPage.getData(), "onboardingImages", "document-front.png");

        ImageCrop backPage = getApi().documentPageCrop(customerId, "back", null, null);
        saveImage(backPage.getData(), "onboardingImages", "document-back.png");

        ImageCrop documentPortrait = getApi().documentPortrait(customerId, null, null);
        saveImage(documentPortrait.getData(), "onboardingImages", "portrait.png");
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new CustomerOnboardingWithMagnifeyeLiveness(new Configuration()).test();
    }
}
