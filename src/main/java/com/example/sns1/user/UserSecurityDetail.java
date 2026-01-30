package com.example.sns1.user;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSecurityDetail extends User {

    private Long id;
    private String nickname; 

    public UserSecurityDetail(String email, String password, Collection<? extends GrantedAuthority> authorities, Long id, String nickname) {
        super(email, password, authorities);
        this.id = id;
        this.nickname = nickname;
    }
}