package org.acme.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRedirectFollower {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRedirectFollower.class);
    private static final int MAX_REDIRECTS = 5;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;

    public HttpRedirectFollower() {
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(TIMEOUT)
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build();
    }

    /**
     * Follows redirects manually to get the final destination URL. This is needed for a.co links
     * which redirect to the actual Amazon product page.
     *
     * @param originalUrl The original URL to follow
     * @return The final destination URL after following redirects, or original URL if no redirects
     */
    public String followRedirects(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }

        String currentUrl = originalUrl;
        int redirectCount = 0;

        try {
            while (redirectCount < MAX_REDIRECTS) {
                LOGGER.debug("Following redirect #{}: {}", redirectCount + 1, currentUrl);

                HttpRequest request =
                        HttpRequest.newBuilder()
                                .uri(URI.create(currentUrl))
                                .timeout(TIMEOUT)
                                .GET()
                                .build();

                HttpResponse<String> response =
                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                // Check if it's a redirect
                if (statusCode >= 300 && statusCode < 400) {
                    String locationHeader = response.headers().firstValue("Location").orElse(null);
                    if (locationHeader == null) {
                        LOGGER.warn(
                                "Redirect response without Location header for URL: {}",
                                currentUrl);
                        break;
                    }

                    // Handle relative URLs
                    if (locationHeader.startsWith("/")) {
                        URI baseUri = URI.create(currentUrl);
                        locationHeader =
                                baseUri.getScheme() + "://" + baseUri.getHost() + locationHeader;
                    }

                    currentUrl = locationHeader;
                    redirectCount++;
                    LOGGER.debug("Redirected to: {}", currentUrl);
                } else if (statusCode == 200) {
                    // Success - we've reached the final destination
                    LOGGER.debug("Final destination reached: {}", currentUrl);
                    break;
                } else {
                    LOGGER.warn("Unexpected status code {} for URL: {}", statusCode, currentUrl);
                    break;
                }
            }

            if (redirectCount >= MAX_REDIRECTS) {
                LOGGER.warn(
                        "Maximum redirects ({}) reached for URL: {}", MAX_REDIRECTS, originalUrl);
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error following redirects for URL: {}", originalUrl, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return originalUrl; // Return original URL on error
        }

        return currentUrl;
    }
}
