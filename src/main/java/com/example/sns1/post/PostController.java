package com.example.sns1.post;

import com.example.sns1.user.UserData;
import com.example.sns1.user.UserSecurityDetail;
import com.example.sns1.user.UserService;

import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Controller
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String mainpage(Model model) {
        List<Post> postList = this.postService.getList();
        model.addAttribute("postList", postList);
        return "mainpage";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/post")
    @ResponseBody
    public ResponseEntity<List<PostResponseDto>> PostListApi() {
        List<PostResponseDto> result = postService.getList().stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/create")
    @ResponseBody
    public ResponseEntity<?> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        return processCreatePost(content, file, userSecurityDetail);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/post/create")
    @ResponseBody
    public ResponseEntity<?> createPostApi(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        return processCreatePost(content, file, userSecurityDetail);
    }

    private ResponseEntity<?> processCreatePost(String content, MultipartFile file, @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        boolean isContentEmpty = (content == null || content.trim().isEmpty());
        boolean isFileEmpty = (file == null || file.isEmpty());

        if (isContentEmpty && isFileEmpty) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "내용을 입력해 주세요.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            UserData userData = this.userService.getUser(userSecurityDetail.getId());
            Post savedPost = this.postService.create(content, userData, file);
            PostResponseDto responseDto = PostResponseDto.from(savedPost);
            messagingTemplate.convertAndSend("/sub/posts", responseDto);
           return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 중 오류가 발생했습니다.");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/delete/{postId}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable("postId") Long postId,
                                        @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        try {
            postService.deletePost(userSecurityDetail.getId(), postId);
            Post post = postService.getPost(postId); 
            PostResponseDto postResponseDto = PostResponseDto.from(post);
            messagingTemplate.convertAndSend("/sub/posts", postResponseDto);
            return ResponseEntity.ok().body("게시물이 삭제되었습니다.");
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/modify/{postId}")
    @ResponseBody
    public ResponseEntity<?> modifyPost(@PathVariable("postId") Long postId,
                                        @RequestParam("newContent") String newContent,
                                        @RequestParam(value = "newFile", required = false) MultipartFile newFile,
                                        @RequestParam(value = "deleteImg", required = false, defaultValue = "false") Boolean deleteImg,
                                        @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        try {
            postService.modifyPost(userSecurityDetail.getId(), postId, newContent, newFile, deleteImg);
            Post post = postService.getPost(postId);
            PostResponseDto postResponseDto = PostResponseDto.from(post);
            messagingTemplate.convertAndSend("/sub/posts", postResponseDto);
            return ResponseEntity.ok().body("게시물이 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}