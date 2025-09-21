package org.acme.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.acme.model.LinkType;
import org.acme.model.ProcessedLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkProcessor.class);

    // Pattern to extract URLs from text messages
    private static final String URL_REGEX = "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    private final AmazonLinkService amazonLinkService;

    public LinkProcessor(AmazonLinkService amazonLinkService) {
        this.amazonLinkService = amazonLinkService;
    }

    /**
     * Processes a text message to find and process Amazon links.
     *
     * @param message The message text to process
     * @return List of processed links found in the message
     */
    public List<ProcessedLink> processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            LOGGER.debug("Empty or null message received");
            return List.of();
        }

        LOGGER.debug("Processing message: {}", message);

        var urls = extractUrls(message);
        var processedLinks = new ArrayList<ProcessedLink>();

        for (String url : urls) {
            if (amazonLinkService.isAmazonUrl(url)) {
                ProcessedLink processed = amazonLinkService.processAmazonUrl(url);
                processedLinks.add(processed);
                LOGGER.info("Processed Amazon link: {} -> Success: {}", url, processed.processed());
            } else {
                LOGGER.debug("Non-Amazon URL ignored: {}", url);
            }
        }

        return processedLinks;
    }

    /**
     * Processes a single URL directly.
     *
     * @param url The URL to process
     * @return ProcessedLink result
     */
    public ProcessedLink processUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return ProcessedLink.failed(url, LinkType.UNKNOWN);
        }

        if (amazonLinkService.isAmazonUrl(url)) {
            return amazonLinkService.processAmazonUrl(url);
        }
        return ProcessedLink.failed(url, LinkType.NON_AMAZON);
    }

    /** Extracts all URLs from a text message. */
    private List<String> extractUrls(String text) {
        var urls = new ArrayList<String>();
        var matcher = URL_PATTERN.matcher(text);

        while (matcher.find()) {
            urls.add(matcher.group());
        }

        LOGGER.debug("Extracted {} URLs from message", urls.size());
        return urls;
    }

    /**
     * Formats a successful processed link for bot response. Uses the clean & simple format: just
     * the affiliate URL.
     */
    public String formatResponse(ProcessedLink processedLink) {
        if (!processedLink.processed() || processedLink.affiliateUrl() == null) {
            return null; // No response for failed processing
        }

        // Clean & Simple format (Option A from brainstorming)
        return "💰 " + processedLink.affiliateUrl();
    }

    /** Formats multiple processed links for bot response. */
    public List<String> formatResponses(List<ProcessedLink> processedLinks) {
        return processedLinks.stream()
                .filter(ProcessedLink::processed)
                .map(this::formatResponse)
                .filter(Objects::nonNull)
                .toList();
    }
}
