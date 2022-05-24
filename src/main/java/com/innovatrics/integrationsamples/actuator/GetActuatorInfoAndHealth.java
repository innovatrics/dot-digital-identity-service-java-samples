package com.innovatrics.integrationsamples.actuator;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This example represents possibility how to check Digital identity service version, health state, iFace version and its license expiration.
 */
public class GetActuatorInfoAndHealth {
    private static final Logger LOG = LogManager.getLogger(GetActuatorInfoAndHealth.class);

    public static void main(String[] args) throws IOException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final ActuatorApi actuatorApi = new ActuatorApi(client);

        try {
            final ActuatorHealth actuatorHealth = actuatorApi.health();
            final ActuatorInfo actuatorInfo = actuatorApi.info();

            LOG.info("Digital Identity Service return Health status: " + actuatorHealth.getStatus());

            LOG.info("Digital Identity Service is running in version: " + actuatorInfo.getBuild().getVersion());
            LOG.info("Digital Identity Service has SAM library in version: " + actuatorInfo.getSam().getVersion());
            LOG.info("Digital Identity Service has iFace library in version: " + actuatorInfo.getIface().getVersion() + " with license expiration: "
                    + actuatorInfo.getIface().getLicense().getYear() + "-" + actuatorInfo.getIface().getLicense().getMonth() + "-" + actuatorInfo.getIface().getLicense().getDay());

        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }
}
