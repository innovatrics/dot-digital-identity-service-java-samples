package com.innovatrics.integrationsamples.faceoperations;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateFaceRequest;
import com.innovatrics.dot.integrationsamples.disapi.model.CropCoordinatesResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.FaceOperationsApi;
import com.innovatrics.dot.integrationsamples.disapi.model.Image;
import com.innovatrics.dot.integrationsamples.disapi.model.ImageCrop;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
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
public class FaceImageCrops extends BaseApiTest<FaceOperationsApi> {
    private static final Logger log = LogManager.getLogger(FaceImageCrops.class);

    private static final int FORCED_WIDTH = 800;
    private static final int FORCED_HEIGHT = 800;

    public FaceImageCrops(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * Detects the face in the image and logs the coordinates of the cropped image.
     * If an attempt is made to crop with incorrect dimensions, the cropping
     * should still be performed correctly. The image size will be aligned, and the image will be resized.
     *
     * @throws ApiException if an error occurs during any API call
     * @throws IOException if an I/O error occurs during file operations
     */
    @Override
    protected void doTest() throws ApiException, IOException {
        String faceId = getApi().detectFace(new CreateFaceRequest().image(new Image().url(configuration.EXAMPLE_IMAGE_URL))).getId();
        log.info("Face detected with id: {}", faceId);

        CropCoordinatesResponse cropCoordinatesResponse = getApi().doCropCoordinates(faceId);
        log.info("Face crop found with face Fully Present: {} on coordinates: \n{}", cropCoordinatesResponse.getFullyCaptured(), cropCoordinatesResponse.getCoordinates());

        String initialFileName = "croppedFaceImage.png";
        performCrop(faceId, null, null, initialFileName);

        String fileNameForWidth = "croppedFaceImage_width_" + FORCED_WIDTH + ".png";
        performCrop(faceId, null, FORCED_WIDTH, fileNameForWidth);

        String fileNameForHeight = "croppedFaceImage_height_" + FORCED_HEIGHT + ".png";
        performCrop(faceId, FORCED_HEIGHT, null, fileNameForHeight);

        // removed background

        String fileNameForBackground = "removedBackgroundFaceImage.png";
        performCropWithBackgroundRemoval(faceId, null, null, fileNameForBackground);

        String fileNameForBackgroundWidth = "removedBackgroundFaceImage_width_" + FORCED_WIDTH + ".png";
        performCropWithBackgroundRemoval(faceId, null, FORCED_WIDTH, fileNameForBackgroundWidth);

        String fileNameForBackgroundHeight = "removedBackgroundFaceImage_height_" + FORCED_HEIGHT + ".png";
        performCropWithBackgroundRemoval(faceId, FORCED_HEIGHT, null, fileNameForBackgroundHeight);
    }

    /**
     * Performs a cropping operation on a face image identified by the given face ID and saves the cropped image to a file.
     *
     * @param faceId the identifier of the face image to be cropped
     * @param horizontalLength the width of the resulting cropped image
     * @param verticalLength the height of the resulting cropped image
     * @param fileName the name of the file where the cropped image will be saved
     * @throws ApiException if an error occurs while attempting to crop the image using the API
     * @throws IOException if an error occurs during the saving of the image
     */
    private void performCrop(String faceId, Integer horizontalLength, Integer verticalLength, String fileName) throws ApiException, IOException {
        log.info("Calling crop for face with id: {}, resulting height: {}, width: {}", faceId, horizontalLength, verticalLength);
        ImageCrop crop = getApi().doCrop(faceId, horizontalLength, verticalLength);
        saveImage(crop.getData(), fileName);
        log.info("Face image crop obtained and stored in faceImageCropsOutput/{}", fileName);
    }

    /**
     * Performs a crop operation on a face image while removing the background.
     *
     * @param faceId the identifier of the face image to be processed
     * @param horizontalLength the desired width of the resulting cropped image
     * @param verticalLength the desired height of the resulting cropped image
     * @param fileName the name of the file where the resulting image will be saved
     * @throws ApiException if an error occurs while attempting to crop the image via the API
     * @throws IOException if an I/O error occurs during the saving of the image
     */
    private void performCropWithBackgroundRemoval(String faceId, Integer horizontalLength, Integer verticalLength, String fileName) throws ApiException, IOException {
        log.info("Calling crop with removed background for face with id: {}, resulting height: {}, width: {}", faceId, horizontalLength, verticalLength);
        ImageCrop crop = getApi().doCropRemoveBackground(faceId, horizontalLength, verticalLength);
        saveImage(crop.getData(), fileName);
        log.info("Face image removed background crop obtained and stored in faceImageCropsOutput/{}", fileName);
    }

    /**
     * Saves the given image data to a specified file.
     * This method first prepares the output directory where the image will be saved.
     * It then reads the image data from a byte array and writes it to a file
     * in the "faceImageCropsOutput" directory with the given file name.
     *
     * @param image the byte array representing the image data to be saved
     * @param fileName the name of the file to which the image data is to be saved
     * @throws IOException if an I/O error occurs during the saving of the image
     */
    private void saveImage(byte[] image, String fileName) throws IOException {
        prepareOutputDirectory("faceImageCropsOutput");

        ByteArrayInputStream bis = new ByteArrayInputStream(image);
        BufferedImage bImage2 = ImageIO.read(bis);
        ImageIO.write(bImage2, "png", new File("faceImageCropsOutput/" + fileName));
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new FaceImageCrops(new Configuration()).test();
    }
}
