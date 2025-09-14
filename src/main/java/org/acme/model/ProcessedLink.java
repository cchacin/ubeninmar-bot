package org.acme.model;

public record ProcessedLink(
        String originalUrl,
        String finalUrl,
        String asin,
        String domain,
        LinkType type,
        boolean processed,
        String affiliateUrl) {

    public static ProcessedLink failed(String originalUrl, LinkType type) {
        return new ProcessedLink(originalUrl, null, null, null, type, false, null);
    }

    public static ProcessedLink success(
            String originalUrl,
            String finalUrl,
            String asin,
            String domain,
            LinkType type,
            String affiliateUrl) {
        return new ProcessedLink(originalUrl, finalUrl, asin, domain, type, true, affiliateUrl);
    }
}
