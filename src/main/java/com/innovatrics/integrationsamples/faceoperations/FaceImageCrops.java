package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CropCoordinatesResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.ImageCrop;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * This example demonstrates usage of face operations API for creating face image crops for e.g. users' avatars with
 * various configuration options. Crops should be only used for UI purposes and should not be reused as reference
 * images for re-detection.
 */
public class FaceImageCrops {
    private static final Logger LOG = LogManager.getLogger(FaceImageCrops.class);

    public static void main(String[] args) throws IOException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final FaceOperationsApi faceApi = new FaceOperationsApi(client);

        try {
            String faceId = faceApi.detect1(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL))).getId();
            LOG.info("Face detected with id: " + faceId);

            CropCoordinatesResponse cropCoordinatesResponse = faceApi.doCropCoordinates(faceId);
            LOG.info("Face crop found with face Fully Present: " + cropCoordinatesResponse.getFullyCaptured() + " on coordinates: \n" + cropCoordinatesResponse.getCoordinates());

            String fileName = "croppedFaceImage.png";
            ImageCrop crop = faceApi.doCrop(faceId, null, null);
            saveImage(crop.getData(), fileName);
            LOG.info("Face image crop obtained and stored in faceImageCropsOutput/" + fileName);

            int forcedWidth = 800;
            int forcedHeight = 800;

            String fileNameForWidth = "croppedFaceImage_width_" + forcedWidth + ".png";
            LOG.info("Calling crop for face with id: " + faceId + ", resulting width: " + forcedWidth);
            ImageCrop cropForcedWidth = faceApi.doCrop(faceId, null, forcedWidth);
            saveImage(cropForcedWidth.getData(), fileNameForWidth);
            LOG.info("Face image crop obtained and stored in faceImageCropsOutput/" + fileNameForWidth);

            String fileNameForHeight = "croppedFaceImage_height_" + forcedHeight + ".png";
            LOG.info("Calling crop for face with id: " + faceId + ", resulting heigth: " + forcedHeight);
            ImageCrop cropForcedHeight = faceApi.doCrop(faceId, forcedHeight, null);
            saveImage(cropForcedHeight.getData(), fileNameForHeight);
            LOG.info("Face image crop obtained and stored in faceImageCropsOutput/" + fileNameForHeight);

            // removed background

            String fileNameForBackground = "removedBackgroundFaceImage.png";
            LOG.info("Calling crop with removed background for face with id: " + faceId);
            ImageCrop removeBackgroundResponse = faceApi.doCropRemoveBackground(faceId, null, null);
            saveImage(removeBackgroundResponse.getData(), fileNameForBackground);
            LOG.info("Face image removed background crop obtained and stored in faceImageCropsOutput/" + fileNameForBackground);

            String fileNameForBackgroundWidth = "removedBackgroundFaceImage_width_" + forcedWidth + ".png";
            LOG.info("Calling crop with removed background for face with id: " + faceId + ", resulting width: " + forcedWidth);
            ImageCrop removeBackgroundResponseWidth = faceApi.doCropRemoveBackground(faceId, null, forcedWidth);
            saveImage(removeBackgroundResponseWidth.getData(), fileNameForBackgroundWidth);
            LOG.info("Face image removed background crop obtained and stored in faceImageCropsOutput/" + fileNameForBackgroundWidth);

            String fileNameForBackgroundHeight = "removedBackgroundFaceImage_heigth_" + forcedHeight + ".png";
            LOG.info("Calling crop with removed background for face with id: " + faceId + ", resulting heigth: " + forcedHeight);
            ImageCrop removeBackgroundResponseHeigth = faceApi.doCropRemoveBackground(faceId, forcedHeight, null);
            saveImage(removeBackgroundResponseHeigth.getData(), fileNameForBackgroundHeight);
            LOG.info("Face image removed background crop obtained and stored in faceImageCropsOutput/" + fileNameForBackgroundHeight);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }

    private static void saveImage(byte[] image, String fileName) throws IOException {
        prepareOutputDirectory();

        ByteArrayInputStream bis = new ByteArrayInputStream(image);
        BufferedImage bImage2 = ImageIO.read(bis);
        ImageIO.write(bImage2, "png", new File("faceImageCropsOutput/" + fileName));
    }

    private static void prepareOutputDirectory() {
        File resultDirectory = new File("faceImageCropsOutput");
        if (!(resultDirectory.exists() && resultDirectory.isDirectory())) {
            resultDirectory.mkdir();
        }
    }
}
