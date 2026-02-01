package com.example.sns1.post;

import com.example.sns1.user.UserData;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
public class PostHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imgUrl;

    private LocalDateTime modifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_id")
    private UserData modifier;
}