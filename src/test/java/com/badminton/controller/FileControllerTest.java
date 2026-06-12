package com.badminton.controller;

import com.badminton.dto.response.FileUploadResponse;
import com.badminton.exception.GlobalExceptionHandler;
import com.badminton.service.FileService;
import com.badminton.support.MockMvcTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcTestSupport.buildMockMvc(fileController);
    }

    @Test
    void uploadFile_shouldReturn201_whenValidImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "court.jpg", "image/jpeg", "image-content".getBytes());

        when(fileService.uploadImage(any()))
                .thenReturn(FileUploadResponse.builder()
                        .secureUrl("https://res.cloudinary.com/demo/image.jpg")
                        .publicId("badminton/courts/image")
                        .build());

        mockMvc.perform(multipart("/api/v1/files/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.secureUrl").value("https://res.cloudinary.com/demo/image.jpg"));
    }
}
