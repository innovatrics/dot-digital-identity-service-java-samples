package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceQualityResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.HeadPoseAttribute;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This example demonstrates usage of face operations API for evaluating face image quality such as head rotation.
 */
public class FaceImageQualityCheck extends BaseApiTest<FaceOperationsApi> {
    private static final Logger log = LogManager.getLogger(FaceImageQualityCheck.class);

    public FaceImageQualityCheck(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes the test for detecting a face in an image and evaluating its quality.
     *
     * @throws ApiException if there is an error in detecting the face or checking the quality.
     */
    @Override
    protected void doTest() throws ApiException {
        final CreateFaceResponse faceResponse = getApi().detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL)));
        final String faceId = faceResponse.getId();
        log.info("Face detected with id: {}", faceId);

        log.info("About to evaluate custom quality check with documented Glass Detection Preconditions set as: ");
        log.info("  'face detection confidence' >= {}", configuration.QUALITY_GLASS_CONDITIONS_DETECTION_CONFIDENCE);
        log.info("  'yaw angle' <{};{}>", configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_LOW, configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_HIGH);
        log.info("  'pitch angle' <{};{}>", configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_LOW, configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_HIGH);
        final FaceQualityResponse qualityResponse = getApi().checkQuality(faceId);

        if (faceResponse.getDetection() == null) {
            throw new ApiException("Detection Object is null");
        }

        final Double detectionConfidence = faceResponse.getDetection().getConfidence();
        boolean checkResult = customImageQualityCheck(qualityResponse, detectionConfidence, configuration);
        log.info("Face image is compliant with selected quality criteria: {}", checkResult);
    }

    /**
     * Checks the quality of an image based on various attributes such as
     * detection confidence, yaw angle, and pitch angle.
     *
     * @param qualityResponse the response object containing the quality attributes
     * @param confidence the detection confidence score
     * @param configuration the configuration object holding the threshold values
     * @return true if all quality attributes meet the specified conditions, false otherwise
     * @throws ApiException if the qualityResponse object or required attributes are not correctly provided
     */
    private boolean customImageQualityCheck(FaceQualityResponse qualityResponse, Double confidence, Configuration configuration) throws ApiException {
        if ( qualityResponse == null || qualityResponse.getYaw() == null || qualityResponse.getPitch() == null) {
            throw new ApiException("QualityResponse Object is not correctly filled up.");
        }

        log.info("Checking selected quality attributes.");
        log.info("Checking detection confidence with score: {}", confidence);
        boolean isDetectionConfidenceOk = confidence >= configuration.QUALITY_GLASS_CONDITIONS_DETECTION_CONFIDENCE;

        log.info("Checking yaw angle attribute");
        boolean isYawAngleOk = evaluateHeadposeAttribute(qualityResponse.getYaw(), configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_LOW, configuration.QUALITY_GLASS_CONDITIONS_YAW_ANGLE_HIGH);

        log.info("Checking pitch angle attribute");
        boolean isPitchAngleOk = evaluateHeadposeAttribute(qualityResponse.getPitch(), configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_LOW, configuration.QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_HIGH);
        return isDetectionConfidenceOk && isPitchAngleOk && isYawAngleOk;
    }

    /**
     * Evaluates whether a given head pose attribute meets specified conditions.
     *
     * @param attribute the head pose attribute to evaluate
     * @param min the minimum angle threshold
     * @param max the maximum angle threshold
     * @return true if the attribute's preconditions are met and its angle is within the specified range, false otherwise
     */
    private boolean evaluateHeadposeAttribute(HeadPoseAttribute attribute, Double min, Double max) {
        log.info("Evaluating given attribute with preconditions met: {} and angle/score: {}", attribute.getPreconditionsMet(), attribute.getAngle());
        return attribute.getPreconditionsMet() && attribute.getAngle() > min && attribute.getAngle() < max;
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new FaceImageQualityCheck(new Configuration()).test();
    }
}
