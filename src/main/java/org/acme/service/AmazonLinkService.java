package org.acme.service;

import java.util.regex.Pattern;
import org.acme.model.LinkType;
import org.acme.model.ProcessedLink;
import org.acme.util.HttpRedirectFollower;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmazonLinkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonLinkService.class);

    // Enhanced regex to handle various Amazon URL formats
    private static final String AMAZON_REGEX =
            "https?://(?:www\\.)?(amazon\\.[a-z.]{2,6}|a\\.[a-z.]{2,6}).*?(?:/dp/|/gp/product/)([A-Z0-9]{10}).*";

    // Regex for a.co short links
    private static final String AMAZON_SHORT_REGEX = "https?://a\\.co/.*";

    private static final Pattern AMAZON_PATTERN =
            Pattern.compile(AMAZON_REGEX, Pattern.CASE_INSENSITIVE);
    private static final Pattern AMAZON_SHORT_PATTERN =
            Pattern.compile(AMAZON_SHORT_REGEX, Pattern.CASE_INSENSITIVE);

    private final HttpRedirectFollower redirectFollower;
    private final AffiliateService affiliateService;

    public AmazonLinkService(
            HttpRedirectFollower redirectFollower, AffiliateService affiliateService) {
        this.redirectFollower = redirectFollower;
        this.affiliateService = affiliateService;
    }

    public ProcessedLink processAmazonUrl(String url) {
        if (url == null || url.isEmpty()) {
            return ProcessedLink.failed(url, LinkType.UNKNOWN);
        }

        LOGGER.debug("Processing Amazon URL: {}", url);

        LinkType linkType = detectLinkType(url);

        return switch (linkType) {
            case AMAZON_STANDARD -> processStandardAmazonUrl(url);
            case AMAZON_SHORT -> processShortAmazonUrl(url);
            default -> ProcessedLink.failed(url, linkType);
        };
    }

    /** Detects the type of Amazon link. */
    private LinkType detectLinkType(String url) {
        if (AMAZON_SHORT_PATTERN.matcher(url).matches()) {
            return LinkType.AMAZON_SHORT;
        }
        if (AMAZON_PATTERN.matcher(url).matches()) {
            return LinkType.AMAZON_STANDARD;
        }
        if (isAmazonDomain(url)) {
            return LinkType.AMAZON_STANDARD; // Might be a different format
        }
        return LinkType.NON_AMAZON;
    }

    /** Processes standard Amazon URLs (amazon.com/dp/, amazon.co.uk/gp/product/, etc.) */
    private ProcessedLink processStandardAmazonUrl(String url) {
        var matcher = AMAZON_PATTERN.matcher(url);

        if (matcher.matches()) {
            String domain = matcher.group(1);
            String asin = matcher.group(2);
            String affiliateUrl = affiliateService.addAffiliateTag(url, asin);

            LOGGER.info("Processed standard Amazon URL - ASIN: {}, Domain: {}", asin, domain);
            return ProcessedLink.success(
                    url, url, asin, domain, LinkType.AMAZON_STANDARD, affiliateUrl);
        }

        return ProcessedLink.failed(url, LinkType.AMAZON_STANDARD);
    }

    /** Processes short Amazon URLs (a.co) by following redirects. */
    private ProcessedLink processShortAmazonUrl(String url) {
        LOGGER.debug("Processing short Amazon URL, following redirects: {}", url);

        var finalUrl = redirectFollower.followRedirects(url);

        if (finalUrl.equals(url)) {
            LOGGER.warn("No redirect found for short URL: {}", url);
            return ProcessedLink.failed(url, LinkType.AMAZON_SHORT);
        }

        LOGGER.debug("Short URL resolved to: {}", finalUrl);

        // Now process the final URL as a standard Amazon URL
        var matcher = AMAZON_PATTERN.matcher(finalUrl);

        if (matcher.matches()) {
            var domain = matcher.group(1);
            var asin = matcher.group(2);
            var affiliateUrl = affiliateService.addAffiliateTag(finalUrl, asin);

            LOGGER.info("Processed short Amazon URL - ASIN: {}, Domain: {}", asin, domain);
            return ProcessedLink.success(
                    url, finalUrl, asin, domain, LinkType.AMAZON_SHORT, affiliateUrl);
        }

        return ProcessedLink.failed(url, LinkType.AMAZON_SHORT);
    }

    /** Checks if URL belongs to Amazon domain (basic check). */
    private boolean isAmazonDomain(String url) {
        return url.toLowerCase().contains("amazon.") || url.toLowerCase().contains("a.co");
    }

    /** Checks if a URL is an Amazon URL (any type). */
    public boolean isAmazonUrl(String url) {
        return detectLinkType(url) != LinkType.NON_AMAZON;
    }
}
