package com.innovatrics.integrationsamples.onboarding.liveness;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.*;
import static com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse.ErrorCodeEnum.*;

/**
 * This example represents sample implementation of Eye Gaze Liveness (formerly called as Active Liveness) evaluation on Digital Identity Service (DIS).
 */
public class EvaluateEyeGazeLiveness {
    private static final Logger LOG = LogManager.getLogger(EvaluateEyeGazeLiveness.class);

    public static void main(String[] args) throws IOException, URISyntaxException {

        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final CustomerOnboardingApi customerOnboardingApi = new CustomerOnboardingApi(client);

        try {
            final CreateCustomerResponse customer = customerOnboardingApi.createCustomer();
            final String customerId = customer.getId();
            LOG.info("Customer created with id: " + customerId);

            customerOnboardingApi.createLiveness(customerId);

            createAndCheckCustomerEyeGazeLivenessSegment(customerId, customerOnboardingApi, "top-left", EYE_GAZE_TOP_LEFT);
            createAndCheckCustomerEyeGazeLivenessSegment(customerId, customerOnboardingApi, "bottom-left", EYE_GAZE_BOTTOM_LEFT);
            createAndCheckCustomerEyeGazeLivenessSegment(customerId, customerOnboardingApi, "bottom-right", EYE_GAZE_BOTTOM_RIGHT);
            createAndCheckCustomerEyeGazeLivenessSegment(customerId, customerOnboardingApi, "top-left", EYE_GAZE_TOP_LEFT);
            createAndCheckCustomerEyeGazeLivenessSegment(customerId, customerOnboardingApi, "bottom-right", EYE_GAZE_BOTTOM_RIGHT);

            final EvaluateCustomerLivenessResponse eyeGazeLivenessResponse =  customerOnboardingApi.evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.EYE_GAZE_LIVENESS));
            if (eyeGazeLivenessResponse.getErrorCode() == null) {
                LOG.info("Customer Eye Gaze Liveness score: " + eyeGazeLivenessResponse.getScore());
            } else if (eyeGazeLivenessResponse.getErrorCode() == NOT_ENOUGH_DATA) {
                LOG.error("You have to upload at least 4 segments for Eye Gaze Liveness evaluation.");
            } else if (eyeGazeLivenessResponse.getErrorCode() == INVALID_DATA) {
                LOG.error("Either eyes was not detected on less then 4 segments or face was not detected on at least one segment.");
            }

            customerOnboardingApi.deleteCustomer(customerId);

        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }

    }

    /**
     * --------------------------------------------- Customer Liveness API --------------------------------------------
     */

    private static void createAndCheckCustomerEyeGazeLivenessSegment(String customerId, CustomerOnboardingApi customerOnboardingApi,
                                                                     String fileName, CreateCustomerLivenessSelfieRequest.AssertionEnum segmentPlacement) throws ApiException {
        CreateCustomerLivenessSelfieResponse eyeGazeLivenessSegmentResponse =
                createCustomerEyeGazeLivenessSegment(customerId, customerOnboardingApi, fileName, segmentPlacement);
        if (eyeGazeLivenessSegmentResponse.getWarnings() != null) {
            LOG.warn("Adding image: " + fileName + " as eye gaze image for position: " + segmentPlacement + "ended with warning code " + eyeGazeLivenessSegmentResponse.getWarnings());
        }
        if (eyeGazeLivenessSegmentResponse.getErrorCode() == null) {
            LOG.info("Successfully added image: " + fileName + " as eye gaze image for position: " + segmentPlacement);
        } else {
            LOG.warn("Adding image: " + fileName + " as eye gaze image for position: " + segmentPlacement + "failed with error code " + eyeGazeLivenessSegmentResponse.getErrorCode());
        }
    }

    private static CreateCustomerLivenessSelfieResponse createCustomerEyeGazeLivenessSegment(String customerId, CustomerOnboardingApi customerOnboardingApi,
                                                                                             String fileName, CreateCustomerLivenessSelfieRequest.AssertionEnum segmentPlacement) throws ApiException {
        // This API calls can return ErrorCode NO_FACE_DETECTED if there is no face is presented on image.
        // This API calls can return Warning MULTIPLE_FACES_DETECTED if there is are detected more than one face on image.
        return customerOnboardingApi.createLivenessSelfie(
                customerId,
                new CreateCustomerLivenessSelfieRequest()
                        .image(new Image().data(getPositionImage(fileName)))
                        .assertion(segmentPlacement)
        );
    }

    /**
     * ------------------------------------------------ Helper methods ------------------------------------------------
     */

    private static byte[] getPositionImage(String position) {
        final URL resource = EvaluateEyeGazeLiveness.class.getClassLoader().getResource("images/faces/" + position + ".jpeg");
        try {
            return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("This should not happened.");
        }
    }
}
