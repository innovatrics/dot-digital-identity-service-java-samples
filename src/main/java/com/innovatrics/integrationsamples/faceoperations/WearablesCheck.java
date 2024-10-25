package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceMaskResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.GlassesResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This example demonstrates usage of face operations API for evaluating presence of wearables such as face mask or
 * glasses in the provided face image.
 */
public class WearablesCheck {
    private static final Logger LOG = LogManager.getLogger(WearablesCheck.class);

    public static void main(String[] args) throws ApiException, IOException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final FaceOperationsApi faceApi = new FaceOperationsApi(client);

        String faceId;
        try {
            faceId = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL))).getId();
            LOG.info("Face detected with id: " + faceId);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
            return;
        }

        checkFaceMask(configuration, faceApi, faceId);
        checkGlasses(configuration, faceApi, faceId);
    }

    private static void checkFaceMask(Configuration configuration, FaceOperationsApi faceApi, String faceId) {
        try {
            FaceMaskResponse faceMaskResponse = faceApi.checkFaceMask(faceId);
            boolean maskDetected = faceMaskResponse.getScore() > configuration.WEARABLES_FACE_MASK_THRESHOLD;
            LOG.info("Face mask detected on face image: " + maskDetected);
        } catch (ApiException exception) {
            LOG.error("Mask detection call failed. Make sure balanced or accurate detection mode is enabled");
        }
    }

    private static void checkGlasses(Configuration configuration, FaceOperationsApi faceApi, String faceId) {
        try {
            GlassesResponse glassesResponse = faceApi.checkGlasses(faceId);
            boolean hasGlasses = glassesResponse.getScore() > configuration.WEARABLES_GLASSES_THRESHOLD;
            boolean hasHeavyFrame = glassesResponse.getHeavyFrame() > configuration.WEARABLES_HEAVY_GLASS_FRAME_THRESHOLD;
            boolean hasTintedGlass = glassesResponse.getTinted() > configuration.WEARABLES_TINTED_GLASS_THRESHOLD;
            LOG.info("Glasses were detected on face image: " + hasGlasses + " having heavy frame: " + hasHeavyFrame + " and having tinted glass: " + hasTintedGlass);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
            return;
        }
    }
}
