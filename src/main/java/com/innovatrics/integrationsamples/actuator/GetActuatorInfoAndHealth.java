package com.innovatrics.integrationsamples.actuator;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * A class for testing and logging the health status and detailed information of the digital
 * identity service actuator.
 */
public class GetActuatorInfoAndHealth extends BaseApiTest<ActuatorApi> {
    private static final Logger LOG = LogManager.getLogger(GetActuatorInfoAndHealth.class);

    public GetActuatorInfoAndHealth(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the test to interact with the Actuator API. This method fetches the health status
     * and detailed information of the actuator and logs them.
     *
     * @throws ApiException if there is an error while interacting with the Actuator API
     */
    @Override
    public void doTest() throws ApiException {
        final ActuatorHealth actuatorHealth = getApi().health();
        final ActuatorInfo actuatorInfo = getApi().info();

        logActuatorHealth(actuatorHealth);
        logActuatorInfo(actuatorInfo);
    }

    
    /**
     * Logs the health status of the digital identity service.
     *
     * @param actuatorHealth the health status of the Actuator to be logged
     */
    private static void logActuatorHealth(ActuatorHealth actuatorHealth) {
        LOG.info("Digital Identity Service return Health status: {}", actuatorHealth.getStatus());
    }

    
    /**
     * Logs the detailed information of the digital identity service actuator.
     *
     * @param actuatorInfo the information of the Actuator to be logged
     */
    private static void logActuatorInfo(ActuatorInfo actuatorInfo) {
        LOG.info("Digital Identity Service is running in version: {}", actuatorInfo.getBuild().getVersion());
        LOG.info("Digital Identity Service has SAM library in version: {}", actuatorInfo.getSam().getVersion());
        LOG.info("Digital Identity Service has iFace library in version: {} with license expiration: {}-{}-{}",
                actuatorInfo.getIface().getVersion(),
                actuatorInfo.getIface().getLicense().getYear(),
                actuatorInfo.getIface().getLicense().getMonth(),
                actuatorInfo.getIface().getLicense().getDay());
    }


    /**
     * The main method that initiates the process of fetching and logging actuator information and health status
     *
     * @param args Command-line arguments provided to the program
     * @throws Exception Any exception that might be thrown during the execution of the test method
     */
    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new GetActuatorInfoAndHealth(new Configuration()).test();
    }
}