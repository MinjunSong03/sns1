package com.example.sns1.post;

import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostResponseDto {
    private Long id;
    private String content;
    private String createDate;
    private List<AnswerDto> answerList;
    private UserDataDto author;
    private String imgUrl;
    private LocalDateTime modifiedAt;
    private LocalDateTime deletedAt;

    @Getter
    @Builder
    public static class UserDataDto {
        private Long id;
        private String username;
    }

    @Getter
    @Builder
    public static class AnswerDto {
        private Long id;
        private String content;
        private String createDate;
        private UserDataDto author;
        private Long postId;
        private LocalDateTime modifiedAt;
        private LocalDateTime deletedAt;
    }

    public static PostResponseDto from(Post post) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return PostResponseDto.builder()
                .id(post.getId())
                .content(post.getDeletedAt() != null ? "삭제된 게시물입니다." : post.getContent())
                .createDate(post.getCreateDate() != null ? 
                    post.getCreateDate().format(formatter) : "")
                .answerList(post.getAnswerList() != null ? 
                    post.getAnswerList().stream().map(answer -> AnswerDto.builder()
                        .id(answer.getId())
                        .content(answer.getDeletedAt() != null ? "삭제된 댓글입니다." : answer.getContent())
                        .createDate(answer.getCreateDate() != null ? 
                            answer.getCreateDate().format(formatter) : "")
                        .author(answer.getAuthor() != null ? 
                                UserDataDto.builder()
                                    .id(answer.getAuthor().getId())
                                    .username(answer.getAuthor().getUsername())
                                    .build() 
                                : null)
                        .postId(post.getId())
                        .modifiedAt(answer.getModifiedAt())
                        .deletedAt(answer.getDeletedAt())
                        .build())
                    .collect(Collectors.toList()) 
                    : new ArrayList<>())
                .author(post.getAuthor() != null ? 
                        UserDataDto.builder()
                            .id(post.getAuthor().getId())
                            .username(post.getAuthor().getUsername())
                            .build() 
                        : null)
                .imgUrl(post.getDeletedAt() != null ? null : post.getImgUrl())
                .modifiedAt(post.getModifiedAt())
                .deletedAt(post.getDeletedAt())
                .build();
    }
}
