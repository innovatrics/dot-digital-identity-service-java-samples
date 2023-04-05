package com.innovatrics.integrationsamples.onboarding.liveness;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.ErrorCodeEnum.*;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.WarningsEnum.*;
import static com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse.ErrorCodeEnum.*;

/**
 * This example represents sample implementation of image Passive Liveness evaluation by Digital Identity Service (DIS).
 * Note: If more than one photos are uploaded with AssertionEnum "NONE", final score will be average of scores of all images.
 */

public class EvaluatePassiveLiveness {
    private static final Logger LOG = LogManager.getLogger(EvaluatePassiveLiveness.class);

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

            final CreateCustomerLivenessSelfieResponse livenessSelfieByUrlResponse = customerOnboardingApi.createLivenessSelfie(customerId,
                    new CreateCustomerLivenessSelfieRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL)).assertion(CreateCustomerLivenessSelfieRequest.AssertionEnum.NONE));

            if (livenessSelfieByUrlResponse.getWarnings() != null) {
                if (livenessSelfieByUrlResponse.getWarnings().contains(MULTIPLE_FACES_DETECTED)) {
                    LOG.warn("Image added into Passive Liveness check contains more than one just face. " +
                            "Only the biggest face will be evaluated for Passive Liveness");
                }
                if (livenessSelfieByUrlResponse.getWarnings().contains(LOW_QUALITY)) {
                    LOG.warn("Image added into Passive Liveness check has low quality and liveness evaluation may NOT be reliable. " +
                            "Please use photos satisfying image requirements: https://developers.innovatrics.com/digital-onboarding/docs/functionalities/face/passive-liveness-check/#passive-liveness-evaluation");
                }
            }

            if (livenessSelfieByUrlResponse.getErrorCode() != null) {
                if (livenessSelfieByUrlResponse.getErrorCode().equals(NO_FACE_DETECTED)) {
                    LOG.warn("Face was not detected on image. Passive Liveness can not be evaluated on this image.");
                } else {
                    LOG.error("This should not happen.");
                }
            } else {
                LOG.info("Added image for Passive Liveness evaluation.");
            }

            // This API calls can NOT return ErrorCode INVALID_DATA.
            final EvaluateCustomerLivenessResponse passiveLivenessResponse = customerOnboardingApi.evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.PASSIVE_LIVENESS));

            if (passiveLivenessResponse.getErrorCode() == null) {
                LOG.info("Average Passive Liveness score from uploaded photos has been evaluated to: " + passiveLivenessResponse.getScore());
            } else if (passiveLivenessResponse.getErrorCode() == NOT_ENOUGH_DATA) {
                LOG.warn("Passive Liveness evaluation failed! At least one photo needs to be successfully uploaded with AssertionEnum \"NONE\"");
            } else {
                LOG.error("This should not happen.");
            }

            customerOnboardingApi.deleteCustomer(customerId);

        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

}
