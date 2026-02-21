package com.mini.buting.api.university.repository;

import com.mini.buting.api.university.domain.College;
import com.mini.buting.api.university.dto.response.CollegeResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollegeRepository extends JpaRepository<College, Long> {

    List<College> findAllByOrderByNameAsc();
}
