package com.example.sns1.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.HashMap;

import org.springframework.dao.DataIntegrityViolationException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

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

    @GetMapping("/user/login")
    public String login() {
        return "loginpage";
    }

    @PostMapping("/api/login")
    @ResponseBody  
    public Map<String, Object> loginApi(@RequestParam("username") String username, 
                                        @RequestParam("password") String password,
                                        HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            request.login(username, password);
            
            response.put("status", "success");
            response.put("message", "로그인 성공");
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        
        return response;
    }
    
}
