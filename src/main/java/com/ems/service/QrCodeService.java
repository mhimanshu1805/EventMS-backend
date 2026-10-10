package com.ems.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;

import com.ems.entity.Event;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Builds a PNG QR that points at the public event page: {frontendBaseUrl}/events/{slug}.
 */
@Service
public class QrCodeService {

    private static final int SIZE = 250;

    private final EventService eventService;
    private final PublicEventLinks publicEventLinks;

    public QrCodeService(EventService eventService, PublicEventLinks publicEventLinks) {
        this.eventService = eventService;
        this.publicEventLinks = publicEventLinks;
    }

    public byte[] pngForOwnedEvent(Long eventId) {
        Event event = eventService.requireOwnedEvent(eventId);
        String url = publicEventLinks.forSlug(event.getSlug());
        try {
            BitMatrix matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, SIZE, SIZE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Could not generate QR code", ex);
        }
    }
}
