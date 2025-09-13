package com.app.Kaylia.controller;

import com.app.Kaylia.dto.LoginDto;
import com.app.Kaylia.dto.ResetPasswordData;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.UserRepo;
import com.app.Kaylia.services.AuthServices;
import com.app.Kaylia.services.MailServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthServices authServices;

    @Autowired
    private MailServices mailServices;


    @GetMapping("/login")
    public String loginPage(Model model){
        model.addAttribute("loginDto", new LoginDto());
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletRequest request){
        String role = authServices.verifyUser(loginDto, request);

        if(role == null){
            return ResponseEntity.badRequest().body(Map.of("message","Invalid Credentials"));
        }

        // Role-based redirect URL
        String redirectUrl;
        switch (role) {
            case "ROLE_SUPER_ADMIN", "ROLE_ADMIN" -> redirectUrl = "/admin/dashboard";
            case "ROLE_VENDOR" -> redirectUrl = "/vendor/dashboard";
            default -> redirectUrl = "/";
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login Successful",
                "redirect", redirectUrl
        ));
    }

    @GetMapping("/signup")
    public String signupPage(){
        return "signup";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody User user){

        String isCreated = authServices.registerUser(user);

        if(isCreated.equals("Successful")) {
            mailServices.sendWelcomeEmail(user.getEmail(), user.getFName());
            return ResponseEntity.ok(Map.of("message", "User Created Successfully"));
        } else if(isCreated.equals("Email Already in Use!")){
            return ResponseEntity.badRequest().body(Map.of("message", "Email Already in Use!"));
        }else {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to Register!"));
        }

    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear Spring Security context
        SecurityContextHolder.clearContext();

        // Redirect to index page
        return "redirect:/";
    }


    //for login status check in the landing page for dashboard
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("email") != null) {
            String email = (String) session.getAttribute("email");

            // Fetch user from DB to get role
            Optional<User> optionalUser = userRepo.findUserByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                String role = user.getRole();

                return ResponseEntity.ok(Map.of(
                        "loggedIn", true,
                        "email", email,
                        "role", role
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
                "loggedIn", false
        ));

    }

    @GetMapping("/reset-password")
    public String passwordRestPage(){
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String passwordReset(@ModelAttribute ResetPasswordData resetPasswordData){
        System.out.println("DTO values: " + resetPasswordData.getEmail() + " , " + resetPasswordData.getCurrentPassword());
        User user = userRepo.findByEmail(resetPasswordData.getEmail());

        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

        String password = bCrypt.encode(resetPasswordData.getCurrentPassword());

        user.setPassword(password);

        userRepo.save(user);

        return "login";
    }

    @PostMapping("/reset-user-verify")
    public String resetPasswordPage(@ModelAttribute ResetPasswordData resetPasswordData, Model model) {
        System.out.println("email: " + resetPasswordData.getEmail() +
                " password: " + resetPasswordData.getCurrentPassword());

        String email = resetPasswordData.getEmail();
        Optional<User> user = userRepo.findUserByEmail(email);

        if (user.isPresent()) {
            User activeUser = user.get();

            BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

            if (bCrypt.matches(resetPasswordData.getCurrentPassword(), activeUser.getPassword())) {
                model.addAttribute("user", resetPasswordData.getEmail());
                return "password-reset";
            }
        }

        model.addAttribute("error", "User not found or invalid password");
        return "reset-password";
    }


}
