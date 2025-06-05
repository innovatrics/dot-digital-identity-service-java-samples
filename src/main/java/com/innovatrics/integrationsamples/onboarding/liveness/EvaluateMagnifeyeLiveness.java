package com.innovatrics.integrationsamples.onboarding.liveness;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessRecordResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessRecordResponse.ErrorCodeEnum;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest.TypeEnum;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.integrationsamples.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;

import com.innovatrics.integrationsamples.testhelper.CustomerOnboardingApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This example represents sample implementation of Magnifeye Liveness evaluation on Digital Identity Service (DIS).
 */
public class EvaluateMagnifeyeLiveness extends CustomerOnboardingApiTest {

    private static final Logger log = LogManager.getLogger(EvaluateMagnifeyeLiveness.class);
    public static final String INVALID_DATA = "Invalid data. Magnifeye Liveness can not be evaluated on this liveness record.";

    public EvaluateMagnifeyeLiveness(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Evaluating the customer Magnifeye Liveness score.
     *
     * @throws ApiException if there is an error with the API call.
     * @throws URISyntaxException if the URI is incorrect.
     * @throws IOException if an input or output exception occurred.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final CreateCustomerResponse customer = getApi().createCustomer();
        final String customerId = customer.getId();
        log.info("Customer created with id: {}", customerId);

        try {
            evaluateMagnifeyeLiveness(customerId);
        } finally {
            deleteCustomerWithId(customerId);
        }
    }

    /**
     * Evaluates the Magnifeye Liveness for a specified customer,
     * then evaluating the liveness score and logging the results.
     *
     * @param customerId The unique identifier of the customer.
     * @throws ApiException if there is an error with the API call.
     * @throws IOException if an input or output exception occurred.
     * @throws URISyntaxException if the URI is incorrect.
     */
    private void evaluateMagnifeyeLiveness(String customerId) throws ApiException, IOException, URISyntaxException {
        getApi().createLiveness(customerId);

        // create customer liveness record for Magnifeye Liveness
        final byte[] binaryData = getBinaryData(configuration.LIVENESS_RECORDS_MAGNIFEYE_LIVENESS_BINARY_FILE);
        final CreateCustomerLivenessRecordResponse livenessRecordResponse = getApi().createLivenessRecord(customerId, binaryData);

        if (livenessRecordResponse.getErrorCode() == null) {
            log.info("Customer liveness record for Magnifeye Liveness was successfully created.");
        } else if (livenessRecordResponse.getErrorCode() == ErrorCodeEnum.INVALID_DATA) {
            log.error(INVALID_DATA);
        } else {
            log.error("This should not happen.");
        }

        // evaluate Magnifeye Liveness score
        final EvaluateCustomerLivenessResponse magnifeyeLivenessResponse =
                getApi().evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(TypeEnum.MAGNIFEYE_LIVENESS));

        if (magnifeyeLivenessResponse.getErrorCode() == null) {
            log.info("Customer Magnifeye Liveness score: {}", magnifeyeLivenessResponse.getScore());
        } else if (magnifeyeLivenessResponse.getErrorCode() == EvaluateCustomerLivenessResponse.ErrorCodeEnum.NOT_ENOUGH_DATA) {
            log.error("Liveness record has to be created to evaluate Magnifeye Liveness.");
        } else if (magnifeyeLivenessResponse.getErrorCode() == EvaluateCustomerLivenessResponse.ErrorCodeEnum.INVALID_DATA){
            log.error(INVALID_DATA);
        }
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new EvaluateMagnifeyeLiveness(new Configuration()).test();
    }
}
