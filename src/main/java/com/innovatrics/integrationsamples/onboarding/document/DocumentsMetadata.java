package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This example represents sample implementation of getting information about documents which are supported on Level 2 and
 * filtering only Czech Documents.
 * From metadata of these documents sample information is printed into log.
 */
public class DocumentsMetadata {
    private static final Logger LOG = LogManager.getLogger(DocumentsMetadata.class);
    private static final String MRZ = "machineReadableZone";
    private static final String COUNTRY = "CZE";

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);
        client.setBearerToken(configuration.DOT_AUTHENTICATION_TOKEN);
        final MetadataApi metadataApi = new MetadataApi(client);

        try {
            final DocumentMetadataResponse documentMetadataResponse = metadataApi.metadata();

            final List<Document> documentsMetadata = Collections.unmodifiableList(documentMetadataResponse.getDocuments());

            final List<Document> filteredDocumentsMetadata = new ArrayList<>();
            documentsMetadata.forEach(documentMetadata -> {
                String country = documentMetadata.getDocumentType().getCountry();
                if (COUNTRY.equals(country)) {
                    filteredDocumentsMetadata.add(documentMetadata);
                }
            });

            int documentNumber = 0;
            LOG.info("Digital Identity Service support these " + COUNTRY + " documents:");
            for (Document documentMetadata : filteredDocumentsMetadata) {
                boolean hasMRZZone = false;
                for (PageMetadata pageMetadata : documentMetadata.getPages().values()) {
                    if (pageMetadata.getVisualZone().containsKey(MRZ)) {
                        hasMRZZone = true;
                        break;
                    }
                }

                LOG.info(++documentNumber + "/ Document Type: " + documentMetadata.getDocumentType().getType() +
                        "; Document Edition: " + documentMetadata.getDocumentType().getEdition() +
                        "; This document has " + documentMetadata.getPages().size() + " pages and " +
                        (hasMRZZone ? "contains" : "do not contains") + " MRZ Zone");
            }

        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: " + exception.getCode() + " and response: " + exception.getResponseBody());
        }
    }
}
