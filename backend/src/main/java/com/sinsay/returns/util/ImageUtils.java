package com.sinsay.returns.util;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.Set;

public class ImageUtils {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE);

    public static void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_TYPES.contains(contentType)) {
            throw new UnsupportedMediaTypeStatusException(
                    "Only JPEG and PNG images are supported. Received: " + contentType);
        }
    }
}
