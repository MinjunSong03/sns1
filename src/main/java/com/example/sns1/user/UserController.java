package com.example.sns1.user;

import com.example.sns1.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.Map;
import java.security.Principal;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpMessageSendingOperations messagingTemplate;
    
    @GetMapping("/user/login")
    public String login(Principal principal) {
        if (principal != null) {
        return "redirect:/";
        }
        return "loginpage";
    }

    @PostMapping("/api/user/login")
    @ResponseBody  
    public Map<String, Object> loginApi(@RequestParam("username") String username, 
                                        @RequestParam("password") String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            String jwt = jwtTokenProvider.generateToken(authentication);

            UserSecurityDetail userSecurityDetail = (UserSecurityDetail) authentication.getPrincipal();
            String nickname = userSecurityDetail.getNickname();

            response.put("status", "success");
            response.put("message", "로그인 되었습니다.");
            response.put("token", jwt);
            response.put("username", nickname);
            response.put("email", username);
            response.put("id", userSecurityDetail.getId());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        return response;
    }

    @PostMapping("/api/user/logout")
    @ResponseBody
    public Map<String, Object> logoutApi() {
        Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "로그아웃 되었습니다.");
            return response;
        }

    @GetMapping("/user/signup")
    public String signup(UserCreateForm userCreateForm, 
                         Principal principal) {
        if (principal != null) {
        return "redirect:/";
        }
        return "signuppage";
    }

    @PostMapping("/user/signup")
    public String signup(@Valid UserCreateForm userCreateForm, 
                         BindingResult bindingResult) {
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

    @PostMapping("/api/user/signup")
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

    @GetMapping("/user/detail")
    public String detail(Model model, @AuthenticationPrincipal UserSecurityDetail userSecurityDetail) {
        Long userId = userSecurityDetail.getId();
        UserData userData = this.userService.getUser(userId);
        model.addAttribute("userData", userData);
        return "detail";
    }

    @PostMapping("/user/changeUsername")
    @ResponseBody
    public Map<String, Object> changeUsername(@AuthenticationPrincipal UserSecurityDetail userSecurityDetail, 
                                              @RequestParam("newUsername") String newUsername) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.changeUsername(userSecurityDetail.getId(), newUsername);
            UserData userData = userService.getUser(userSecurityDetail.getId());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserSecurityDetail uSecurityDetail = (UserSecurityDetail) auth.getPrincipal();
            uSecurityDetail.setNickname(newUsername);
            
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                uSecurityDetail, auth.getCredentials(), auth.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("id", userData.getId());
            updatePayload.put("newUsername", newUsername);

            messagingTemplate.convertAndSend("/sub/user-update", updatePayload);

            response.put("status", "success");
            response.put("message", "사용자명이 변경되었습니다.");
            response.put("newUsername", newUsername);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "이미 존재하는 사용자명입니다.");
        }
        return response;
    }

    @PostMapping("/user/changePassword")
    @ResponseBody
    public Map<String, Object> changePassword(@AuthenticationPrincipal UserSecurityDetail userSecurityDetail, 
                                              @RequestParam("currentPassword") String currentPassword,
                                              @RequestParam("newPassword1") String newPassword1,
                                              @RequestParam("newPassword2") String newPassword2,
                                              HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (!newPassword1.equals(newPassword2)) {
             response.put("status", "error");
             response.put("message", "새 비밀번호가 서로 일치하지 않습니다.");
             return response;
        }
        try {
            userService.changePassword(userSecurityDetail.getId(), currentPassword, newPassword1);
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.put("status", "success");
            response.put("message", "비밀번호가 변경되었습니다.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "현재 비밀번호가 일치하지 않습니다.");
        }
        return response;
    }

    @PostMapping("/user/withdrawal")
    @ResponseBody
    public Map<String, Object> withdrawal(@RequestParam("password") String password, 
                                          @AuthenticationPrincipal UserSecurityDetail userSecurityDetail, 
                                          HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserData userData = userService.getUser(userSecurityDetail.getId());
            Long deletedUserId = userData.getId();

            userService.withdrawal(userSecurityDetail.getId(), password);
            SecurityContextHolder.clearContext();

            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("type", "WITHDRAWAL");
            updatePayload.put("id", deletedUserId);
            messagingTemplate.convertAndSend("/sub/user-update", updatePayload);

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.put("status", "success");
            response.put("message", "회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage()); 
        }
        return response;
    }

    @PostMapping("/api/user/changeUsername")
    @ResponseBody
    public Map<String, Object> changeUsernameApi(@AuthenticationPrincipal UserSecurityDetail userSecurityDetail, 
                                                 @RequestParam("newUsername") String newUsername) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.changeUsername(userSecurityDetail.getId(), newUsername);
            UserData userData = userService.getUser(userSecurityDetail.getId());

            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("id", userData.getId());
            updatePayload.put("newUsername", newUsername);
            messagingTemplate.convertAndSend("/sub/user-update", updatePayload);

            response.put("status", "success");
            response.put("message", "사용자명이 변경되었습니다.");
            response.put("newUsername", newUsername);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "이미 존재하는 사용자명입니다.");
        }
        return response;
    }

    @PostMapping("/api/user/changePassword")
    @ResponseBody
    public Map<String, Object> changePasswordApi(@AuthenticationPrincipal UserSecurityDetail userSecurityDetail,
                                                 @RequestParam("currentPassword") String currentPassword,
                                                 @RequestParam("newPassword1") String newPassword1,
                                                 @RequestParam("newPassword2") String newPassword2) {
        Map<String, Object> response = new HashMap<>();

        if (!newPassword1.equals(newPassword2)) {
             response.put("status", "error");
             response.put("message", "새 비밀번호가 서로 일치하지 않습니다.");
             return response;
        }
        try {
            userService.changePassword(userSecurityDetail.getId(), currentPassword, newPassword1);
            response.put("status", "success");
            response.put("message", "비밀번호가 변경되었습니다.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "현재 비밀번호가 일치하지 않습니다.");
        }
        return response;
    }
    
    @PostMapping("/api/user/withdrawal")
    @ResponseBody
    public Map<String, Object> withdrawalApi(@RequestParam("password") String password,
                                             @AuthenticationPrincipal UserSecurityDetail userSecurityDetail, 
                                             HttpServletResponse response,
                                             HttpServletRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            UserData userData = userService.getUser(userSecurityDetail.getId());
            Long deletedUserId = userData.getId();

            userService.withdrawal(userSecurityDetail.getId(), password);

            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("type", "WITHDRAWAL");
            updatePayload.put("id", deletedUserId);
            messagingTemplate.convertAndSend("/sub/user-update", updatePayload);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }

            responseMap.put("status", "success");
            responseMap.put("message", "회원탈퇴가 완료되어 로그아웃 처리되었습니다.");
        } catch (Exception e) {
            responseMap.put("status", "error");
            responseMap.put("message", "비밀번호가 일치하지 않습니다.");
        }
        return responseMap;
    }
}