package com.innovatrics.integrationsamples.testhelper;

import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.JSON;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * Abstract base class for testing purposes.
 */
public abstract class BaseApiTest<T> {
    private static final Logger log = LogManager.getLogger(BaseApiTest.class);

    public static final String REMOTE_LOCATION_PATTERN = "^https?://.*$";
    private static final Pattern pattern = Pattern.compile(REMOTE_LOCATION_PATTERN);

    protected T api;
    protected final Configuration configuration;
    protected ApiClient apiClient;

    public BaseApiTest(final Configuration configuration) throws ReflectiveOperationException {
        this.configuration = configuration;
        setup();
    }

    public T getApi() {
        return api;
    }

    public void setup() throws ReflectiveOperationException {
        apiClient = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        apiClient.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        JSON.setGson(new GsonBuilder()
                        .registerTypeAdapter(byte[].class, new DataTypeDeserializer())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
                .setStrictness(Strictness.LENIENT)
                .setPrettyPrinting().create());

        Class<T> apiType;
        Type genericSuperclass = getClass().getGenericSuperclass();
        while ( ! (genericSuperclass instanceof ParameterizedType )) {
            genericSuperclass = ((Class<?>) genericSuperclass).getGenericSuperclass();
        }

        Type genericSuperclassType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        apiType = (Class<T>) genericSuperclassType;
        api = apiType.getConstructor(ApiClient.class).newInstance(apiClient);
    }

    /**
     * Executes the specific test logic defined in the abstract method doTest.
     * Catches and logs exceptions that occur during the test execution.
     * If an ApiException is thrown, logs the HTTP status code and response body.
     * If a general exception is thrown, logs the cause of the exception.
     */
    public void test() {
        try {
            doTest();
        } catch (ApiException apiException) {
            log.error("Request to server failed with code: {} and response: {}", apiException.getCode(), apiException.getResponseBody());
        } catch (Exception genericException) {
            log.error("General error: {}", String.valueOf(genericException.getCause()), genericException);
        }
    }

    /**
     * An abstract method to be implemented for executing specific test logic.
     *
     * @throws ApiException if an API-related error occurs
     * @throws URISyntaxException if there is an issue with the syntax of a URI
     * @throws IOException if an I/O error occurs
     */
    protected abstract void doTest() throws ApiException, URISyntaxException, IOException;


    /**
     * Retrieves the image bytes from a specified resource name.
     *
     * @param resourceName the name of the resource to retrieve
     * @return a byte array containing the image data
     * @throws IOException if an I/O error occurs during resource retrieval
     * @throws MissingResource if the resource cannot be found
     */
    protected byte[] getDetectionImage(final String resourceName) throws IOException {
        return loadImageFromLocation(resourceName);
    }

    /**
     * Validates that the given resource is not null. Logs an error and throws a MissingResource exception if the resource is null.
     *
     * @param resourceName the name of the resource to validate
     * @param resource the URL of the resource to validate
     */
    protected void checkResourceIsValid(String resourceName, URL resource) {
        if ( resource == null ) {
            log.error("Resource {} does not exists.", resourceName);
            throw new MissingResource(resourceName);
        }
    }

    /**
     * Loads an image from a specified URL location.
     *
     * @param location the location of the image resource to load
     * @return a byte array containing the image data
     * @throws IOException if an I/O error occurs during resource retrieval
     */
    protected byte[] loadImageFromLocation(final String location) throws IOException {
        if (location == null) {
            throw new MissingResource("No location provided.");
        }

        if (isRemoteLocation(location)) {
            return loadImageFromURL(location);
        }

        URL resource = getClass().getClassLoader().getResource(location);
        checkResourceIsValid(location, resource);

        try (final InputStream is = Objects.requireNonNull(resource).openStream()){
            return is.readAllBytes();
        }
    }

