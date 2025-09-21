package org.acme.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import org.acme.model.LinkType;
import org.acme.model.ProcessedLink;
import org.acme.util.HttpRedirectFollower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkProcessorTest {

    LinkProcessor linkProcessor;
    HttpResponse<String> response =
            new HttpResponse<String>() {
                @Override
                public int statusCode() {
                    return 200;
                }

                @Override
                public HttpRequest request() {
                    return null;
                }

                @Override
                public Optional<HttpResponse<String>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return null;
                }

                @Override
                public String body() {
                    return "";
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return null;
                }

                @Override
                public HttpClient.Version version() {
                    return null;
                }
            };

    @BeforeEach
    void setUp() {
        var redirectFollower = new HttpRedirectFollower(httpRequest -> response);
        var affiliateService = new AffiliateService("myaffiliate-20");
        var amazonLinkService =
                new AmazonLinkService(
                        redirectFollower::followRedirects, affiliateService::addAffiliateTag);
        this.linkProcessor =
                new LinkProcessor(
                        amazonLinkService::isAmazonUrl, amazonLinkService::processAmazonUrl);
    }

    @Test
    void shouldProcessStandardAmazonUrl() {
        // Given
        var message = "Check out this product: https://amazon.com/dp/B07XYZ1234";

        // When
        var results = linkProcessor.processMessage(message);

        // Then
        assertThat(results).hasSize(1);
        var result = results.getFirst();
        assertThat(result.processed()).isTrue();
        assertThat(result.asin()).isEqualTo("B07XYZ1234");
        assertThat(result.type()).isEqualTo(LinkType.AMAZON_STANDARD);
        assertThat(result.affiliateUrl()).contains("tag=myaffiliate-20");
    }

    @Test
    void shouldFormatResponseCorrectly() {
        // Given
        var processedLink =
                ProcessedLink.success(
                        "https://amazon.com/dp/B07XYZ1234",
                        "https://amazon.com/dp/B07XYZ1234",
                        "B07XYZ1234",
                        "amazon.com",
                        LinkType.AMAZON_STANDARD,
                        "https://amazon.com/dp/B07XYZ1234?tag=ubeferrer-20");

        // When
        var response = linkProcessor.formatResponse(processedLink);

        // Then
        assertThat(response).isEqualTo("💰 https://amazon.com/dp/B07XYZ1234?tag=ubeferrer-20");
    }

    @Test
    void shouldIgnoreNonAmazonUrls() {
        // Given
        var message = "Check out https://google.com and https://github.com";

        // When
        var results = linkProcessor.processMessage(message);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldHandleMultipleAmazonUrls() {
        // Given
        var message =
                "Product 1: https://amazon.com/dp/B07ABC1234 and Product 2:"
                        + " https://amazon.co.uk/gp/product/B07DEF5678";

        // When
        var results = linkProcessor.processMessage(message);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.getFirst().asin()).isEqualTo("B07ABC1234");
        assertThat(results.get(1).asin()).isEqualTo("B07DEF5678");
    }
}
