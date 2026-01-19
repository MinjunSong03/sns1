package com.example.sns1.user;

import org.springframework.stereotype.Service;

import com.example.sns1.DataNotFoundException;

import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserData create(String username, String email, String password) {
        UserData user = new UserData();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreateDate(LocalDateTime.now());
        this.userRepository.save(user);
        return user;
    }

    public UserData getUser(String username) {
        Optional<UserData> userData = this.userRepository.findByUsername(username);
        if (userData.isPresent()) {
            return userData.get();
        } else {
            throw new DataNotFoundException("회원정보를 찾을 수 없습니다.");
        }
    }
}