    /**
     * Loads an image from a specified URL.
     *
     * @param fileName the URL of the image resource to load
     * @return a byte array containing the image data
     * @throws IOException if an I/O error occurs during resource retrieval
     * @throws MissingResource if the fileName is null
     */
    private byte[] loadImageFromURL(String fileName) throws IOException {
        if (fileName == null) {
            throw new MissingResource("No fileName provided.");
        }

        URL url = URI.create(fileName).toURL();
        try (final InputStream is = Objects.requireNonNull(url).openStream()){
            return is.readAllBytes();
        }
    }

    /**
     * Retrieves the image bytes for an L0 document given its image ID.
     *
     * @param imageId the unique identifier for the L0 document image
     * @return a byte array containing the image data
     * @throws IOException if an I/O error occurs while retrieving the image
     */
    protected byte[] getL0DocumentImage(final String imageId) throws IOException {
        return loadImageFromLocation(isRemoteLocation(imageId) ? imageId : String.format("images/documents/L0/%s.jpeg", imageId));
    }

    /**
     * Retrieves the image bytes for an L1 document given its image ID.
     *
     * @param imageId the unique identifier for the L1 document image
     * @return a byte array containing the image data
     * @throws IOException if an I/O error occurs while retrieving the image
     */
    protected byte[] getL1DocumentImage(final String imageId) throws IOException {
        return loadImageFromLocation(isRemoteLocation(imageId) ? imageId : String.format("images/documents/L1/%s.jpeg", imageId));
    }

    /**
     * Retrieves the image bytes for an L2 document given its image ID.
     *
     * @param imageId the unique identifier for the L2 document image
     * @return a byte array containing the image data
     * @throws IOException if an I/O error occurs while retrieving the image
     */
    protected byte[] getL2DocumentImage(final String imageId) throws IOException {
        return loadImageFromLocation(isRemoteLocation(imageId) ? imageId : String.format("images/documents/L2/%s.jpeg", imageId));
    }

    /**
     * Retrieves the image bytes for a face image given its image ID.
     *
     * @param imageId the unique identifier for the face image
     * @return a byte array containing the image data
     * @throws IOException if an I/O error occurs while retrieving the image
     */
    protected byte[] getFacesImage(final String imageId) throws IOException {
        return loadImageFromLocation(isRemoteLocation(imageId) ? imageId : String.format("images/faces/%s.jpeg", imageId));
    }

    /**
     * Checks if the given location is a remote HTTP or HTTPS URL.
     *
     * @param location the location string to check
     * @return true if the location is a remote HTTP or HTTPS URL, false otherwise
     */
    protected boolean isRemoteLocation(String location) {
        return pattern.matcher(location).matches();
    }

    /**
     * Prepares the specified output directory by creating it if it does not already exist.
     *
     * @param outputDirectory the path of the directory to prepare
     */
    protected void prepareOutputDirectory(final String outputDirectory) {
        File resultDirectory = new File(outputDirectory);
        if (!(resultDirectory.exists() && resultDirectory.isDirectory())) {
            resultDirectory.mkdir();
        }
    }

    /**
     * Saves the given image to a file with the specified name in the "onboardingImages" directory.
     *
     * @param image    the byte array representing the image to be saved
     * @param fileName the name of the file to save the image as, including the file extension
     * @throws IOException if an error occurs during writing the image to the file
     */
    protected void saveImage(byte[] image, String outputFolder, String fileName) throws IOException {
        prepareOutputDirectory(outputFolder);

        try ( ByteArrayInputStream bis = new ByteArrayInputStream(image)) {
            BufferedImage bImage2 = ImageIO.read(bis);
            ImageIO.write(bImage2, "png", new File(outputFolder + FileSystems.getDefault().getSeparator() + fileName));
        }
    }

    protected File getBinaryFile(String path) {
        if (path == null) {
            throw new RuntimeException("No path provided.");
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("File is missing: " + path);
        }
        return file;
    }
}
