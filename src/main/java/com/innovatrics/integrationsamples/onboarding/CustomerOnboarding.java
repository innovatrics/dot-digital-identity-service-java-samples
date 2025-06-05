package com.innovatrics.integrationsamples.onboarding;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse;
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
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.ImageCrop;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This example demonstrates comprehensive usage of customer onboarding API. Face and document images are used to create
 * a customer entity. This example can serve as a starting point when integrating with DIS for onboarding use cases.
 */
public class CustomerOnboarding extends CustomerOnboardingApiTest {
    private static final Logger log = LoggerFactory.getLogger(CustomerOnboarding.class);

    public CustomerOnboarding(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the customer onboarding test by creating a customer, evaluating their onboarding
     * process, and then deleting the customer.
     *
     * @throws ApiException if the API request fails or the response indicates an error
     * @throws URISyntaxException if the URI syntax used is incorrect
     * @throws IOException if an I/O error occurs during API requests
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluateCustomerOnboarding(customerId);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    /**
     * Evaluates the customer onboarding process by conducting a series of checks including
     * selfie verification, liveness detection, and document classification. It saves images of
     * the document's front, back, and portrait if the process is successful.
     *
     * @param customerId the unique identifier of the customer being onboarded
     * @throws URISyntaxException if the URI syntax used is incorrect
     * @throws IOException if an I/O error occurs during API requests or image saving
     * @throws ApiException if the API request fails or the response indicates an error
     */
    private void evaluateCustomerOnboarding(String customerId) throws URISyntaxException, IOException, ApiException {
        CreateSelfieResponse selfieResponse = getApi().createSelfie1(customerId, new CreateSelfieRequest().image(new Image().data(getFacesImage("face"))));
        CreateSelfieResponse.ErrorCodeEnum selfieError = selfieResponse.getErrorCode();
        if (selfieError != null) {
            log.error(selfieError.getValue());
            return;
        }
        log.info("Face detected on selfie.");

        getApi().createLiveness(customerId);
        CreateCustomerLivenessSelfieResponse livenessSelfieResponse = getApi().createLivenessSelfie(customerId, new CreateCustomerLivenessSelfieRequest().image(new Image().data(getFacesImage("face"))).assertion(CreateCustomerLivenessSelfieRequest.AssertionEnum.NONE));
        if (livenessSelfieResponse.getWarnings() != null && !livenessSelfieResponse.getWarnings().isEmpty()) {
            for (CreateCustomerLivenessSelfieResponse.WarningsEnum warning : livenessSelfieResponse.getWarnings()) {
                log.warn("Liveness selfie warning: {}", warning.getValue());
            }
            log.error("Liveness selfie does not meet quality required for accurate passive liveness evaluation.");
            return;
        }
        CreateCustomerLivenessSelfieResponse.ErrorCodeEnum livenessSelfieError = livenessSelfieResponse.getErrorCode();
        if (livenessSelfieError != null) {
            log.error(livenessSelfieError.getValue());
            return;
        }
        final EvaluateCustomerLivenessResponse passiveLivenessResponse = getApi().evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.PASSIVE_LIVENESS));
        EvaluateCustomerLivenessResponse.ErrorCodeEnum passiveLivenessError = passiveLivenessResponse.getErrorCode();
        if (passiveLivenessError != null) {
            log.error(passiveLivenessError.getValue());
            return;
        }
        log.info("Passive liveness score: {}", passiveLivenessResponse.getScore());

        getApi().createDocument(customerId, new CreateDocumentRequest().advice(new DocumentAdvice().classification(new DocumentClassificationAdvice().addCountriesItem("INO"))));
        CreateDocumentPageResponse createDocumentResponseFront =
                getApi().createDocumentPage1(customerId, new CreateDocumentPageRequest().image(new Image().data(getL2DocumentImage("document-front"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentFrontError = createDocumentResponseFront.getErrorCode();
        if (documentFrontError != null) {
            log.error(documentFrontError.getValue());
            return;
        }
        log.info("Document classified: {} page type: {}", createDocumentResponseFront.getDocumentType().getType(), createDocumentResponseFront.getPageType());
        CreateDocumentPageResponse createDocumentResponseBack =
                getApi().createDocumentPage1(customerId, new CreateDocumentPageRequest().image(new Image().data(getL2DocumentImage("document-back"))));
        verifyDocumentResponseBack(customerId, createDocumentResponseBack);
    }

    protected void verifyDocumentResponseBack(String customerId, CreateDocumentPageResponse createDocumentResponseBack) throws ApiException, IOException {
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
        saveImage(frontPage.getData(), "onboardingImages","document-front.png");

        ImageCrop backPage = getApi().documentPageCrop(customerId, "back", null, null);
        saveImage(backPage.getData(), "onboardingImages", "document-back.png");

        ImageCrop documentPortrait = getApi().documentPortrait(customerId, null, null);
        saveImage(documentPortrait.getData(), "onboardingImages", "portrait.png");
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new CustomerOnboarding(new Configuration()).test();
    }
}
