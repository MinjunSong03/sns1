package com.example.sns1.post;

import lombok.Getter;
import lombok.Builder;

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
    private String imgUrl;
    private AuthorDto author;
    private List<AnswerDto> answerList;

    @Getter
    @Builder
    public static class AuthorDto {
        private Long id;
        private String username;
    }

    @Getter
    @Builder
    public static class AnswerDto {
        private Long id;
        private String content;
        private String username;
        private String createDate;
        private Long postId;
    }

    public static PostResponseDto from(Post post) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return PostResponseDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .imgUrl(post.getImgUrl())
                .createDate(post.getCreateDate() != null ? 
                        post.getCreateDate().format(formatter) : "")
                .author(post.getAuthor() != null ? 
                        AuthorDto.builder()
                            .id(post.getAuthor().getId())
                            .username(post.getAuthor().getUsername())
                            .build() 
                        : null)
                .answerList(post.getAnswerList() != null ? 
                        post.getAnswerList().stream().map(answer -> AnswerDto.builder()
                            .id(answer.getId())
                            .content(answer.getContent())
                            .username(answer.getAuthor() != null ? answer.getAuthor().getUsername() : "알 수 없음")
                            .createDate(answer.getCreateDate() != null ? 
                                    answer.getCreateDate().format(formatter) : "")
                            .postId(post.getId())
                            .build())
                        .collect(Collectors.toList()) 
                        : new ArrayList<>())
                .build();
    }
}
