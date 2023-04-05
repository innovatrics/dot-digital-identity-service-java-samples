package com.innovatrics.integrationsamples;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
    public final String DOT_IDENTITY_SERVICE_URL;
    public final String DOT_AUTHENTICATION_TOKEN;
    public final String LIVENESS_RECORDS_MAGNIFEYE_LIVENESS_BINARY_FILE;
    public final String SIMILARITY_PROBE_IMAGE_URL;
    public final String SIMILARITY_REFERENCE_IMAGE_URL;
    public final String EXAMPLE_IMAGE_URL;
    public final Double ASPECTS_CHECK_AGE_THRESHOLD;
    public final Double ASPECTS_CHECK_GENDER_THRESHOLD;
    public final Double WEARABLES_FACE_MASK_THRESHOLD;
    public final Double WEARABLES_GLASSES_THRESHOLD;
    public final Double WEARABLES_HEAVY_GLASS_FRAME_THRESHOLD;
    public final Double WEARABLES_TINTED_GLASS_THRESHOLD;
    public final Double QUALITY_GLASS_CONDITIONS_DETECTION_CONFIDENCE;
    public final Double QUALITY_GLASS_CONDITIONS_YAW_ANGLE_LOW;
    public final Double QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_LOW;
    public final Double QUALITY_GLASS_CONDITIONS_YAW_ANGLE_HIGH;
    public final Double QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_HIGH;

    public Configuration() throws IOException {
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(Configuration.class.getClassLoader().getResource("application.properties").getPath()));
        DOT_IDENTITY_SERVICE_URL = appProps.getProperty("dot-identity-service-url");
        DOT_AUTHENTICATION_TOKEN = appProps.getProperty("dot-authentication-token");
        LIVENESS_RECORDS_MAGNIFEYE_LIVENESS_BINARY_FILE = appProps.getProperty("liveness-records.magnifeye-liveness.binary-file");
        EXAMPLE_IMAGE_URL = appProps.getProperty("example-image-url");
        SIMILARITY_PROBE_IMAGE_URL = appProps.getProperty("similarity.probe.example-image-url");
        SIMILARITY_REFERENCE_IMAGE_URL = appProps.getProperty("similarity.reference.example-image-url");
        ASPECTS_CHECK_AGE_THRESHOLD = Double.parseDouble(appProps.getProperty("aspects-check.age-threshold"));
        ASPECTS_CHECK_GENDER_THRESHOLD = Double.parseDouble(appProps.getProperty("aspects-check.gender-threshold"));
        WEARABLES_FACE_MASK_THRESHOLD = Double.parseDouble(appProps.getProperty("wearables.face-mask-threshold"));
        WEARABLES_GLASSES_THRESHOLD = Double.parseDouble(appProps.getProperty("wearables.glasses-threshold"));
        WEARABLES_HEAVY_GLASS_FRAME_THRESHOLD = Double.parseDouble(appProps.getProperty("wearables.heavy-glass-frame-threshold"));
        WEARABLES_TINTED_GLASS_THRESHOLD = Double.parseDouble(appProps.getProperty("wearables.tinted-glass-threshold"));
        QUALITY_GLASS_CONDITIONS_DETECTION_CONFIDENCE = Double.parseDouble(appProps.getProperty("quality.glass-conditions.detection-confidence"));
        QUALITY_GLASS_CONDITIONS_YAW_ANGLE_LOW = Double.parseDouble(appProps.getProperty("quality.glass-conditions.yaw-angle.low"));
        QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_LOW = Double.parseDouble(appProps.getProperty("quality.glass-conditions.pitch-angle.low"));
        QUALITY_GLASS_CONDITIONS_YAW_ANGLE_HIGH = Double.parseDouble(appProps.getProperty("quality.glass-conditions.yaw-angle.high"));
        QUALITY_GLASS_CONDITIONS_PITCH_ANGLE_HIGH = Double.parseDouble(appProps.getProperty("quality.glass-conditions.pitch-angle.high"));
    }
}
