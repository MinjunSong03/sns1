package com.example.sns1.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sns1.jwt.JwtTokenProvider;

import java.util.Map;
import java.util.HashMap;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/user/login")
    public String login() {
        return "loginpage";
    }

    @PostMapping("/api/login")
    @ResponseBody  
    public Map<String, Object> loginApi(@RequestParam("username") String username, 
                                        @RequestParam("password") String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            String jwt = jwtTokenProvider.generateToken(authentication);

            response.put("status", "success");
            response.put("message", "로그인 되었습니다.");
            response.put("token", jwt);
            response.put("username", authentication.getName());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        return response;
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public Map<String, Object> logoutApi() {
        Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "로그아웃 되었습니다.");
            return response;
        }

    @GetMapping("/user/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signuppage";
    }

    @PostMapping("/user/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signuppage";
        }
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", 
                    "비밀번호가 일치하지 않습니다.");
            return "signuppage";
        }
        try {
            userService.create(userCreateForm.getUsername(), 
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        }catch(DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signuppage";
        }catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signuppage";
        }
        return "redirect:/user/login";
    }

    @PostMapping("/api/signup")
    @ResponseBody
    public Map<String, Object> signupApi(@RequestParam("email") String email, 
                                         @RequestParam("password1") String password1,
                                         @RequestParam("password2") String password2,
                                         @RequestParam("username") String username) { 
        Map<String, Object> response = new HashMap<>();

        if (!password1.equals(password2)) {
            response.put("status", "error");
            response.put("message", "비밀번호가 일치하지 않습니다.");
            return response;
        }
        try {
            userService.create(username, email, password1);
            response.put("status", "success");
            response.put("message", "회원가입이 완료되었습니다.");
        } catch (DataIntegrityViolationException e) {
            response.put("status", "error");
            response.put("message", "이미 등록된 사용자입니다.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "회원가입에 실패했습니다.");
        }
        return response;
    }
}
