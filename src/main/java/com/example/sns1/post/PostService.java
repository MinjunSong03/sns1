package com.example.sns1.post;

import com.example.sns1.user.UserData;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostHistoryRepository postHistoryRepository;

    public List<Post> getList() {
        return this.postRepository.findAllByOrderByIdDesc();
    }

    public Post create(String content, UserData userData, MultipartFile file) throws IOException {
        Post post = new Post();
        post.setContent(content);
        post.setCreateDate(LocalDateTime.now());
        post.setAuthor(userData);
        if (file != null && !file.isEmpty()) {
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files/"; 
            File saveFile = new File(projectPath);
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }
            UUID uuid = UUID.randomUUID();
            String fileName = uuid + "_" + file.getOriginalFilename();
            File destination = new File(projectPath, fileName);
            file.transferTo(destination);
            post.setImgUrl("/files/" + fileName);
        }
        return this.postRepository.save(post);
    }

    public Post getPost(Long postId) {
        return this.postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = this.postRepository.findById(postId)
                    .orElseThrow(()->new RuntimeException("Post not found"));
        if (post.getDeletedAt() != null) {
            throw new RuntimeException("이미 삭제된 게시물입니다.");
        }
        if (post.getAuthor() == null) {
            throw new RuntimeException("게시물 삭제 권한이 없습니다.");
        }
        if (post.getAuthor() == null || !post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("게시물 삭제 권한이 없습니다.");
        }
        post.setDeletedAt(LocalDateTime.now());
        this.postRepository.save(post);
    }

    @Transactional
    public void modifyPost(Long userId, Long postId, String newContent, MultipartFile newFile, Boolean deleteImg) throws IOException {
        Post post = this.postRepository.findById(postId)
                        .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("게시물 수정 권한이 없습니다.");
        }

        if (post.getDeletedAt() != null) {
            throw new RuntimeException("삭제된 게시물입니다.");
        }
        
        PostHistory postHistory = new PostHistory();
        postHistory.setPost(post);
        postHistory.setContent(post.getContent());
        postHistory.setImgUrl(post.getImgUrl());
        postHistory.setModifiedAt(LocalDateTime.now());
        postHistory.setModifier(post.getAuthor());

        this.postHistoryRepository.save(postHistory);

        post.setContent(newContent);
        
        if (newFile != null && !newFile.isEmpty()) {
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files/";
            File saveFile = new File(projectPath);
            if (!saveFile.exists()) {
                saveFile.mkdirs();
            }

            UUID uuid = UUID.randomUUID();
            String fileName = uuid + "_" + newFile.getOriginalFilename();
            File destination = new File(projectPath, fileName);
            newFile.transferTo(destination);

            post.setImgUrl("/files/" + fileName);
        } else if (deleteImg != null && deleteImg) {
            post.setImgUrl(null);
        }
    }
}
