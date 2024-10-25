package com.innovatrics.integrationsamples.onboarding;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
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
import com.innovatrics.dot.integrationsamples.disapi.model.CustomerOnboardingApi;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.DocumentClassificationAdvice;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.ImageCrop;
import com.innovatrics.integrationsamples.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

/**
 * This example demonstrates comprehensive usage of customer onboarding API. Face and document images are used to create
 * a customer entity. This example can serve as a starting point when integrating with DIS for onboarding use cases.
 */
public class CustomerOnboarding {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerOnboarding.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final CustomerOnboardingApi customerOnboardingApi = new CustomerOnboardingApi(client);

        try {
            final CreateCustomerResponse customerResponse = customerOnboardingApi.createCustomer();
            String customerId = customerResponse.getId();
            LOG.info("Customer created with id: " + customerId);

            CreateSelfieResponse selfieResponse = customerOnboardingApi.createSelfie1(customerId, new CreateSelfieRequest().image(new Image().data(getDetectionImage())));
            CreateSelfieResponse.ErrorCodeEnum selfieError = selfieResponse.getErrorCode();
            if (selfieError != null) {
                LOG.error(selfieError.getValue());
                return;
            }
            LOG.info("Face detected on selfie.");

            customerOnboardingApi.createLiveness(customerId);
            CreateCustomerLivenessSelfieResponse livenessSelfieResponse = customerOnboardingApi.createLivenessSelfie(customerId, new CreateCustomerLivenessSelfieRequest().image(new Image().data(getDetectionImage())).assertion(CreateCustomerLivenessSelfieRequest.AssertionEnum.NONE));
            if (livenessSelfieResponse.getWarnings() != null) {
                for (CreateCustomerLivenessSelfieResponse.WarningsEnum warning : livenessSelfieResponse.getWarnings()) {
                    LOG.warn("Liveness selfie warning: " + warning.getValue());
                }
                LOG.error("Liveness selfie does not meet quality required for accurate passive liveness evaluation.");
                return;
            }
            CreateCustomerLivenessSelfieResponse.ErrorCodeEnum livenessSelfieError = livenessSelfieResponse.getErrorCode();
            if (livenessSelfieError != null) {
                LOG.error(livenessSelfieError.getValue());
                return;
            }
            final EvaluateCustomerLivenessResponse passiveLivenessResponse = customerOnboardingApi.evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.PASSIVE_LIVENESS));
            EvaluateCustomerLivenessResponse.ErrorCodeEnum passiveLivenessError = passiveLivenessResponse.getErrorCode();
            if (passiveLivenessError != null) {
                LOG.error(passiveLivenessError.getValue());
                return;
            }
            LOG.info("Passive liveness score: " + passiveLivenessResponse.getScore());

            customerOnboardingApi.createDocument(customerId, new CreateDocumentRequest().advice(new DocumentAdvice().classification(new DocumentClassificationAdvice().addCountriesItem("INO"))));
            CreateDocumentPageResponse createDocumentResponseFront = customerOnboardingApi.createDocumentPage1(customerId, new CreateDocumentPageRequest().image(new Image().data(getDocumentImage("document-front"))));
            CreateDocumentPageResponse.ErrorCodeEnum documentFrontError = createDocumentResponseFront.getErrorCode();
            if (documentFrontError != null) {
                LOG.error(documentFrontError.getValue());
                return;
            }
            LOG.info("Document classified: " + createDocumentResponseFront.getDocumentType().getType() + " page type: " + createDocumentResponseFront.getPageType());
            CreateDocumentPageResponse createDocumentResponseBack = customerOnboardingApi.createDocumentPage1(customerId, new CreateDocumentPageRequest().image(new Image().data(getDocumentImage("document-back"))));
            CreateDocumentPageResponse.ErrorCodeEnum documentBackError = createDocumentResponseBack.getErrorCode();
            if (documentBackError != null) {
                LOG.error(documentBackError.getValue());
                return;
            }
            LOG.info("Document classified: " + createDocumentResponseBack.getDocumentType().getType() + " page type: " + createDocumentResponseBack.getPageType());

            Customer customer = customerOnboardingApi.getCustomer(customerId).getCustomer();
            if (customer == null || customer.getDocument() == null || customer.getDocument().getLinks().getPortrait() == null) {
                LOG.error("Face not found on document portrait");
                return;
            }

            LOG.info("Customer: " + customer);

            ImageCrop frontPage = customerOnboardingApi.documentPageCrop(customerId, "front", null, null);
            saveImage(frontPage.getData(), "document-front.png");

            ImageCrop backPage = customerOnboardingApi.documentPageCrop(customerId, "back", null, null);
            saveImage(backPage.getData(), "document-back.png");

            ImageCrop documentPortrait = customerOnboardingApi.documentPortrait(customerId, null, null);
            saveImage(documentPortrait.getData(), "portrait.png");

            LOG.info("Deleting customer with id: " + customerId);
            customerOnboardingApi.deleteCustomer(customerId);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    private static byte[] getDocumentImage(String imageId) throws URISyntaxException, IOException {
        final URL resource = CustomerOnboarding.class.getClassLoader().getResource("images/documents/" + imageId + ".jpeg");
        return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
    }

    private static byte[] getDetectionImage() throws URISyntaxException, IOException {
        final URL resource = CustomerOnboarding.class.getClassLoader().getResource("images/faces/face.jpeg");
        return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
    }

    private static void saveImage(byte[] image, String fileName) throws IOException {
        prepareOutputDirectory();

        ByteArrayInputStream bis = new ByteArrayInputStream(image);
        BufferedImage bImage2 = ImageIO.read(bis);
        ImageIO.write(bImage2, "png", new File("onboardingImages/" + fileName));
    }

    private static void prepareOutputDirectory() {
        File resultDirectory = new File("onboardingImages");
        if (!(resultDirectory.exists() && resultDirectory.isDirectory())) {
            resultDirectory.mkdir();
        }
    }
}
