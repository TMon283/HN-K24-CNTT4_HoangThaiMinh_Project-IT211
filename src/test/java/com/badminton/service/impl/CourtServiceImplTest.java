package com.badminton.service.impl;

import com.badminton.dto.response.CourtImageResponse;
import com.badminton.dto.response.FileUploadResponse;
import com.badminton.entity.Court;
import com.badminton.entity.CourtImage;
import com.badminton.exception.ResourceNotFoundException;
import com.badminton.exception.ValidationException;
import com.badminton.mapper.CourtImageMapper;
import com.badminton.repository.CourtImageRepository;
import com.badminton.repository.CourtRepository;
import com.badminton.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtServiceImplTest {

    @Mock
    private CourtRepository courtRepository;
    @Mock
    private CourtImageRepository courtImageRepository;
    @Mock
    private FileService fileService;
    @Mock
    private CourtImageMapper courtImageMapper;

    @InjectMocks
    private CourtServiceImpl courtService;

    @Test
    void uploadCourtImages_shouldSaveImages_whenCourtExists() {
        Court court = Court.builder().id(1L).name("Court A").pricePerHour(new BigDecimal("150000")).build();
        MultipartFile file = new MockMultipartFile("files", "court.jpg", "image/jpeg", "image-data".getBytes());

        FileUploadResponse uploadResponse = FileUploadResponse.builder()
                .secureUrl("https://res.cloudinary.com/demo/image.jpg")
                .publicId("badminton/courts/court")
                .build();

        CourtImage savedImage = CourtImage.builder()
                .id(1L)
                .court(court)
                .imageUrl(uploadResponse.getSecureUrl())
                .publicId(uploadResponse.getPublicId())
                .build();

        CourtImageResponse imageResponse = CourtImageResponse.builder()
                .id(1L)
                .courtId(1L)
                .imageUrl(uploadResponse.getSecureUrl())
                .build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(fileService.uploadImage(file)).thenReturn(uploadResponse);
        when(courtImageRepository.save(any(CourtImage.class))).thenReturn(savedImage);
        when(courtImageMapper.toResponseList(any())).thenReturn(List.of(imageResponse));

        List<CourtImageResponse> result = courtService.uploadCourtImages(1L, List.of(file));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImageUrl()).contains("cloudinary.com");
        verify(courtImageRepository).save(any(CourtImage.class));
    }

    @Test
    void uploadCourtImages_shouldThrowNotFound_whenCourtMissing() {
        when(courtRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courtService.uploadCourtImages(99L, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void uploadCourtImages_shouldThrowValidation_whenNoFiles() {
        Court court = Court.builder().id(1L).name("Court A").build();
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

        assertThatThrownBy(() -> courtService.uploadCourtImages(1L, List.of()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least one image file is required");
    }

    @Test
    void getCourtImages_shouldReturnImages_whenCourtExists() {
        Court court = Court.builder().id(1L).name("Court A").build();
        CourtImage image = CourtImage.builder().id(1L).court(court).imageUrl("https://example.com/1.jpg").build();

        when(courtRepository.existsById(1L)).thenReturn(true);
        when(courtImageRepository.findByCourtId(1L)).thenReturn(List.of(image));
        when(courtImageMapper.toResponse(image)).thenReturn(CourtImageResponse.builder().id(1L).courtId(1L).build());

        List<CourtImageResponse> result = courtService.getCourtImages(1L);

        assertThat(result).hasSize(1);
    }
}
