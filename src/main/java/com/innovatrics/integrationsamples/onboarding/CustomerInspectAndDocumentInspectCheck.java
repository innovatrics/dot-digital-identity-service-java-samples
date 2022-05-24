package com.innovatrics.integrationsamples.onboarding;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

/**
 * This example demonstrates usage of Customer Inspect and Document Inspect from Onboarding API.
 * Face and document images are used to create a customer entity.
 **/
public class CustomerInspectAndDocumentInspectCheck {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerInspectAndDocumentInspectCheck.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient();
        client.setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        CustomerOnboardingApi customerOnboardingApi = new CustomerOnboardingApi(client);

        try {
            final CreateCustomerResponse customerResponse = customerOnboardingApi.createCustomer();
            String customerId = customerResponse.getId();
            LOG.info("Customer created with id: " + customerId);

            //For example shortening most of the request for inspect preparation was moved into prepareAllDataForInspect method.
            prepareAllDataForInspect(customerId, customerOnboardingApi);

            LOG.info("\nCustomer Inspect info below:");
            final CustomerInspectResponse customerInspectResponse = customerOnboardingApi.inspect(customerId);

            if (customerInspectResponse.getSelfieInspection() != null) {
                if (customerInspectResponse.getSelfieInspection().getSimilarityWith() != null) {
                    if (customerInspectResponse.getSelfieInspection().getSimilarityWith().getDocumentPortrait() != null) {
                        LOG.info(customerInspectResponse.getSelfieInspection().getSimilarityWith().getDocumentPortrait() ? "Document Portrait is similar to Selfie Photo." : "Document Portrait is NOT similar to Selfie Photo!");
                    }
                    if (customerInspectResponse.getSelfieInspection().getSimilarityWith().getLivenessSelfies() != null) {
                        LOG.info(customerInspectResponse.getSelfieInspection().getSimilarityWith().getLivenessSelfies() ? "Liveness Selfie is similar to Selfie Photo." : "Liveness Selfie is NOT similar to Selfie Photo!");
                    }
                }

                if (customerInspectResponse.getSelfieInspection().getGenderConsistency() != null) {
                    LOG.info("Gender estimated from selfie is: " + customerInspectResponse.getSelfieInspection().getGenderEstimate());
                    if (customerInspectResponse.getSelfieInspection().getGenderConsistency().getDocumentPortrait() == false || customerInspectResponse.getSelfieInspection().getGenderConsistency().getViz() == false || customerInspectResponse.getSelfieInspection().getGenderConsistency().getMrz() == false) {
                        LOG.info("Inconsistent gender across customer data has been identified!");
                    }
                }
            }

            LOG.info("\nDocument Inspect info below:");
            final DocumentInspectResponse documentInspectResponse = customerOnboardingApi.documentInspect(customerId);
            if (documentInspectResponse.getExpired() != null) {
                LOG.info(documentInspectResponse.getExpired() ? "Document is expired!" : "Document is valid.");
            }

            if (documentInspectResponse.getMrzInspection() != null) {
                LOG.info(documentInspectResponse.getMrzInspection().getValid() ? "Document MRZ is valid." : "Document has invalid MRZ checksums!");
            }

            if (documentInspectResponse.getPortraitInspection() != null) {
                LOG.info("Gender estimated from document is: " + documentInspectResponse.getPortraitInspection().getGenderEstimate());
                if (documentInspectResponse.getPortraitInspection().getGenderConsistency() != null) {
                    if (documentInspectResponse.getPortraitInspection().getGenderConsistency().getViz() != null) {
                        if (documentInspectResponse.getPortraitInspection().getGenderConsistency().getViz() == false || documentInspectResponse.getPortraitInspection().getGenderConsistency().getMrz() == false) {
                            LOG.info("Inconsistent gender across document data has been identified!");
                        }
                    }
                }
            }

            if (documentInspectResponse.getVisualZoneInspection() != null && documentInspectResponse.getVisualZoneInspection().getTextConsistency() != null) {
                if (documentInspectResponse.getVisualZoneInspection().getTextConsistency().getConsistent() == false) {
                    LOG.info("Document has some inconsistent text on Visual Zone!");
                    TextConsistentWith textConsistentWith = documentInspectResponse.getVisualZoneInspection().getTextConsistency().getConsistencyWith();
                    if (textConsistentWith != null) {
                        if (textConsistentWith.getMrz() != null) {
                            for (String inconsistentText : textConsistentWith.getMrz().getInconsistentTexts()) {
                                LOG.info("Document has inconsistent text on Visual Zone and MRZ in fields : " + inconsistentText);
                            }
                        }
                        if (textConsistentWith.getBarcodes() != null) {
                            for (String inconsistentText : textConsistentWith.getBarcodes().getInconsistentTexts()) {
                                LOG.info("Document has inconsistent text on Visual Zone and Barcode in fields : " + inconsistentText);
                            }
                        }
                    }
                }
            }

            if (documentInspectResponse.getPageTampering() != null) {
                for (Map.Entry<String, PageTamperingInspection> pageTamperingPage : documentInspectResponse.getPageTampering().entrySet()) {
                    if (pageTamperingPage.getValue().getColorProfileChangeDetected() != null && pageTamperingPage.getValue().getColorProfileChangeDetected()) {
                        LOG.info("Black and white copy of real document has been identified on page: " + pageTamperingPage.getKey() + "!");
                    }
                    if (pageTamperingPage.getValue().getLooksLikeScreenshot() != null && pageTamperingPage.getValue().getLooksLikeScreenshot()) {
                        LOG.info("Screen Attack has been identified on page: " + pageTamperingPage.getKey() + "!");
                    }
                }
            }

            LOG.info("Deleting customer with id: " + customerId);
            customerOnboardingApi.delete(customerId);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    /**
     * ------------------------------------------ Inspect data preparation --------------------------------------------
     */

    private static void prepareAllDataForInspect(String customerId, CustomerOnboardingApi customerOnboardingApi) throws ApiException, URISyntaxException, IOException {
        LOG.info("Data preparation started.");
        CreateSelfieResponse selfieResponse = customerOnboardingApi.createSelfie(customerId, new CreateSelfieRequest().image(new Image().data(getDetectionImage())));
        CreateSelfieResponse.ErrorCodeEnum selfieError = selfieResponse.getErrorCode();
        if (selfieError != null) {
            LOG.error(selfieError.getValue());
        }

        customerOnboardingApi.createLiveness(customerId);
        CreateCustomerLivenessSelfieResponse livenessSelfieResponse = customerOnboardingApi.createLivenessSelfie(customerId, new CreateCustomerLivenessSelfieRequest().image(new Image().data(getDetectionImage())).assertion(CreateCustomerLivenessSelfieRequest.AssertionEnum.NONE));

        CreateCustomerLivenessSelfieResponse.ErrorCodeEnum livenessSelfieError = livenessSelfieResponse.getErrorCode();
        if (livenessSelfieError != null) {
            LOG.error(livenessSelfieError.getValue());
        }
        final EvaluateCustomerLivenessResponse passiveLivenessResponse = customerOnboardingApi.evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.PASSIVE_LIVENESS));
        EvaluateCustomerLivenessResponse.ErrorCodeEnum passiveLivenessError = passiveLivenessResponse.getErrorCode();
        if (passiveLivenessError != null) {
            LOG.error(passiveLivenessError.getValue());
        }

