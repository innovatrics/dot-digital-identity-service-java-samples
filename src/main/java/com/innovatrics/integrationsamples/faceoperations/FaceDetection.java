package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;


/**
 * This example demonstrates usage of face operations API for detecting faces in image.
 */
public class FaceDetection {
    private static final Logger LOG = LogManager.getLogger(FaceDetection.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final FaceOperationsApi faceApi = new FaceOperationsApi(client);

        try {
            final CreateFaceResponse createFaceByUrl = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL)));
            LOG.info("Face created with id: " + createFaceByUrl.getId() + " with detection confidence: " + createFaceByUrl.getDetection().getConfidence());

            final CreateFaceResponse createFaceFromBytes = faceApi.detect1(new CreateFaceRequest().image(new Image().data(getDetectionImage())));
            LOG.info("Face created with id: " + createFaceFromBytes.getId() + " with detection confidence: " + createFaceFromBytes.getDetection().getConfidence());

            faceApi.deleteFace(createFaceByUrl.getId());
            LOG.info("Deleted face with id: " + createFaceByUrl.getId());
            faceApi.deleteFace(createFaceFromBytes.getId());
            LOG.info("Deleted face with id: " + createFaceFromBytes.getId());
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    private static byte[] getDetectionImage() throws URISyntaxException, IOException {
        final URL resource = FaceDetection.class.getClassLoader().getResource("images/faces/face.jpeg");
        return new FileInputStream(Path.of(resource.toURI()).toFile()).readAllBytes();
    }
}
