package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceMaskResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.GlassesResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This example demonstrates usage of face operations API for evaluating presence of wearables such as face mask or
 * glasses in the provided face image.
 */
public class WearablesCheck extends BaseApiTest<FaceOperationsApi> {
    private static final Logger log = LogManager.getLogger(WearablesCheck.class);

    public WearablesCheck(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Detects a face from a predefined image URL and checks for the presence of
     * a face mask and glasses on the detected face.
     *
     * @throws ApiException if there is an error during the face detection or attribute checks.
     */
    @Override
    protected void doTest() throws ApiException {
        String faceId = getApi().detectFace(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL))).getId();
        log.info("Face detected with id: {}", faceId);

        checkFaceMask(configuration, faceId);
        checkGlasses(configuration, faceId);
    }

    /**
     * Checks the presence of a face mask on a face identified by the given face ID using the specified configuration.
     *
     * @param configuration The configuration object containing threshold values for face mask detection.
     * @param faceId The ID of the face to be checked for the presence of a face mask.
     */
    private void checkFaceMask(Configuration configuration, String faceId) {
        try {
            FaceMaskResponse faceMaskResponse = getApi().checkFaceMask(faceId);
            boolean maskDetected = faceMaskResponse.getScore() > configuration.WEARABLES_FACE_MASK_THRESHOLD;
            log.info("Face mask detected on face image: {}", maskDetected);
        } catch (ApiException exception) {
            log.error("Mask detection call failed. Make sure balanced or accurate detection mode is enabled");
        }
    }

    /**
     * Checks the presence of glasses, heavy frame, and tinted glasses on a face identified by the given face ID.
     *
     * @param configuration The configuration object containing threshold values for glasses detection.
     * @param faceId The ID of the face to be checked for the presence of glasses and related attributes.
     */
    private void checkGlasses(Configuration configuration, String faceId) {
        try {
            GlassesResponse glassesResponse = getApi().checkGlasses(faceId);
            if (glassesResponse == null || glassesResponse.getScore() == null || glassesResponse.getHeavyFrame() == null || glassesResponse.getTinted() == null) {
                throw new ApiException("Glasses response is invalid");
            }

            boolean hasGlasses = glassesResponse.getScore() > configuration.WEARABLES_GLASSES_THRESHOLD;
            boolean hasHeavyFrame = glassesResponse.getHeavyFrame() > configuration.WEARABLES_HEAVY_GLASS_FRAME_THRESHOLD;
            boolean hasTintedGlass = glassesResponse.getTinted() > configuration.WEARABLES_TINTED_GLASS_THRESHOLD;
            log.info("Glasses were detected on face image: {} having heavy frame: {} and having tinted glass: {}", hasGlasses, hasHeavyFrame, hasTintedGlass);
        } catch (ApiException exception) {
            log.error("Request to server failed with code: {} and response: {}", exception.getCode(), exception.getResponseBody());
        }
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new WearablesCheck(new Configuration()).test();
    }
}
