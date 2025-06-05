package com.innovatrics.integrationsamples.onboarding.liveness;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.ErrorCodeEnum.NO_FACE_DETECTED;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.WarningsEnum.LOW_QUALITY;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.WarningsEnum.MULTIPLE_FACES_DETECTED;
import static com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse.ErrorCodeEnum.NOT_ENOUGH_DATA;

/**
 * This example represents sample implementation of image Passive Liveness evaluation by Digital Identity Service (DIS).
 * Note: If more than one photos are uploaded with AssertionEnum "NONE", final score will be average of scores of all images.
 */

public class EvaluatePassiveLiveness extends CustomerOnboardingApiTest {
    private static final Logger log = LogManager.getLogger(EvaluatePassiveLiveness.class);

    public EvaluatePassiveLiveness(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Creates a new customer using the API, evaluates passive liveness for the customer,
     * and deletes the customer.
     *
     * @throws ApiException If an error occurs while communicating with the API.
     * @throws URISyntaxException If the given URI syntax is incorrect.
     * @throws IOException If an input or output exception occurs.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customer = getApi().createCustomer();
        String customerId = customer.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluatePassiveLiveness(customerId);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    /**
     * Evaluates the passive liveness of a customer's image based on the provided customer ID.
     *
     * @param customerId The ID of the customer whose passive liveness is being evaluated.
     * @throws ApiException If an error occurs while communicating with the API.
     * @throws URISyntaxException If the given URI syntax is incorrect.
     * @throws IOException If an input or output exception occurred.
     */
    private void evaluatePassiveLiveness(String customerId) throws ApiException, URISyntaxException, IOException {
        getApi().createLiveness(customerId);

        final CreateCustomerLivenessSelfieResponse livenessSelfieByUrlResponse =
                getApi().createLivenessSelfie(customerId,
                createCustomerLivenessSelfieRequest(configuration.EXAMPLE_IMAGE_URL, CreateCustomerLivenessSelfieRequest.AssertionEnum.NONE));

        if (livenessSelfieByUrlResponse.getWarnings() != null && !livenessSelfieByUrlResponse.getWarnings().isEmpty()) {
            if (livenessSelfieByUrlResponse.getWarnings().contains(MULTIPLE_FACES_DETECTED)) {
                log.warn("Image added into Passive Liveness check contains more than one just face. " +
                        "Only the biggest face will be evaluated for Passive Liveness");
            }
            if (livenessSelfieByUrlResponse.getWarnings().contains(LOW_QUALITY)) {
                log.warn("Image added into Passive Liveness check has low quality and liveness evaluation may NOT be reliable. " +
                        "Please use photos satisfying image requirements: https://developers.innovatrics.com/digital-onboarding/docs/functionalities/face/passive-liveness-check/#passive-liveness-evaluation");
            }
        }

        if (livenessSelfieByUrlResponse.getErrorCode() != null) {
            if (livenessSelfieByUrlResponse.getErrorCode().equals(NO_FACE_DETECTED)) {
                log.warn("Face was not detected on image. Passive Liveness can not be evaluated on this image.");
            } else {
                log.error("This should not happen.");
            }
        } else {
            log.info("Added image for Passive Liveness evaluation.");
        }

        // This API calls can NOT return ErrorCode INVALID_DATA.
        final EvaluateCustomerLivenessResponse passiveLivenessResponse =
                getApi().evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.PASSIVE_LIVENESS));

        if (passiveLivenessResponse.getErrorCode() == null) {
            log.info("Average Passive Liveness score from uploaded photos has been evaluated to: {}", passiveLivenessResponse.getScore());
        } else if (passiveLivenessResponse.getErrorCode() == NOT_ENOUGH_DATA) {
            log.warn("Passive Liveness evaluation failed! At least one photo needs to be successfully uploaded with AssertionEnum \"NONE\"");
        } else {
            log.error("This should not happen.");
        }
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new EvaluatePassiveLiveness(new Configuration()).test();
    }
}
