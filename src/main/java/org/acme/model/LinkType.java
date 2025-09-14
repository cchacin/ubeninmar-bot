package org.acme.model;

public enum LinkType {
    AMAZON_STANDARD, // Regular amazon.com/dp/ or /gp/product/ links
    AMAZON_SHORT, // a.co shortened links
    NON_AMAZON, // Not an Amazon link
    UNKNOWN // Could not determine type
}
