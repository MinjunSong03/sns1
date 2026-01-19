package com.example.sns1.post;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

import com.example.sns1.user.UserData;

import java.util.UUID;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    public List<Post> getList() {
        return this.postRepository.findAllByOrderByIdDesc();
    }

    public Post create(String content, UserData userData, MultipartFile file) throws IOException {
        Post post = new Post();
        post.setContent(content);
        post.setCreateDate(LocalDateTime.now());
        post.setAuthor(userData);
        if (file != null && !file.isEmpty()) {
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/files"; 
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

    public Post getPost(Long id) {
        Optional<Post> post = this.postRepository.findById(id);
        
        if (post.isPresent()) {
            return post.get();
        } else {
            throw new RuntimeException("Post not found");
        }
    }
}
