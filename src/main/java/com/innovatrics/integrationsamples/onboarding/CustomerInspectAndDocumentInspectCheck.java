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
import com.innovatrics.dot.integrationsamples.disapi.model.CustomerInspectResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentClassificationAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentInspectResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.PageTamperingInspection;
import com.innovatrics.dot.integrationsamples.disapi.model.TextConsistentWith;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * This example demonstrates usage of Customer Inspect and Document Inspect from Onboarding API.
 * Face and document images are used to create a customer entity.
 **/
public class CustomerInspectAndDocumentInspectCheck extends CustomerOnboardingApiTest {
    private static final Logger log = LoggerFactory.getLogger(CustomerInspectAndDocumentInspectCheck.class);

    public CustomerInspectAndDocumentInspectCheck(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customerResponse = getApi().createCustomer();
        String customerId = customerResponse.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluateCheckCustomerInspectAndDocumentInspect(customerId);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    private void evaluateCheckCustomerInspectAndDocumentInspect(String customerId) throws ApiException, URISyntaxException, IOException {
        //For example shortening most of the request for inspect preparation was moved into prepareAllDataForInspect method.
        prepareAllDataForInspect(customerId);

        log.info("Customer Inspect info below:");
        final CustomerInspectResponse customerInspectResponse = getApi().inspect(customerId);

        if (customerInspectResponse.getSelfieInspection() != null) {
            if (customerInspectResponse.getSelfieInspection().getSimilarityWith() != null) {
                if (customerInspectResponse.getSelfieInspection().getSimilarityWith().getDocumentPortrait() != null) {
                    log.info(customerInspectResponse.getSelfieInspection().getSimilarityWith().getDocumentPortrait() ? "Document Portrait is similar to Selfie Photo." : "Document Portrait is NOT similar to Selfie Photo!");
                }
                if (customerInspectResponse.getSelfieInspection().getSimilarityWith().getLivenessSelfies() != null) {
                    log.info(customerInspectResponse.getSelfieInspection().getSimilarityWith().getLivenessSelfies() ? "Liveness Selfie is similar to Selfie Photo." : "Liveness Selfie is NOT similar to Selfie Photo!");
                }
            }

            if (customerInspectResponse.getSelfieInspection().getGenderConsistency() != null) {
                log.info("Gender estimated from selfie is: {}", customerInspectResponse.getSelfieInspection().getGenderEstimate());
                if (Boolean.FALSE.equals(customerInspectResponse.getSelfieInspection().getGenderConsistency().getDocumentPortrait())
                        || Boolean.FALSE.equals(customerInspectResponse.getSelfieInspection().getGenderConsistency().getViz())
                        || Boolean.FALSE.equals(customerInspectResponse.getSelfieInspection().getGenderConsistency().getMrz())) {
                    log.info("Inconsistent gender across customer data has been identified!");
                }
            }
        }

        log.info("Document Inspect info below:");
        final DocumentInspectResponse documentInspectResponse = getApi().documentInspect(customerId);
        if (documentInspectResponse.getExpired() != null) {
            log.info(documentInspectResponse.getExpired() ? "Document is expired!" : "Document is valid.");
        }

        if (documentInspectResponse.getMrzInspection() != null) {
            log.info(documentInspectResponse.getMrzInspection().getValid() ? "Document MRZ is valid." : "Document has invalid MRZ checksums!");
        }

        if (documentInspectResponse.getPortraitInspection() != null) {
            log.info("Gender estimated from document is: {}", documentInspectResponse.getPortraitInspection().getGenderEstimate());
            if (documentInspectResponse.getPortraitInspection().getGenderConsistency() != null
                    && (Boolean.FALSE.equals(documentInspectResponse.getPortraitInspection().getGenderConsistency().getViz())
                    || Boolean.FALSE.equals(documentInspectResponse.getPortraitInspection().getGenderConsistency().getMrz()))) {
                log.info("Inconsistent gender across document data has been identified!");
            }
        }

        if (documentInspectResponse.getVisualZoneInspection() != null && documentInspectResponse.getVisualZoneInspection().getTextConsistency() != null) {
            if (!documentInspectResponse.getVisualZoneInspection().getTextConsistency().getConsistent()) {
                log.info("Document has some inconsistent text on Visual Zone!");
                TextConsistentWith textConsistentWith = documentInspectResponse.getVisualZoneInspection().getTextConsistency().getConsistencyWith();
                if (textConsistentWith != null) {
                    if (textConsistentWith.getMrz() != null) {
                        for (String inconsistentText : textConsistentWith.getMrz().getInconsistentTexts()) {
                            log.info("Document has inconsistent text on Visual Zone and MRZ in fields : {}", inconsistentText);
                        }
                    }
                    if (textConsistentWith.getBarcodes() != null) {
                        for (String inconsistentText : textConsistentWith.getBarcodes().getInconsistentTexts()) {
                            log.info("Document has inconsistent text on Visual Zone and Barcode in fields : {}", inconsistentText);
                        }
                    }
                }
            }
        }

        if (documentInspectResponse.getPageTampering() != null) {
            for (Map.Entry<String, PageTamperingInspection> pageTamperingPage : documentInspectResponse.getPageTampering().entrySet()) {
                if (pageTamperingPage.getValue().getColorProfileChangeDetected() != null && pageTamperingPage.getValue().getColorProfileChangeDetected()) {
                    log.info("Black and white copy of real document has been identified on page: {}!", pageTamperingPage.getKey());
                }
                if (pageTamperingPage.getValue().getLooksLikeScreenshot() != null && pageTamperingPage.getValue().getLooksLikeScreenshot()) {
                    log.info("Screen Attack has been identified on page: {}!", pageTamperingPage.getKey());
                }
            }
        }
    }

    /**
     * ------------------------------------------ Inspect data preparation --------------------------------------------
     */

    private void prepareAllDataForInspect(String customerId) throws ApiException, URISyntaxException, IOException {
        log.info("Data preparation started.");
        CreateSelfieResponse selfieResponse = getApi().createSelfie(customerId, new CreateSelfieRequest().image(new Image().data(getFacesImage("face"))));
        CreateSelfieResponse.ErrorCodeEnum selfieError = selfieResponse.getErrorCode();
        if (selfieError != null) {
            log.error(selfieError.getValue());
        }

        getApi().createLiveness(customerId);
        CreateCustomerLivenessSelfieResponse livenessSelfieResponse =
                getApi().createLivenessSelfie(customerId,
                        createCustomerLivenessSelfieRequest("face", CreateCustomerLivenessSelfieRequest.AssertionEnum.NONE));

        CreateCustomerLivenessSelfieResponse.ErrorCodeEnum livenessSelfieError = livenessSelfieResponse.getErrorCode();
        if (livenessSelfieError != null) {
            log.error(livenessSelfieError.getValue());
        }
        final EvaluateCustomerLivenessResponse passiveLivenessResponse =
                getApi().evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.PASSIVE_LIVENESS));

