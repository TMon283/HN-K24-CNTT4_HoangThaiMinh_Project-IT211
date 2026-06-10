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
import com.badminton.service.CourtService;
import com.badminton.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final FileService fileService;
    private final CourtImageMapper courtImageMapper;

    @Override
    @Transactional
    public List<CourtImageResponse> uploadCourtImages(Long courtId, List<MultipartFile> files) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Court not found with id: " + courtId));

        if (files == null || files.isEmpty()) {
            throw new ValidationException("At least one image file is required");
        }

        List<CourtImage> savedImages = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> {
                    FileUploadResponse upload = fileService.uploadImage(file);
                    CourtImage image = CourtImage.builder()
                            .court(court)
                            .imageUrl(upload.getSecureUrl())
                            .publicId(upload.getPublicId())
                            .build();
                    return courtImageRepository.save(image);
                })
                .collect(Collectors.toList());

        return courtImageMapper.toResponseList(savedImages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtImageResponse> getCourtImages(Long courtId) {
        if (!courtRepository.existsById(courtId)) {
            throw new ResourceNotFoundException("Court not found with id: " + courtId);
        }

        return courtImageRepository.findByCourtId(courtId).stream()
                .map(courtImageMapper::toResponse)
                .collect(Collectors.toList());
    }
}
