package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceQualityResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.HeadPoseAttribute;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This example demonstrates usage of face operations API for evaluating face image quality such as head rotation.
 */
public class FaceImageQualityCheck {
    private static final Logger LOG = LogManager.getLogger(FaceImageQualityCheck.class);

    public static void main(String[] args) throws IOException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final FaceOperationsApi faceApi = new FaceOperationsApi(client);

        try {
            final CreateFaceResponse faceResponse = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL)));
            final String faceId = faceResponse.getId();
            LOG.info("Face detected with id: " + faceId);

            LOG.info("About to evaluate custom quality check with documented Glass Detection Preconditions set as: ");
            LOG.info("  'face detection confidence' >= " + configuration.QUALITY_GLASS_CONDITIONS_DETECTION_CONFIDENCE);
            LOG.info("  'yaw angle' <" + configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_LOW + ";" + configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_HIGH + ">");
            LOG.info("  'pitch angle' <" + configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_LOW + ";" + configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_HIGH + ">");
            final FaceQualityResponse qualityResponse = faceApi.checkQuality(faceId);

            final Double detectionConfidence = faceResponse.getDetection().getConfidence();
            boolean checkResult = customImageQualityCheck(qualityResponse, detectionConfidence, configuration);
            LOG.info("Face image is compliant with selected quality criteria: " + checkResult);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    private static boolean customImageQualityCheck(FaceQualityResponse qualityResponse, Double confidence, Configuration configuration) {
        LOG.info("Checking selected quality attributes.");
        LOG.info("Checking detection confidence with score: " + confidence);
        boolean isDetectionConfidenceOk = confidence >= configuration.QUALITY_GLASS_CONDITIONS_DETECTION_CONFIDENCE;
        LOG.info("Checking yaw angle attribute");
        boolean isYawAngleOk = evaluateHeadposeAttribute(qualityResponse.getYaw(), configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_LOW, configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_HIGH);
        LOG.info("Checking pitch angle attribute");
        boolean isPitchAngleOk = evaluateHeadposeAttribute(qualityResponse.getPitch(), configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_LOW, configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_HIGH);
        return isDetectionConfidenceOk && isPitchAngleOk && isYawAngleOk;
    }

    private static boolean evaluateHeadposeAttribute(HeadPoseAttribute attribute, Double min, Double max) {
        LOG.info("Evaluating given attribute with preconditions met: " + attribute.getPreconditionsMet() + " and angle/score: " + attribute.getAngle());
        return attribute.getPreconditionsMet() && attribute.getAngle() > min && attribute.getAngle() < max;
    }
}
