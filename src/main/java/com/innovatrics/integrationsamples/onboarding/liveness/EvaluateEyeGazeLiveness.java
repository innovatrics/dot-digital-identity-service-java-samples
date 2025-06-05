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

import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.EYE_GAZE_BOTTOM_LEFT;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.EYE_GAZE_BOTTOM_RIGHT;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.EYE_GAZE_TOP_LEFT;
import static com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse.ErrorCodeEnum.INVALID_DATA;
import static com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse.ErrorCodeEnum.NOT_ENOUGH_DATA;

/**
 * This example represents sample implementation of Eye Gaze Liveness (formerly called as Active Liveness) evaluation on Digital Identity Service (DIS).
 */
public class EvaluateEyeGazeLiveness extends CustomerOnboardingApiTest {
    private static final Logger log = LogManager.getLogger(EvaluateEyeGazeLiveness.class);

    public EvaluateEyeGazeLiveness(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Evaluating the liveness of the customer image.
     *
     * @throws ApiException If there is an error during the API call.
     * @throws URISyntaxException If the URI syntax is incorrect.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customer = getApi().createCustomer();
        final String customerId = customer.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluateLivenessOfImage(customerId);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    /**
     * Evaluates the liveness of an image by creating and checking customer eye gaze liveness segments.
     *
     * @param customerId The ID of the customer for which the liveness is to be evaluated.
     * @throws ApiException If there is an error during the API call.
     * @throws URISyntaxException If the URI syntax is incorrect.
     * @throws IOException If an I/O error occurs.
     */
    private void evaluateLivenessOfImage(String customerId) throws ApiException, URISyntaxException, IOException {
        getApi().createLiveness(customerId);

        createAndCheckCustomerEyeGazeLivenessSegment(customerId, "top-left", EYE_GAZE_TOP_LEFT);
        createAndCheckCustomerEyeGazeLivenessSegment(customerId, "bottom-left", EYE_GAZE_BOTTOM_LEFT);
        createAndCheckCustomerEyeGazeLivenessSegment(customerId, "bottom-right", EYE_GAZE_BOTTOM_RIGHT);
        createAndCheckCustomerEyeGazeLivenessSegment(customerId, "top-left", EYE_GAZE_TOP_LEFT);
        createAndCheckCustomerEyeGazeLivenessSegment(customerId, "bottom-right", EYE_GAZE_BOTTOM_RIGHT);

        final EvaluateCustomerLivenessResponse eyeGazeLivenessResponse = getApi().evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.EYE_GAZE_LIVENESS));
        if (eyeGazeLivenessResponse.getErrorCode() == null) {
            log.info("Customer Eye Gaze Liveness score: {}", eyeGazeLivenessResponse.getScore());
        } else if (eyeGazeLivenessResponse.getErrorCode() == NOT_ENOUGH_DATA) {
            log.error("You have to upload at least 4 segments for Eye Gaze Liveness evaluation.");
        } else if (eyeGazeLivenessResponse.getErrorCode() == INVALID_DATA) {
            log.error("Either eyes was not detected on less then 4 segments or face was not detected on at least one segment.");
        }
    }

    /**
     * Creates and checks a customer eye gaze liveness segment.
     *
     * @param customerId The ID of the customer for whom the liveness segment is being created.
     * @param fileName The name of the file containing the liveness image.
     * @param segmentPlacement The placement assertion for the liveness segment.
     * @throws ApiException if there is an error during the API call.
     * @throws URISyntaxException if the URI syntax is incorrect.
     * @throws IOException if an I/O error occurs.
     */
    private void createAndCheckCustomerEyeGazeLivenessSegment(String customerId,
                                                              String fileName,
                                                              CreateCustomerLivenessSelfieRequest.AssertionEnum segmentPlacement) throws ApiException, URISyntaxException, IOException {
        CreateCustomerLivenessSelfieResponse eyeGazeLivenessSegmentResponse =
                createCustomerEyeGazeLivenessSegment(customerId, fileName, segmentPlacement);

        if (eyeGazeLivenessSegmentResponse.getWarnings() != null && !eyeGazeLivenessSegmentResponse.getWarnings().isEmpty() ) {
            log.warn("Adding image: {} as eye gaze image for position: {} ended with warning code {}", fileName, segmentPlacement, eyeGazeLivenessSegmentResponse.getWarnings());
        }

        if (eyeGazeLivenessSegmentResponse.getErrorCode() == null) {
            log.info("Successfully added image: {} as eye gaze image for position: {}", fileName, segmentPlacement);
        } else {
            log.warn("Adding image: {} as eye gaze image for position: {} failed with error code {}", fileName, segmentPlacement, eyeGazeLivenessSegmentResponse.getErrorCode());
        }
    }

    /**
     * Creates a customer eye gaze liveness segment using the specified parameters.
     *
     * @param customerId The ID of the customer for whom the liveness segment is being created.
     * @param fileName The name of the file containing the liveness image.
     * @param segmentPlacement The placement assertion for the liveness segment.
     * @return CreateCustomerLivenessSelfieResponse The response from the liveness selfie creation API.
     * @throws ApiException if there is an error during the API call.
     * @throws URISyntaxException if the URI syntax is incorrect.
     * @throws IOException if an I/O error occurs.
     */
    private CreateCustomerLivenessSelfieResponse createCustomerEyeGazeLivenessSegment(String customerId,
                                                                                      String fileName,
                                                                                      CreateCustomerLivenessSelfieRequest.AssertionEnum segmentPlacement) throws ApiException, URISyntaxException, IOException {
        // This API calls can return ErrorCode NO_FACE_DETECTED if there is no face is presented on image.
        // This API calls can return Warning MULTIPLE_FACES_DETECTED if there is are detected more than one face on image.
        return getApi().createLivenessSelfie(
                customerId,
                createCustomerLivenessSelfieRequest(fileName, segmentPlacement)
        );
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new EvaluateEyeGazeLiveness(new Configuration()).test();
    }
}
