package org.acme.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AffiliateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffiliateService.class);

    private final String affiliateTag;

    public AffiliateService() {
        this(System.getenv("AFFILIATE_TAG"));
    }

    public AffiliateService(String affiliateTag) {
        this.affiliateTag = affiliateTag;
    }

    /**
     * Adds affiliate tag to an Amazon URL. Handles both URLs with existing query parameters and
     * those without.
     *
     * @param amazonUrl The Amazon URL to add affiliate tag to
     * @param asin The product ASIN
     * @return The URL with affiliate tag added
     */
    public String addAffiliateTag(String amazonUrl, String asin) {
        if (amazonUrl == null || amazonUrl.isEmpty() || asin == null || asin.isEmpty()) {
            LOGGER.warn("Cannot add affiliate tag to invalid URL or ASIN");
            return amazonUrl;
        }

        try {
            // Remove existing tag parameter if present
            var cleanUrl = removeExistingTag(amazonUrl);

            // Determine the separator (? or &) based on whether URL already has query parameters
            var separator = cleanUrl.contains("?") ? "&" : "?";

            // Add the affiliate tag
            var affiliateUrl =
                    cleanUrl
                            + separator
                            + "tag="
                            + URLEncoder.encode(affiliateTag, StandardCharsets.UTF_8);

            LOGGER.debug("Added affiliate tag to URL: {} -> {}", amazonUrl, affiliateUrl);
            return affiliateUrl;

        } catch (Exception e) {
            LOGGER.error("Error adding affiliate tag to URL: {}", amazonUrl, e);
            return amazonUrl;
        }
    }

    /** Removes existing tag parameter from URL to avoid conflicts. */
    private String removeExistingTag(String url) {
        if (url == null) {
            return url;
        }

        // Remove tag=value parameter (handles various formats)
        return url.replaceAll("[?&]tag=[^&]*", "")
                .replaceAll("&+", "&") // Remove multiple consecutive &
                .replaceAll("[?&]$", ""); // Remove trailing ? or &
    }
}
