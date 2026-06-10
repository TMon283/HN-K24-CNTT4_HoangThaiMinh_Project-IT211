package com.badminton.mapper;

import com.badminton.dto.response.CourtImageResponse;
import com.badminton.entity.CourtImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CourtImageMapper {

    @Mapping(target = "courtId", source = "court.id")
    CourtImageResponse toResponse(CourtImage courtImage);

    default List<CourtImageResponse> toResponseList(List<CourtImage> images) {
        return images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
