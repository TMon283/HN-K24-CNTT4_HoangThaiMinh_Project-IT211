package com.badminton.service.impl;

import com.badminton.dto.response.FileUploadResponse;
import com.badminton.exception.CloudStorageException;
import com.badminton.exception.ValidationException;
import com.badminton.service.FileService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final Cloudinary cloudinary;

    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "badminton/courts",
                            "resource_type", "image"
                    )
            );

            return FileUploadResponse.builder()
                    .secureUrl((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .build();
        } catch (IOException ex) {
            throw new CloudStorageException("Failed to read uploaded file", ex);
        } catch (Exception ex) {
            throw new CloudStorageException("Failed to upload image to Cloudinary: " + ex.getMessage(), ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size must not exceed 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            String allowed = ALLOWED_CONTENT_TYPES.stream()
                    .map(type -> type.replace("image/", ""))
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Only image files are allowed: " + allowed);
        }
    }
}
