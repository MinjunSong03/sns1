package com.example.sns1.answer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerHistoryRepository extends JpaRepository<AnswerHistory, Long> {
    @Modifying
    @Query("update AnswerHistory ah set ah.modifier = null where ah.modifier.id = :userId")
    void updateModifierToNull(@Param("userId") Long userId);
}
