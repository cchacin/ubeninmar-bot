package org.acme.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.acme.model.ProcessedLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LinkProcessor(
        Predicate<String> isAmazonUrl, Function<String, ProcessedLink> processAmazonUrl) {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkProcessor.class);

    // Pattern to extract URLs from text messages
    private static final String URL_REGEX = "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    public List<ProcessedLink> processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            LOGGER.debug("Empty or null message received");
            return List.of();
        }

        LOGGER.debug("Processing message: {}", message);

        var urls = extractUrls(message);
        var processedLinks = new ArrayList<ProcessedLink>();

        for (String url : urls) {
            if (isAmazonUrl.test(url)) {
                ProcessedLink processed = processAmazonUrl.apply(url);
                processedLinks.add(processed);
                LOGGER.info("Processed Amazon link: {} -> Success: {}", url, processed.processed());
            } else {
                LOGGER.debug("Non-Amazon URL ignored: {}", url);
            }
        }

        return processedLinks;
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
    String formatResponse(ProcessedLink processedLink) {
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
