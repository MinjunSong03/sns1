package com.example.sns1.answer;

import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AnswerResponseDto {
    private Long id;
    private String content;
    private String createDate;
    private Long postId;
    private UserDataDto author;
    private LocalDateTime modifiedAt;
    private LocalDateTime deletedAt;

    @Getter
    @Builder
    public static class UserDataDto {
        private Long id;
        private String username;
    }

    public static AnswerResponseDto from(Answer answer) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return AnswerResponseDto.builder()
                .id(answer.getId())
                .content(answer.getDeletedAt() != null ? "삭제된 댓글입니다." : answer.getContent())
                .createDate(answer.getCreateDate() != null ? 
                        answer.getCreateDate().format(formatter) : "")
                .postId(answer.getPost().getId())
                .author(answer.getAuthor() != null ? 
                        UserDataDto.builder()
                            .id(answer.getAuthor().getId())
                            .username(answer.getAuthor().getUsername())
                            .build() 
                        : null)
                .modifiedAt(answer.getModifiedAt())
                .deletedAt(answer.getDeletedAt())
                .build();
    }
}