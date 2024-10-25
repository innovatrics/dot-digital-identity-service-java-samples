package com.innovatrics.integrationsamples.onboarding.liveness;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CustomerOnboardingApi;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.NEUTRAL;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.SMILE;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.WarningsEnum.LOW_QUALITY;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.WarningsEnum.MULTIPLE_FACES_DETECTED;
import static com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse.ErrorCodeEnum.NOT_ENOUGH_DATA;

/**
 * This example represents sample implementation of image Smile Liveness evaluation by Digital Identity Service (DIS).
 */

public class EvaluateSmileLiveness {
    private static final Logger LOG = LogManager.getLogger(EvaluateSmileLiveness.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final CustomerOnboardingApi customerOnboardingApi = new CustomerOnboardingApi(client);

        try {
            final CreateCustomerResponse customer = customerOnboardingApi.createCustomer();
            String customerId = customer.getId();
            LOG.info("Customer created with id: " + customerId);

            customerOnboardingApi.createLiveness(customerId);

            createAndCheckCustomerSmileLivenessExpression(customerId,customerOnboardingApi, "face-neutral", NEUTRAL);
            createAndCheckCustomerSmileLivenessExpression(customerId,customerOnboardingApi, "face-smile", SMILE);


            // This API calls can NOT return ErrorCode INVALID_DATA.
            final EvaluateCustomerLivenessResponse smileLivenessResponse = customerOnboardingApi.evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.SMILE_LIVENESS));

            if (smileLivenessResponse.getErrorCode() == null) {
                LOG.info("Smile Liveness score from uploaded photos has been evaluated to: " + smileLivenessResponse.getScore());
            } else if (smileLivenessResponse.getErrorCode() == NOT_ENOUGH_DATA) {
                LOG.warn("Smile Liveness evaluation failed! At least one pair of photos with expression NEUTRAL and SMILE photo needs to be successfully uploaded.");
            } else {
                LOG.error("This should not happen.");
            }

            customerOnboardingApi.deleteCustomer(customerId);

        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    /**
     * --------------------------------------------- Customer Liveness API --------------------------------------------
     */

    private static void createAndCheckCustomerSmileLivenessExpression(String customerId, CustomerOnboardingApi customerOnboardingApi,
                                                                     String fileName, CreateCustomerLivenessSelfieRequest.AssertionEnum faceExpression) throws ApiException {
        CreateCustomerLivenessSelfieResponse smileLivenessExpressionResponse =
                createCustomerSmileLivenessExpression(customerId, customerOnboardingApi, fileName, faceExpression);

        if (smileLivenessExpressionResponse.getWarnings() != null) {
            if (smileLivenessExpressionResponse.getWarnings().contains(MULTIPLE_FACES_DETECTED)) {
                LOG.warn("Image added into Smile Liveness check contains more than one just face. " +
                        "Only the biggest face will be evaluated for Passive Liveness");
            }
            if (smileLivenessExpressionResponse.getWarnings().contains(LOW_QUALITY)) {
                LOG.warn("Image added into Smile Liveness check has low quality and liveness evaluation may NOT be reliable. " +
                        "Please use photos satisfying image requirements: https://developers.innovatrics.com/digital-onboarding/docs/functionalities/face/passive-liveness-check/#passive-liveness-evaluation");
            }
        }

        if (smileLivenessExpressionResponse.getErrorCode() == null) {
            LOG.info("Successfully added image: " + fileName + " as smile liveness for face expression: " + faceExpression);
        } else {
            LOG.error("Adding image: " + fileName + " as smile liveness for face expression: " + faceExpression + " failed with error code: " + smileLivenessExpressionResponse.getErrorCode());
            throw new RuntimeException("Smile Liveness can not be calculated if any of images fails.");
        }
    }

    private static CreateCustomerLivenessSelfieResponse createCustomerSmileLivenessExpression(String customerId, CustomerOnboardingApi customerOnboardingApi,
                                                                                             String fileName, CreateCustomerLivenessSelfieRequest.AssertionEnum faceExpression) throws ApiException {
        // This API calls can return ErrorCode NO_FACE_DETECTED if no face is presented on image.
        // This API calls can return Warning MULTIPLE_FACES_DETECTED if more than one face is detected on image.
        // This API calls can return Warning LOW_QUALITY if image has does not fulfill smile liveness requirements.
        return customerOnboardingApi.createLivenessSelfie(
                customerId,
                new CreateCustomerLivenessSelfieRequest()
                        .image(new Image().data(getPositionImage(fileName)))
                        .assertion(faceExpression)
        );
    }

    /**
     * ------------------------------------------------ Helper methods ------------------------------------------------
     */

    private static byte[] getPositionImage(String name) {
        final URL resource = EvaluateEyeGazeLiveness.class.getClassLoader().getResource("images/faces/" + name + ".jpeg");
        try {
            return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("This should not happened.");
        }
    }
}
