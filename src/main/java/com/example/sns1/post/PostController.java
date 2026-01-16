package com.example.sns1.post;

import com.example.sns1.user.UserData;
import com.example.sns1.user.UserService;

import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import java.util.Map;

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
            Principal principal) {
        return processCreatePost(content, file, principal);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/post/create")
    @ResponseBody
    public ResponseEntity<?> createPostApi(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {
        return processCreatePost(content, file, principal);
    }

    private ResponseEntity<?> processCreatePost(String content, MultipartFile file, Principal principal) {
        boolean isContentEmpty = (content == null || content.trim().isEmpty());
        boolean isFileEmpty = (file == null || file.isEmpty());

        if (isContentEmpty && isFileEmpty) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "내용을 입력해 주세요.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            UserData userData = this.userService.getUser(principal.getName());
            Post savedPost = this.postService.create(content, userData, file);
            PostResponseDto responseDto = PostResponseDto.from(savedPost);
            messagingTemplate.convertAndSend("/sub/posts", responseDto);
           return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 중 오류가 발생했습니다.");
        }
    }
}