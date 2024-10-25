package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceAspectsResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This example demonstrates usage of face operations API for evaluating face aspects such as age or gender.
 */
public class FaceAspectsCheck {
    private static final Logger LOG = LogManager.getLogger(FaceAspectsCheck.class);

    public static void main(String[] args) throws IOException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final FaceOperationsApi faceApi = new FaceOperationsApi(client);

        try {
            final Double genderDecisionThreshold = configuration.ASPECTS_CHECK_GENDER_THRESHOLD;
            final Double ageDecisionThreshold = configuration.ASPECTS_CHECK_AGE_THRESHOLD;
            final String faceId = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL))).getId();
            LOG.info("Face detected with id: " + faceId);

            FaceAspectsResponse faceAspectsResponse = faceApi.evaluateAspects(faceId);

            boolean ageResult = faceAspectsResponse.getAge() >= ageDecisionThreshold;
            String genderResult = faceAspectsResponse.getGender() >= genderDecisionThreshold ? "F" : "M";
            LOG.info("Gender is evaluated as: " + genderResult + " with score " + faceAspectsResponse.getGender() + " and threshold: " + genderDecisionThreshold);
            LOG.info("Face aspects check with detected age of: " + faceAspectsResponse.getAge() + " and threshold of: " + ageDecisionThreshold);
            LOG.info("Age of face on image is: " + (ageResult ? "Higher" : "Lower") + " than requested threshold.");
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

}
