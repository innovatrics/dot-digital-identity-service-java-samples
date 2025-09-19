package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceAspectsResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * FaceAspectsCheck class is responsible for executing a test that detects faces and evaluates their aspects,
 * such as age and gender, using a FaceOperationsApi instance.
 */
public class FaceAspectsCheck extends BaseApiTest<FaceOperationsApi> {
    private static final Logger log = LogManager.getLogger(FaceAspectsCheck.class);

    final Double GENDER_DECISION_THRESHOLD;
    final Double AGE_DECISION_THRESHOLD;
    final String EXAMPLE_IMAGE_URL;

    public FaceAspectsCheck(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);

        GENDER_DECISION_THRESHOLD = configuration.ASPECTS_CHECK_GENDER_THRESHOLD;
        AGE_DECISION_THRESHOLD = configuration.ASPECTS_CHECK_AGE_THRESHOLD;
        EXAMPLE_IMAGE_URL = configuration.EXAMPLE_IMAGE_URL;
    }

    /**
     * Executes a test to detect faces and evaluate their aspects using the FaceOperationsApi.
     * <p>
     * This method utilizes the FaceOperationsApi to detect a face in a specified image URL,
     * logs the detected face ID, evaluates various aspects of the face such as age and gender,
     * and logs the results with appropriate threshold decisions.
     *
     * @throws ApiException If there is an error while communicating with the Face Operations API.
     */
    @Override
    protected void doTest() throws ApiException {
        final String faceId = detectFace(getApi(), EXAMPLE_IMAGE_URL);
        log.info("Face detected with id: {}", faceId);

        FaceAspectsResponse faceAspects = evaluateFaceAspects(getApi(), faceId);

        boolean isAgeAboveThreshold = faceAspects.getAge() >= AGE_DECISION_THRESHOLD;
        String gender = evaluateGender(faceAspects.getGender());

        log.info("Gender is evaluated as: {} with score {} and threshold: {}", gender, faceAspects.getGender(), GENDER_DECISION_THRESHOLD);
        log.info("Face aspects check with detected age of: {} and threshold of: {}", faceAspects.getAge(), AGE_DECISION_THRESHOLD);
        log.info("Age of face on image is: {} than requested threshold.", isAgeAboveThreshold ? "Higher" : "Lower");
    }

    /**
     * Detects a face in the provided image URL using the Face Operations API.
     *
     * @param faceApi  The FaceOperationsApi instance to interact with the face operations service.
     * @param imageUrl The URL of the image in which to detect the face.
     * @return The ID of the detected face.
     * @throws ApiException If there is an error while communicating with the Face Operations API.
     */
    private String detectFace(FaceOperationsApi faceApi, String imageUrl) throws ApiException {
        return faceApi.detectFace(new CreateFaceRequest().image(new Image().url(imageUrl))).getId();
    }

    /**
     * Evaluates the face aspects such as age and gender using the Face Operations API.
     *
     * @param faceApi The FaceOperationsApi instance to interact with the face operations service.
     * @param faceId  The identifier of the face to be evaluated.
     * @return A FaceAspectsResponse object containing the evaluated face aspects data.
     * @throws ApiException If there is an error while communicating with the Face Operations API.
     */
    private FaceAspectsResponse evaluateFaceAspects(FaceOperationsApi faceApi, String faceId) throws ApiException {
        return faceApi.evaluateAspects(faceId);
    }

    /**
     * Evaluates the gender based on the provided gender score.
     *
     * @param genderScore The score indicating the likelihood of the gender being female.
     *                    A higher score increases the probability of the gender being evaluated as female.
     * @return "F" if the gender score is greater than or equal to the GENDER_DECISION_THRESHOLD, otherwise "M".
     */
    private String evaluateGender(Double genderScore) {
        return genderScore >= GENDER_DECISION_THRESHOLD ? "F" : "M";
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new FaceAspectsCheck(new Configuration()).test();
    }
}