        EvaluateCustomerLivenessResponse.ErrorCodeEnum passiveLivenessError = passiveLivenessResponse.getErrorCode();
        if (passiveLivenessError != null) {
            log.error(passiveLivenessError.getValue());
        }

        getApi().createDocument(customerId, new CreateDocumentRequest().advice(new DocumentAdvice().classification(new DocumentClassificationAdvice().addCountriesItem("INO"))));
        CreateDocumentPageResponse createDocumentResponseFront =
                getApi().createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getL2DocumentImage("document-front"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentFrontError = createDocumentResponseFront.getErrorCode();
        if (documentFrontError != null) {
            log.error(documentFrontError.getValue());
        }

        CreateDocumentPageResponse createDocumentResponseBack =
                getApi().createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getL2DocumentImage("document-back"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentBackError = createDocumentResponseBack.getErrorCode();
        if (documentBackError != null) {
            log.error(documentBackError.getValue());
        }

        Customer customer = getApi().getCustomer(customerId).getCustomer();
        if (customer == null || customer.getDocument() == null || customer.getDocument().getLinks().getPortrait() == null) {
            log.error("Face not found on document portrait");
        }
        log.info("Data preparation ended.");
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new CustomerInspectAndDocumentInspectCheck(new Configuration()).test();
    }
}
