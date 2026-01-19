package com.example.sns1.answer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sns1.post.Post;
import com.example.sns1.post.PostService;
import com.example.sns1.user.UserData;
import com.example.sns1.user.UserService;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class AnswerController {
    private final PostService postService;
    private final AnswerService answerService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

        @PreAuthorize("isAuthenticated()")
        @PostMapping("/answer/create/{id}")
        @ResponseBody
        public ResponseEntity<?> createAnswer(
                @PathVariable("id") Long id,
                @RequestParam("content") String content,
                Principal principal) {
                    return processCreateAnswer(id, content, principal);
                }

        @PreAuthorize("isAuthenticated()")
        @PostMapping("/api/answer/create/{id}")
        @ResponseBody
        public ResponseEntity<?> createAnswerApi(
                @PathVariable("id") Long id,
                @RequestParam("content") String content,
                Principal principal) {
                    return processCreateAnswer(id, content, principal);
                }


    private ResponseEntity<?> processCreateAnswer(Long id, String content, Principal principal) {
        if (content == null || content.trim().isEmpty()) {
                            return ResponseEntity.badRequest().body("내용을 입력해 주세요.");
                        }
                        try {
                            Post post = this.postService.getPost(id);
                            UserData userData = this.userService.getUser(principal.getName());
                            Answer answer = this.answerService.create(post, content, userData);
                            AnswerResponseDto answerDto = AnswerResponseDto.from(answer);
                            messagingTemplate.convertAndSend("/sub/answers", answerDto);
                            return ResponseEntity.ok(answerDto);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return ResponseEntity.internalServerError().body("댓글 등록 중 오류가 발생했습니다.");
                        }
                    }
}