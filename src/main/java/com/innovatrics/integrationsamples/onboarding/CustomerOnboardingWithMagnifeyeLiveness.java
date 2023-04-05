package com.innovatrics.integrationsamples.onboarding;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessRecordResponse;
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
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest.TypeEnum;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.ImageCrop;
import com.innovatrics.dot.integrationsamples.disapi.model.LivenessSelfieOrigin;
import com.innovatrics.integrationsamples.Configuration;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example demonstrates comprehensive usage of customer onboarding API centered around the Magnifeye liveness.
 */
public class CustomerOnboardingWithMagnifeyeLiveness {

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

            customerOnboardingApi.createLiveness(customerId);

            // create customer liveness record for Magnifeye Liveness
            final byte[] magnifeyeBinaryData = getBinaryData(configuration.LIVENESS_RECORDS_MAGNIFEYE_LIVENESS_BINARY_FILE);
            final CreateCustomerLivenessRecordResponse livenessRecordResponse = customerOnboardingApi.createLivenessRecord(customerId, magnifeyeBinaryData);
            CreateCustomerLivenessRecordResponse.ErrorCodeEnum livenessRecordError = livenessRecordResponse.getErrorCode();
            if (livenessRecordError != null) {
                LOG.error(livenessRecordError.getValue());
                return;
            }

            LOG.info("Customer liveness record for Magnifeye Liveness was successfully created.");

            // evaluate Magnifeye liveness score
            final EvaluateCustomerLivenessResponse magnifeyeLivenessResponse = customerOnboardingApi.evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(TypeEnum.MAGNIFEYE_LIVENESS));
            EvaluateCustomerLivenessResponse.ErrorCodeEnum magnifeyeLivenessError = magnifeyeLivenessResponse.getErrorCode();
            if (magnifeyeLivenessError != null) {
                LOG.error(magnifeyeLivenessError.getValue());
                return;
            }

            LOG.info("Customer Magnifeye Liveness score: " + magnifeyeLivenessResponse.getScore());

            // get Magnifeye Liveness selfie link from the response
            final String livenessSelfieLink = livenessRecordResponse.getLinks().getSelfie();

            // create customer selfie using Magnifeye liveness selfie
            final CreateSelfieResponse selfieResponse = customerOnboardingApi.createSelfie(customerId, new CreateSelfieRequest().selfieOrigin(new LivenessSelfieOrigin().link(livenessSelfieLink)));
            CreateSelfieResponse.ErrorCodeEnum createSelfieError = selfieResponse.getErrorCode();
            if (createSelfieError != null) {
                LOG.error(createSelfieError.getValue());
                return;
            }

            LOG.info("Successfully created customer selfie from Magnifeye liveness selfie.");

            List<CreateSelfieResponse.WarningsEnum> createSelfieWarnings = selfieResponse.getWarnings();
            if (createSelfieWarnings != null) {
                for (CreateSelfieResponse.WarningsEnum warning : createSelfieWarnings) {
                    LOG.warn("Customer selfie warning: " + warning.getValue());
                }
            }

            // create document
            customerOnboardingApi.createDocument(customerId, new CreateDocumentRequest().advice(new DocumentAdvice().classification(new DocumentClassificationAdvice().addCountriesItem("INO"))));

            // create document front page
            final CreateDocumentPageResponse createDocumentResponseFront = customerOnboardingApi.createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getDocumentImage("document-front"))));
            CreateDocumentPageResponse.ErrorCodeEnum documentFrontError = createDocumentResponseFront.getErrorCode();
            if (documentFrontError != null) {
                LOG.error(documentFrontError.getValue());
                return;
            }
            LOG.info("Document classified: " + createDocumentResponseFront.getDocumentType().getType() + " page type: " + createDocumentResponseFront.getPageType());

            // create document back page
            final CreateDocumentPageResponse createDocumentResponseBack = customerOnboardingApi.createDocumentPage(customerId, new CreateDocumentPageRequest().image(new Image().data(getDocumentImage("document-back"))));
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

    private static byte[] getBinaryData(String path) {
        try {
            return new FileInputStream(Path.of(path).toFile()).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("This should not happen.");
        }
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
