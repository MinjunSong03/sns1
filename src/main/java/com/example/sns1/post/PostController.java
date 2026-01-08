package com.example.sns1.post;

import com.example.sns1.user.UserData;
import com.example.sns1.user.UserService;

import java.util.stream.Collectors;
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


import java.security.Principal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String mainpage(Model model) {
        List<Post> postList = this.postService.getList();
        model.addAttribute("postList", postList);
        return "mainpage";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/post/create")
    @ResponseBody
    public ResponseEntity<?> createPostApi(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("내용을 입력해주세요.");
        }

        try {
            UserData userData = this.userService.getUser(principal.getName());
            
            Post savedPost = this.postService.create(content, userData, file);
            
            return ResponseEntity.ok(PostResponseDto.from(savedPost));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 중 오류가 발생했습니다.");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/post")
    @ResponseBody
    public ResponseEntity<List<PostResponseDto>> getPostListApi() {
        
        List<PostResponseDto> result = postService.getList().stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}