        customerOnboardingApi.createDocument(customerId, new CreateDocumentRequest().advice(new DocumentAdvice().classification(new DocumentClassificationAdvice().addCountriesItem("INO"))));
        CreateDocumentPageResponse createDocumentResponseFront = customerOnboardingApi.createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getDocumentImage("document-front"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentFrontError = createDocumentResponseFront.getErrorCode();
        if (documentFrontError != null) {
            LOG.error(documentFrontError.getValue());
        }

        CreateDocumentPageResponse createDocumentResponseBack = customerOnboardingApi.createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getDocumentImage("document-back"))));
        CreateDocumentPageResponse.ErrorCodeEnum documentBackError = createDocumentResponseBack.getErrorCode();
        if (documentBackError != null) {
            LOG.error(documentBackError.getValue());
        }

        Customer customer = customerOnboardingApi.getCustomer(customerId).getCustomer();
        if (customer == null || customer.getDocument() == null || customer.getDocument().getLinks().getPortrait() == null) {
            LOG.error("Face not found on document portrait");
        }
        LOG.info("Data preparation ended.");
    }

    /**
     * ------------------------------------------------ Helper methods ------------------------------------------------
     */

    private static byte[] getDocumentImage(String imageId) throws URISyntaxException, IOException {
        final URL resource = CustomerInspectAndDocumentInspectCheck.class.getClassLoader().getResource("images/documents/" + imageId + ".jpeg");
        return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
    }

    private static byte[] getDetectionImage() throws URISyntaxException, IOException {
        final URL resource = CustomerInspectAndDocumentInspectCheck.class.getClassLoader().getResource("images/faces/face.jpeg");
        return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
    }

}
