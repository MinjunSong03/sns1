package com.example.sns1.answer;

import com.example.sns1.post.Post;
import com.example.sns1.post.PostService;
import com.example.sns1.user.UserData;
import com.example.sns1.user.UserSecurityDetail;
import com.example.sns1.user.UserService;

import jakarta.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;

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
            @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
                return processCreateAnswer(id, content, userSecurityDetail);
            }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/answer/create/{id}")
    @ResponseBody
    public ResponseEntity<?> createAnswerApi(
            @PathVariable("id") Long id,
            @RequestParam("content") String content,
            @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
                return processCreateAnswer(id, content, userSecurityDetail);
            }


    private ResponseEntity<?> processCreateAnswer(Long id, String content, @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("내용을 입력해 주세요.");
        }
        try {
            Post post = this.postService.getPost(id);
            UserData userData = this.userService.getUser(userSecurityDetail.getId());
            Answer answer = this.answerService.create(post, content, userData);
            AnswerResponseDto answerDto = AnswerResponseDto.from(answer);
            messagingTemplate.convertAndSend("/sub/answers", answerDto);
            return ResponseEntity.ok(answerDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("댓글 등록 중 오류가 발생했습니다.");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/delete/{answerId}")
    @ResponseBody
    public ResponseEntity<?> deleteAnswer(@PathVariable("answerId") Long answerId,
                                          @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        try {
            answerService.deleteAnswer(userSecurityDetail.getId(), answerId);
            Answer answer = answerService.getAnswer(answerId);
            AnswerResponseDto answerResponseDto = AnswerResponseDto.from(answer);
            messagingTemplate.convertAndSend("/sub/answers", answerResponseDto);
            return ResponseEntity.ok().body("댓글이 삭제되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("answer/modify/{answerId}")
    @ResponseBody
    public ResponseEntity<?> modifyAnswer(@PathVariable("answerId") Long answerId,
                                          @RequestParam("newContent") String newContent,
                                          @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        try {
            answerService.modifyAnswer(userSecurityDetail.getId(), answerId, newContent);
            Answer answer = answerService.getAnswer(answerId);
            AnswerResponseDto answerResponseDto = AnswerResponseDto.from(answer);
            messagingTemplate.convertAndSend("/sub/answers", answerResponseDto);
            return ResponseEntity.ok().body("댓글이 수정되었습니다.");
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}