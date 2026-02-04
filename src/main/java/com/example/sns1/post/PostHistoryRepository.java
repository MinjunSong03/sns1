package com.example.sns1.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostHistoryRepository extends JpaRepository<PostHistory, Long>{
    @Modifying
    @Query("update PostHistory ph set ph.modifier = null where ph.modifier.id = :userId")
    void updateModifierToNull(@Param("userId") Long userId);
}
