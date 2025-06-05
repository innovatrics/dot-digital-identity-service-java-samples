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
import com.innovatrics.dot.integrationsamples.disapi.model.CreateSessionRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateSessionResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Customer;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentClassificationAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.ImageCrop;
import com.innovatrics.dot.integrationsamples.disapi.model.SessionManagementApi;
import com.innovatrics.integrationsamples.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;

import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example demonstrates comprehensive usage of customer onboarding API using request sessions.
 */
public class CustomerOnboardingWithSession extends CustomerOnboardingApiTest {

    private static final Logger log = LoggerFactory.getLogger(CustomerOnboardingWithSession.class);

    public CustomerOnboardingWithSession(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final SessionManagementApi sessionManagementApi = new SessionManagementApi(apiClient);

        // create session and retrieve session token from the response
        final CreateSessionResponse sessionResponse = sessionManagementApi.createSession(new CreateSessionRequest().timeout(600));
        final String sessionToken = sessionResponse.getToken();
        log.info("Session successfully created.");

        // every subsequent request must have session token provided in the header
        apiClient.addDefaultHeader("x-inn-session-token", sessionToken);
        final CreateCustomerResponse customerResponse = getApi().createCustomer();

        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluateCustomerOnboardingWithSession(sessionToken, customerId);
        } finally {
            deleteCustomerWithId(customerId);
            log.info("Deleting session");
            sessionManagementApi.deleteSession(sessionToken);
        }
    }

    private void evaluateCustomerOnboardingWithSession(String sessionToken, String customerId) throws ApiException, URISyntaxException, IOException {
        CreateSelfieResponse selfieResponse = getApi().createSelfie1(customerId, new CreateSelfieRequest().image(new Image().data(getFacesImage("face"))));
        CreateSelfieResponse.ErrorCodeEnum selfieError = selfieResponse.getErrorCode();
        if (selfieError != null) {
            log.error(selfieError.getValue());
            return;
        }
        log.info("Face detected on selfie.");

        getApi().createLiveness(customerId);
        CreateCustomerLivenessSelfieResponse livenessSelfieResponse =
                getApi().createLivenessSelfie(customerId,
                        createCustomerLivenessSelfieRequest("face", CreateCustomerLivenessSelfieRequest.AssertionEnum.NONE));

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
        final EvaluateCustomerLivenessResponse passiveLivenessResponse =
                getApi().evaluateLiveness(customerId,
                        new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.PASSIVE_LIVENESS));
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

        if (createDocumentResponseFront.getDocumentType() == null) {
            throw new ApiException("DocumentResponseFront is not contains document type");
        }

        log.info("Document classified: {} page type: {}", createDocumentResponseFront.getDocumentType().getType(), createDocumentResponseFront.getPageType());
        CreateDocumentPageResponse createDocumentResponseBack =
                getApi().createDocumentPage1(customerId, new CreateDocumentPageRequest().image(new Image().data(getL2DocumentImage("document-back"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentBackError = createDocumentResponseBack.getErrorCode();
        if (documentBackError != null) {
            log.error(documentBackError.getValue());
            return;
        }

        if (createDocumentResponseBack.getDocumentType() == null) {
            throw new ApiException("DocumentResponseFront is not contains document type");
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
        new CustomerOnboardingWithSession(new Configuration()).test();
    }
}
