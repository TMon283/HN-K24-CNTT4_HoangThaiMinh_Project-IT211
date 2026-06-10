package com.badminton.service;

import com.badminton.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse uploadImage(MultipartFile file);
}
