package com.ems.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * One place for the public event address. QR codes and EventResponse.publicUrl
 * both use this so a copied link and a scanned code open the same page.
 */
@Component
public class PublicEventLinks {

    private final String frontendBaseUrl;

    public PublicEventLinks(@Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String forSlug(String slug) {
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return base + "/events/" + slug;
    }
}
