package com.badminton.repository;

import com.badminton.entity.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {

    List<CourtImage> findByCourtId(Long courtId);
}
