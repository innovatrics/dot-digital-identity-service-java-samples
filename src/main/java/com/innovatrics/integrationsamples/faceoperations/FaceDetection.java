package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;


/**
 * This class provides functionality for detecting faces in images using the FaceOperationsApi.
 */
public class FaceDetection extends BaseApiTest<FaceOperationsApi> {
    private static final Logger log = LogManager.getLogger(FaceDetection.class);

    public FaceDetection(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes a series of face detection tests using predefined image sources.
     * This method performs face detection on two different image sources:
     * one specified by a URL and another by a local file path. The face detection
     * results are logged, and the detected faces are subsequently deleted.
     *
     * @throws ApiException If an error occurs during the API call to detect or delete the face.
     * @throws URISyntaxException If the provided URL is invalid.
     * @throws IOException If an I/O error occurs during reading the local image.
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        detectAndLogFace(configuration.EXAMPLE_IMAGE_URL, true);
        detectAndLogFace("images/faces/face.jpeg", false);
    }

    /**
     * Detects a face in the provided image source, logs the detection results,
     * and subsequently deletes the detected face.
     *
     * @param imageSource The source of the image. If isUrl is true, this should be a URL.
     *                    Otherwise, it should be a file path to a local image.
     * @param isUrl A boolean indicating whether the provided imageSource is a URL (true) or a local file path (false).
     * @throws ApiException If an error occurs during the API call to detect or delete the face.
     * @throws IOException If an I/O error occurs during reading the local image.
     * @throws URISyntaxException If the provided URL is invalid.
     */
    private void detectAndLogFace(final String imageSource, boolean isUrl) throws ApiException, IOException, URISyntaxException {
        final CreateFaceResponse response;
        if (isUrl) {
            response = getApi().detect1(new CreateFaceRequest().image(new Image().url(imageSource)));
        } else {
            response = getApi().detect1(new CreateFaceRequest().image(new Image().data(getDetectionImage(imageSource))));
        }

        if( response.getDetection() == null ) {
            throw new ApiException("Detection Object is null");
        }
        log.info("Face created with id: {} with detection confidence: {}", response.getId(), response.getDetection().getConfidence());

        deleteAndLogFace(response.getId());
    }

    /**
     * Deletes the face with the specified faceId and logs the deletion.
     *
     * @param faceId The ID of the face to be deleted.
     * @throws ApiException if there is an error during the API call to delete the face.
     */
    private void deleteAndLogFace(final String faceId) throws ApiException {
        getApi().deleteFace(faceId);
        log.info("Deleted face with id: {}", faceId);
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new FaceDetection(new Configuration()).test();
    }
}
