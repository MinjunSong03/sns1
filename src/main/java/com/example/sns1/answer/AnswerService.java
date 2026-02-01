package com.example.sns1.answer;

import com.example.sns1.post.Post;
import com.example.sns1.user.UserData;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerHistoryRepository answerHistoryRepository;

    public Answer create(Post post, String content, UserData userData) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setPost(post);
        answer.setAuthor(userData);
        return this.answerRepository.save(answer);
    }

    public Answer getAnswer(Long answerId) {
        return this.answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
    }

    @Transactional
    public void deleteAnswer(Long userId, Long AnswerId) {
        Answer answer = this.answerRepository.findById(AnswerId)
                        .orElseThrow(()->new RuntimeException("Answer not found"));
        if (answer.getDeletedAt() != null) {
            throw new RuntimeException("이미 삭제된 댓글입니다.");
        }
        if (answer.getAuthor() == null) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }
        if (answer.getAuthor() == null || !answer.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }
        answer.setDeletedAt(LocalDateTime.now());
        this.answerRepository.save(answer);
    }

    @Transactional
    public void modifyAnswer(Long userId, Long answerId, String newContent) throws IOException {
        Answer answer = this.answerRepository.findById(answerId)
                        .orElseThrow(()->new RuntimeException("Answer not found"));
        if (answer.getAuthor() == null || !answer.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }
        if (answer.getDeletedAt() != null) {
            throw new RuntimeException("삭제된 댓글입니다.");
        }

        AnswerHistory answerHistory = new AnswerHistory();
        answerHistory.setPost(answer.getPost());
        answerHistory.setContent(answer.getContent());
        answerHistory.setModifiedAt(LocalDateTime.now());
        answerHistory.setModifier(answer.getAuthor());

        this.answerHistoryRepository.save(answerHistory);
        answer.setContent(newContent);
    }
}
