package com.example.sns1.answer;

import com.example.sns1.post.Post;
import com.example.sns1.user.UserData;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;

    public Answer create(Post post, String content, UserData userData) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setPost(post);
        answer.setAuthor(userData);
        return this.answerRepository.save(answer);
    }
}
