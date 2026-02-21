package com.mini.buting.api.analysis.repository;

import com.mini.buting.api.analysis.domain.FaceShape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaceShapeRepository extends JpaRepository<FaceShape, Long> {
    Optional<FaceShape> findByName(String animalType);
}
