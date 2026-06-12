package com.badminton.service.impl;

import com.badminton.dto.response.FileUploadResponse;
import com.badminton.exception.ValidationException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private Cloudinary cloudinary;
    @Mock
    private Uploader uploader;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    void uploadImage_shouldReturnSecureUrl_whenValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "court.jpg", "image/jpeg", "image-content".getBytes());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/demo/image.jpg",
                "public_id", "badminton/courts/image"
        ));

        FileUploadResponse response = fileService.uploadImage(file);

        assertThat(response.getSecureUrl()).contains("cloudinary.com");
        assertThat(response.getPublicId()).isEqualTo("badminton/courts/image");
    }

    @Test
    void uploadImage_shouldThrowValidation_whenFileEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);

        assertThatThrownBy(() -> fileService.uploadImage(file))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("File is required");
    }

    @Test
    void uploadImage_shouldThrowValidation_whenInvalidContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "pdf-content".getBytes());

        assertThatThrownBy(() -> fileService.uploadImage(file))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Only image files are allowed");
    }
}
