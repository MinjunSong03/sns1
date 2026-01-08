package com.example.sns1.answer;

import com.example.sns1.post.Post;
import com.example.sns1.post.PostResponseDto;
import com.example.sns1.post.PostService;
import com.example.sns1.user.UserData;
import com.example.sns1.user.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Controller
public class AnswerController {

    private final PostService postService;
    private final AnswerService answerService;
    private final UserService userService;

        @PreAuthorize("isAuthenticated()")
        @PostMapping("/api/answer/create/{id}")
        @ResponseBody
        public ResponseEntity<?> createAnswerApi(
                @PathVariable("id") Long id,
                @RequestParam("content") String content,
                Principal principal) {
            
                if (content == null || content.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("내용을 입력해주세요.");
                }

                try {
                    Post post = this.postService.getPost(id);
                    UserData userData = this.userService.getUser(principal.getName());

                    Answer answer = this.answerService.create(post, content, userData);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    
                    PostResponseDto.AnswerDto answerDto = PostResponseDto.AnswerDto.builder()
                            .id(answer.getId())
                            .content(answer.getContent())
                            .username(answer.getAuthor().getUsername())
                            .createDate(answer.getCreateDate().format(formatter))
                            .build();

                    return ResponseEntity.ok(answerDto);

                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.internalServerError().body("댓글 등록 중 오류가 발생했습니다.");
                }
            }
}