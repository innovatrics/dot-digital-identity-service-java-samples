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

import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.NEUTRAL;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieRequest.AssertionEnum.SMILE;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.WarningsEnum.LOW_QUALITY;
import static com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessSelfieResponse.WarningsEnum.MULTIPLE_FACES_DETECTED;
import static com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse.ErrorCodeEnum.NOT_ENOUGH_DATA;

/**
 * This example represents sample implementation of image Smile Liveness evaluation by Digital Identity Service (DIS).
 */
public class EvaluateSmileLiveness extends CustomerOnboardingApiTest {
    private static final Logger log = LogManager.getLogger(EvaluateSmileLiveness.class);

    public EvaluateSmileLiveness(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the test for evaluating customer face smile liveness.
     * This method creates a new customer, evaluates their smile liveness, and finally deletes the customer.
     *
     * @throws ApiException if there is an error with the API call
     * @throws URISyntaxException if there is a URI syntax error
     * @throws IOException if there is an I/O error during the process
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customer = getApi().createCustomer();
        String customerId = customer.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluateCustomerFaceSmileLiveness(customerId);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    /**
     * Evaluates the customer's smile liveness by verifying the expression in uploaded selfie images.
     *
     * @param customerId the unique identifier of the customer
     * @throws ApiException if there is an error with the API call
     * @throws URISyntaxException if there is a URI syntax error
     * @throws IOException if there is an I/O error during the file operation
     */
    private void evaluateCustomerFaceSmileLiveness(String customerId) throws ApiException, URISyntaxException, IOException {
        getApi().createLiveness(customerId);

        sendCustomerSelfieAndCheckSmileLivenessExpression(customerId, "face-neutral", NEUTRAL);
        sendCustomerSelfieAndCheckSmileLivenessExpression(customerId, "face-smile", SMILE);

        // This API calls can NOT return ErrorCode INVALID_DATA.
        final EvaluateCustomerLivenessResponse smileLivenessResponse =
                getApi().evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(EvaluateCustomerLivenessRequest.TypeEnum.SMILE_LIVENESS));

        if (smileLivenessResponse.getErrorCode() == null) {
            log.info("Smile Liveness score from uploaded photos has been evaluated to: {}", smileLivenessResponse.getScore());
        } else if (smileLivenessResponse.getErrorCode() == NOT_ENOUGH_DATA) {
            log.warn("Smile Liveness evaluation failed! At least one pair of photos with expression NEUTRAL and SMILE photo needs to be successfully uploaded.");
        } else {
            log.error("This should not happen.");
        }
    }

    /**
     * Verifies the customer's smile liveness expression using a selfie image.
     * This method makes an API call to verify the selfie image for the specified customer ID and expected face expression.
     *
     * @param customerId     the unique identifier of the customer
     * @param fileName       the path to the selfie image file
     * @param faceExpression the expected facial expression to verify in the selfie (e.g., SMILE)
     * @throws ApiException       if there is an error with the API call
     * @throws URISyntaxException if there is a URI syntax error
     * @throws IOException        if there is an I/O error during the file operation
     */
    private void sendCustomerSelfieAndCheckSmileLivenessExpression(String customerId,
                                                                   String fileName,
                                                                   CreateCustomerLivenessSelfieRequest.AssertionEnum faceExpression) throws ApiException, URISyntaxException, IOException {
        CreateCustomerLivenessSelfieResponse smileLivenessExpressionResponse =
                verifyCustomerSmileLivenessExpression(customerId, fileName, faceExpression);

        if (smileLivenessExpressionResponse.getWarnings() != null && !smileLivenessExpressionResponse.getWarnings().isEmpty()) {
            if (smileLivenessExpressionResponse.getWarnings().contains(MULTIPLE_FACES_DETECTED)) {
                log.warn("Image added into Smile Liveness check contains more than one just face. " +
                        "Only the biggest face will be evaluated for Passive Liveness");
            }
            if (smileLivenessExpressionResponse.getWarnings().contains(LOW_QUALITY)) {
                log.warn("Image added into Smile Liveness check has low quality and liveness evaluation may NOT be reliable. " +
                        "Please use photos satisfying image requirements: https://developers.innovatrics.com/digital-onboarding/docs/functionalities/face/passive-liveness-check/#passive-liveness-evaluation");
            }
        }

        if (smileLivenessExpressionResponse.getErrorCode() == null) {
            log.info("Successfully added image: {} as smile liveness for face expression: {}", fileName, faceExpression);
        } else {
            log.error("Adding image: {} as smile liveness for face expression: {} failed with error code: {}", fileName, faceExpression, smileLivenessExpressionResponse.getErrorCode());
            throw new RuntimeException("Smile Liveness can not be calculated if any of images fails.");
        }
    }

    /**
     * Verifies the customer's smile liveness expression using a selfie image.
     * This method makes an API call to verify the selfie image for the specified customer ID and expected face expression.
     *
     * @param customerId     the unique identifier of the customer
     * @param fileName       the path to the selfie image file
     * @param faceExpression the expected facial expression to verify in the selfie (e.g., SMILE)
     * @return a CreateCustomerLivenessSelfieResponse object containing the results of the liveness verification
     * @throws ApiException       if there is an error with the API call
     * @throws URISyntaxException if there is a URI syntax error
     * @throws IOException        if there is an I/O error during the file operation
     */
    private CreateCustomerLivenessSelfieResponse verifyCustomerSmileLivenessExpression(String customerId,
                                                                                       String fileName,
                                                                                       CreateCustomerLivenessSelfieRequest.AssertionEnum faceExpression) throws ApiException, URISyntaxException, IOException {
        // This API calls can return ErrorCode NO_FACE_DETECTED if no face is presented on image.
        // This API calls can return Warning MULTIPLE_FACES_DETECTED if more than one face is detected on image.
        // This API calls can return Warning LOW_QUALITY if image has does not fulfill smile liveness requirements.
        return getApi().createLivenessSelfie(
                customerId,
                createCustomerLivenessSelfieRequest(fileName, faceExpression)
        );
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new EvaluateSmileLiveness(new Configuration()).test();
    }
}
