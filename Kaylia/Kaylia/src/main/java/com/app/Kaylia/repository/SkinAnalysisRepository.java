package com.app.Kaylia.repository;

import com.app.Kaylia.model.SkinAnalysis;
import com.app.Kaylia.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkinAnalysisRepository extends JpaRepository<SkinAnalysis, Long> {

    // Find latest analysis by user
    @Query("SELECT s FROM SkinAnalysis s WHERE s.user.userId = :userId ORDER BY s.createdAt DESC")
    List<SkinAnalysis> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Find most recent analysis by user
    @Query("SELECT s FROM SkinAnalysis s WHERE s.user.userId = :userId ORDER BY s.createdAt DESC LIMIT 1")
    Optional<SkinAnalysis> findLatestByUserId(@Param("userId") Long userId);

    // Count total analyses
    @Query("SELECT COUNT(s) FROM SkinAnalysis s")
    Long countTotalAnalyses();

    @Transactional
    void deleteByUser(User user);

    SkinAnalysis findByUser(User user);

    Optional<SkinAnalysis> findSkinAnalysisByUser(User user);

}
