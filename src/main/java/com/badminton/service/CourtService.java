package com.badminton.service;

import com.badminton.dto.response.CourtImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourtService {

    List<CourtImageResponse> uploadCourtImages(Long courtId, List<MultipartFile> files);

    List<CourtImageResponse> getCourtImages(Long courtId);
}
