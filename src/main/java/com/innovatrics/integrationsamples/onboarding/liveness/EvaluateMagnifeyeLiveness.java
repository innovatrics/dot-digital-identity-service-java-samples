package com.innovatrics.integrationsamples.onboarding.liveness;


import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessRecordResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerLivenessRecordResponse.ErrorCodeEnum;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CustomerOnboardingApi;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessRequest.TypeEnum;
import com.innovatrics.dot.integrationsamples.disapi.model.EvaluateCustomerLivenessResponse;
import com.innovatrics.integrationsamples.Configuration;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * This example represents sample implementation of Magnifeye Liveness evaluation on Digital Identity Service (DIS).
 */
public class EvaluateMagnifeyeLiveness {

    private static final Logger LOG = LogManager.getLogger(EvaluateMagnifeyeLiveness.class);

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

            // create customer liveness record for Magnifeye Liveness
            final byte[] binaryData = getBinaryData(configuration.LIVENESS_RECORDS_MAGNIFEYE_LIVENESS_BINARY_FILE);
            final CreateCustomerLivenessRecordResponse livenessRecordResponse = customerOnboardingApi.createLivenessRecord(customerId, binaryData);

            if (livenessRecordResponse.getErrorCode() == null) {
                LOG.info("Customer liveness record for Magnifeye Liveness was successfully created.");
            } else if (livenessRecordResponse.getErrorCode() == ErrorCodeEnum.INVALID_DATA) {
                LOG.error("Invalid data. Magnifeye Liveness can not be evaluated on this liveness record.");
            } else {
                LOG.error("This should not happen.");
            }

            // evaluate Magnifeye Liveness score
            final EvaluateCustomerLivenessResponse magnifeyeLivenessResponse = customerOnboardingApi.evaluateLiveness(customerId, new EvaluateCustomerLivenessRequest().type(TypeEnum.MAGNIFEYE_LIVENESS));

            if (magnifeyeLivenessResponse.getErrorCode() == null) {
                LOG.info("Customer Magnifeye Liveness score: " + magnifeyeLivenessResponse.getScore());
            } else if (magnifeyeLivenessResponse.getErrorCode() == EvaluateCustomerLivenessResponse.ErrorCodeEnum.NOT_ENOUGH_DATA) {
                LOG.error("Liveness record has to be created to evaluate Magnifeye Liveness.");
            } else if (magnifeyeLivenessResponse.getErrorCode() == EvaluateCustomerLivenessResponse.ErrorCodeEnum.INVALID_DATA){
                LOG.error("Invalid data. Magnifeye Liveness can not be evaluated on this liveness record.");
            }

            customerOnboardingApi.deleteCustomer(customerId);

        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    private static byte[] getBinaryData(String path) {
        try {
            return new FileInputStream(Path.of(path).toFile()).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("This should not happen.");
        }
    }
}
