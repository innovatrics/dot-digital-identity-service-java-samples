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

/**
 * This example demonstrates usage of face operations API for evaluating face similarity of an image with generated
 * template. Templates can be used instead of images for performance optimization. Templates are incompatible across
 * major product upgrades.
 */
public class FacesSimilarityImageToTemplate extends BaseApiTest<FaceOperationsApi> {
    private static final Logger log = LogManager.getLogger(FacesSimilarityImageToTemplate.class);

    public FacesSimilarityImageToTemplate(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Executes a test to evaluate face similarity between an image and a generated face template.
     *
     * @throws ApiException if an API error occurs.
     */
    @Override
    protected void doTest() throws ApiException {
        String probeFaceId = getApi().detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL))).getId();

        byte[] template = createTemplate(configuration, getApi());

        FaceSimilarityResponse faceSimilarityResponse = getApi()
                .checkSimilarity1(probeFaceId, new FaceSimilarityRequest().referenceFaceTemplate(template));

        log.info(faceSimilarityResponse.toString());
    }

    /**
     * Generates a face template from an example image.
     *
     * @param configuration The configuration object containing essential settings and URLs.
     * @param faceApi The API instance for interacting with face operations.
     * @return A byte array representing the generated face template.
     * @throws ApiException If an error occurs during the API call.
     */
    private static byte[] createTemplate(Configuration configuration, FaceOperationsApi faceApi) throws ApiException {
        String id = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL))).getId();
        return faceApi.createTemplate(id).getData();
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new FacesSimilarityImageToTemplate(new Configuration()).test();
    }
}
