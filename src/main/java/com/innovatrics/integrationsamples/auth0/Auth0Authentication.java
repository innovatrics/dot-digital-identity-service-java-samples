package com.innovatrics.integrationsamples.auth0;

import com.innovatrics.dot.integrationsamples.disapi.ApiClient;
import com.innovatrics.dot.integrationsamples.disapi.ApiException;
import com.innovatrics.dot.integrationsamples.disapi.model.CreateCustomerResponse;
import com.innovatrics.dot.integrationsamples.disapi.model.CustomerOnboardingApi;
import com.innovatrics.integrationsamples.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

/**
 * The Auth0Authentication class is responsible for handling the authentication process through Auth0.
 * It includes methods to retrieve tokens from Auth0 and to initiate the customer onboarding process.
 */
public class Auth0Authentication {
    private static final Logger LOG = LogManager.getLogger(Auth0Authentication.class);
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String BASIC = "Basic ";
    private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String GRANT_TYPE = "grant_type=client_credentials&audience=";
    private static final int SUCCESS_CODE = 200;

    /**
     * The main method for establishing a connection to the customer onboarding API service.
     * Configures the API client and retrieves the Auth0 token necessary for authentication.
     * Initializes the customer onboarding process by creating a new customer.
     * Handles exceptions that might occur during API requests.
     *
     * @param args Command-line arguments. They are not used in this implementation.
     * @throws IOException If an input or output exception occurs.
     * @throws ApiException If an API exception occurs during the request to the server.
     */
    public static void main(String[] args) throws IOException, ApiException {
        final Configuration configuration = new Configuration();
        final ApiClient client = new ApiClient().setBasePath(configuration.DOT_IDENTITY_SERVICE_URL);

        String auth0Token = getAuth0Token(
                configuration.DOT_AUTH0_CLIENT_ID,
                configuration.DOT_AUTH0_CLIENT_SECRET,
                configuration.DOT_AUTH0_TOKEN_ENDPOINT,
                configuration.DOT_AUTH0_AUDIENCE
        );

        if (auth0Token.isEmpty()) {
            LOG.error("Failed to obtain Auth0 token");
            return;
        }

        client.setBearerToken(auth0Token);

        try {
            final CustomerOnboardingApi customerOnboardingApi = new CustomerOnboardingApi(client);
            final CreateCustomerResponse customerResponse = customerOnboardingApi.createCustomer();
            String customerId = customerResponse.getId();
            LOG.info("Customer created with id: {}", customerId);
        } catch (ApiException exception) {
            LOG.error("Request to server failed with code: {} and response: {}", exception.getCode(), exception.getResponseBody());
        } catch (Exception e) {
            LOG.error("General error: {}", e.getMessage(), e);
        }
    }

    /**
     * This method retrieves an Auth0 token using a client ID, client secret, token endpoint, and audience.
     *
     * @param clientId The client ID used to authenticate with Auth0.
     * @param clientSecret The client secret used to authenticate with Auth0.
     * @param tokenEndpoint The URL endpoint to request the token from Auth0.
     * @param audience The audience for which the token is requested.
     * @return The Auth0 token as a string. If the token cannot be retrieved, returns an empty string.
     */
    private static String getAuth0Token(String clientId, String clientSecret, String tokenEndpoint, String audience) {
        String auth0Token = "";
        try {
            URL url = new URL(tokenEndpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            con.setRequestProperty(AUTHORIZATION, BASIC + encodedCredentials);
            con.setRequestProperty(CONTENT_TYPE, FORM_URLENCODED);
            String postData = GRANT_TYPE + audience;
            con.getOutputStream().write(postData.getBytes());
            int responseCode = con.getResponseCode();

            if (responseCode == SUCCESS_CODE) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // example response string may looks like this:
                    // {"access_token":"some access token","expires_in":604800,"token_type":"Bearer"}
                    // so we get the index of substring "access token":" and add 16 because
                    // that's where the actual token begins

                    int startIndex = response.indexOf("\"access_token\":\"") + 16;
                    int endIndex = response.indexOf("\"", startIndex);
                    auth0Token = response.substring(startIndex, endIndex);
                    LOG.info("OAuth token: " + auth0Token);
                }
            } else {
                LOG.error("Error getting Auth0 token. Response code: {}", responseCode);
            }
        } catch (MalformedURLException e) {
            LOG.error("Malformed URL exception.", e);
        } catch (Exception e) {
            LOG.error("General error: {}", e.getMessage(), e);
        }
        return auth0Token;
    }
}
