package com.innovatrics.integrationsamples.onboarding.document;

import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.*;
import com.innovatrics.integrationsamples.Configuration;
import com.innovatrics.integrationsamples.testhelper.BaseApiTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * This example represents sample implementation of getting information about documents which are supported on Level 2 and
 * filtering only Czech Documents.
 * From metadata of these documents sample information is printed into log.
 */
public class DocumentsMetadata extends BaseApiTest<MetadataApi> {
    private static final Logger log = LogManager.getLogger(DocumentsMetadata.class);
    private static final String MRZ = "machineReadableZone";
    private static final String COUNTRY = "CZE";

    public DocumentsMetadata(Configuration configuration) throws ReflectiveOperationException {
        super(configuration);
    }

    /**
     * This method retrieves the metadata of all documents supported by the Digital Identity Service.
     * It then filters this list to include only documents from the country defined by the `COUNTRY` constant.
     * For each of these filtered documents, it checks if any of the document's pages contains an MRZ (Machine Readable Zone).
     * The details of each filtered document, including whether it contains an MRZ, the type, and edition, are logged.
     *
     * @throws ApiException if there is an error during the API call
     * @throws URISyntaxException if there is an error in URI syntax
     * @throws IOException if there is an I/O error
     */
    @Override
    protected void doTest() throws ApiException, URISyntaxException, IOException {
        final DocumentMetadataResponse documentMetadataResponse = getApi().metadata();

        final List<Document> documentsMetadata = Collections.unmodifiableList(documentMetadataResponse.getDocuments());

        final List<Document> filteredDocumentsMetadata = documentsMetadata.stream()
                .filter(documentMetadata -> COUNTRY.equals(documentMetadata.getDocumentType().getCountry()))
                .toList();

        int documentNumber = 0;
        log.info("Digital Identity Service support these {} {}", COUNTRY, " documents:");

        for (Document documentMetadata : filteredDocumentsMetadata) {
            boolean hasMRZZone = documentMetadata.getPages().values().stream()
                    .anyMatch(pageMetadata -> pageMetadata.getVisualZone().containsKey(MRZ));

            log.info("{}/ Document Type: {}; Document Edition: {}; This document has {} pages and {} MRZ Zone",
                    ++documentNumber,
                    documentMetadata.getDocumentType().getType(),
                    documentMetadata.getDocumentType().getEdition(),
                    documentMetadata.getPages().size(),
                    hasMRZZone ? "contains" : "do not contains");
        }
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        new DocumentsMetadata(new Configuration()).test();
    }
}
