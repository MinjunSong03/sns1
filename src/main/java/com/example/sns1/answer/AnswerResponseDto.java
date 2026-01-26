package com.example.sns1.answer;

import lombok.Getter;
import lombok.Builder;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AnswerResponseDto {
    private Long id;
    private String content;
    private String createDate;
    private Long postId;
    private UserDataDto author;

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
                .content(answer.getContent())
                .createDate(answer.getCreateDate() != null ? 
                        answer.getCreateDate().format(formatter) : "")
                .postId(answer.getPost().getId())
                .author(UserDataDto.builder()
                        .id(answer.getAuthor() != null ? answer.getAuthor().getId() : -1L)
                        .username(answer.getAuthor() != null ? answer.getAuthor().getUsername() : "탈퇴한 사용자")
                        .build())
                .build();
    }
}