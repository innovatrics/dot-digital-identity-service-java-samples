package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceSimilarityRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceSimilarityResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This example demonstrates usage of face operations API for evaluating face similarity of two images.
 */
public class FacesSimilarityImageToImage {
    private static final Logger LOG = LogManager.getLogger(FacesSimilarityImageToImage.class);

    public static void main(String[] args) throws IOException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final FaceOperationsApi faceApi = new FaceOperationsApi(client);

        try {
            String probeFaceId = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.SIMILARITY_REFERENCE_IMAGE_URL))).getId();
            String referenceFaceLink = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.SIMILARITY_REFERENCE_IMAGE_URL))).getLinks().getSelf();

            FaceSimilarityResponse faceSimilarityResponse = faceApi.checkSimilarity(probeFaceId, new FaceSimilarityRequest().referenceFace(referenceFaceLink));

            LOG.info(faceSimilarityResponse.toString());
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }
}
