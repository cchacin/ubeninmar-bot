package org.acme.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.acme.model.LinkType;
import org.acme.model.ProcessedLink;
import org.acme.util.HttpRedirectFollower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkProcessorTest {

    private LinkProcessor linkProcessor;

    @BeforeEach
    void setUp() {
        var redirectFollower = new HttpRedirectFollower();
        var affiliateService = new AffiliateService("myaffiliate-20");
        var amazonLinkService = new AmazonLinkService(redirectFollower, affiliateService);
        this.linkProcessor = new LinkProcessor(amazonLinkService);
    }

    @Test
    void shouldProcessStandardAmazonUrl() {
        // Given
        String message = "Check out this product: https://amazon.com/dp/B07XYZ1234";

        // When
        List<ProcessedLink> results = linkProcessor.processMessage(message);

        // Then
        assertThat(results).hasSize(1);
        ProcessedLink result = results.getFirst();
        assertThat(result.processed()).isTrue();
        assertThat(result.asin()).isEqualTo("B07XYZ1234");
        assertThat(result.type()).isEqualTo(LinkType.AMAZON_STANDARD);
        assertThat(result.affiliateUrl()).contains("tag=myaffiliate-20");
    }

    @Test
    void shouldFormatResponseCorrectly() {
        // Given
        ProcessedLink processedLink =
                ProcessedLink.success(
                        "https://amazon.com/dp/B07XYZ1234",
                        "https://amazon.com/dp/B07XYZ1234",
                        "B07XYZ1234",
                        "amazon.com",
                        LinkType.AMAZON_STANDARD,
                        "https://amazon.com/dp/B07XYZ1234?tag=ubeferrer-20");

        // When
        String response = linkProcessor.formatResponse(processedLink);

        // Then
        assertThat(response).isEqualTo("💰 https://amazon.com/dp/B07XYZ1234?tag=ubeferrer-20");
    }

    @Test
    void shouldIgnoreNonAmazonUrls() {
        // Given
        String message = "Check out https://google.com and https://github.com";

        // When
        List<ProcessedLink> results = linkProcessor.processMessage(message);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldHandleMultipleAmazonUrls() {
        // Given
        String message =
                "Product 1: https://amazon.com/dp/B07ABC1234 and Product 2:"
                        + " https://amazon.co.uk/gp/product/B07DEF5678";

        // When
        List<ProcessedLink> results = linkProcessor.processMessage(message);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.getFirst().asin()).isEqualTo("B07ABC1234");
        assertThat(results.get(1).asin()).isEqualTo("B07DEF5678");
    }
}
