package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceSimilarityRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceSimilarityResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * FacesSimilarityImageToImage is a test class that evaluates the similarity
 * between two face images using the FaceOperationsApi.
 */
public class FacesSimilarityImageToImage extends BaseApiTest<FaceOperationsApi> {
    private static final Logger log = LogManager.getLogger(FacesSimilarityImageToImage.class);

    public FacesSimilarityImageToImage(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes a series of operations to test face similarity detection.
     *
     * This method performs the following steps:
     *
     * 1. Detects a face from a reference image URL to obtain a probe face ID.
     * 2. Detects the same face again to obtain a reference face link.
     * 3. Checks the similarity between the probe face and the reference face.
     * 4. Logs the response containing the similarity details.
     *
     * @throws ApiException if there is an error calling the API
     * @throws URISyntaxException if the provided URI is not valid
     * @throws IOException if an I/O exception occurs
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        String probeFaceId = getApi().detectFace(new CreateFaceRequest()
                .image(new Image().url(configuration.SIMILARITY_REFERENCE_IMAGE_URL))
        ).getId();

        String referenceFaceLink = Objects.requireNonNull(getApi().detectFace(new CreateFaceRequest()
                .image(new Image().url(configuration.SIMILARITY_REFERENCE_IMAGE_URL))
        ).getLinks()).getSelf();

        FaceSimilarityResponse faceSimilarityResponse = getApi().checkSimilarity(probeFaceId, new FaceSimilarityRequest().referenceFace(referenceFaceLink));

        log.info(faceSimilarityResponse.toString());
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new FacesSimilarityImageToImage(new Configuration()).test();
    }
}
